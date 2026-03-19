package pitchmarketplace.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingSearchCriteria;
import pitchmarketplace.dto.BookingSearchResponseDto;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.service.cache.BookingSearchCacheKey;

@Service
public class BookingSearchService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 5;
    private static final int MAX_PAGE_SIZE = 50;

    private final BookingRepository bookingRepository;
    private final Map<BookingSearchCacheKey, BookingSearchResponseDto> searchCache = new HashMap<>();

    public BookingSearchService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public BookingSearchResponseDto searchWithJpql(
            BookingSearchCriteria criteria,
            Integer page,
            Integer size
    ) {
        PageRequest pageRequest = createPageRequest(page, size);
        BookingSearchCacheKey cacheKey = new BookingSearchCacheKey(
                "jpql",
                criteria,
                pageRequest.getPageNumber(),
                pageRequest.getPageSize()
        );
        return search(
                cacheKey,
                "jpql",
                () -> bookingRepository.searchWithFiltersJpql(
                        criteria.district(),
                        criteria.pitchType(),
                        criteria.organizerName(),
                        criteria.status(),
                        criteria.startFrom(),
                        criteria.startTo(),
                        pageRequest
                )
        );
    }

    @Transactional(readOnly = true)
    public BookingSearchResponseDto searchWithNative(
            BookingSearchCriteria criteria,
            Integer page,
            Integer size
    ) {
        PageRequest pageRequest = createPageRequest(page, size);
        BookingSearchCacheKey cacheKey = new BookingSearchCacheKey(
                "native",
                criteria,
                pageRequest.getPageNumber(),
                pageRequest.getPageSize()
        );
        return search(
                cacheKey,
                "native",
                () -> bookingRepository.searchWithFiltersNative(
                        criteria.district(),
                        criteria.pitchType() == null ? null : criteria.pitchType().name(),
                        criteria.organizerName(),
                        criteria.status() == null ? null : criteria.status().name(),
                        criteria.startFrom(),
                        criteria.startTo(),
                        pageRequest
                )
        );
    }

    public void invalidateCache() {
        synchronized (searchCache) {
            searchCache.clear();
        }
    }

    private BookingSearchResponseDto search(
            BookingSearchCacheKey cacheKey,
            String queryType,
            Supplier<Page<Booking>> querySupplier
    ) {
        synchronized (searchCache) {
            BookingSearchResponseDto cachedResponse = searchCache.get(cacheKey);
            if (cachedResponse != null) {
                return cachedResponse.markCacheHit();
            }
        }

        BookingSearchResponseDto freshResponse = toDto(queryType, querySupplier.get());
        synchronized (searchCache) {
            searchCache.put(cacheKey, freshResponse);
        }
        return freshResponse;
    }

    private PageRequest createPageRequest(Integer page, Integer size) {
        int normalizedPage = page == null ? DEFAULT_PAGE : Math.max(page, DEFAULT_PAGE);
        int normalizedSize = size == null ? DEFAULT_SIZE : Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        return PageRequest.of(normalizedPage, normalizedSize);
    }

    private BookingSearchResponseDto toDto(String queryType, Page<Booking> page) {
        List<BookingDto> content = page.getContent().stream()
                .map(this::toDto)
                .toList();
        return new BookingSearchResponseDto(
                queryType,
                false,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                content
        );
    }

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getPitch().getId(),
                booking.getOrganizer().getId(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getStatus()
        );
    }
}
