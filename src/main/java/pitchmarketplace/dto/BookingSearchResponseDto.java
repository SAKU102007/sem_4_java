package pitchmarketplace.dto;

import java.util.List;

public record BookingSearchResponseDto(
        String queryType,
        boolean cacheHit,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<BookingDto> content
) {

    public BookingSearchResponseDto markCacheHit() {
        return new BookingSearchResponseDto(
                queryType,
                true,
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                first,
                last,
                content
        );
    }
}
