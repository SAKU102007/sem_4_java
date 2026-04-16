package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.dto.BookingSearchCriteria;
import pitchmarketplace.dto.BookingSearchResponseDto;
import pitchmarketplace.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingSearchServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingSearchService bookingSearchService;

    @BeforeEach
    void setUp() {
        bookingSearchService = new BookingSearchService(bookingRepository);
    }

    @Test
    void shouldSearchWithJpqlAndReuseCache() {
        BookingSearchCriteria criteria = new BookingSearchCriteria(
                " Central ",
                PitchType.EIGHT,
                " Alexey ",
                BookingStatus.CREATED,
                LocalDateTime.parse("2026-05-01T18:00:00"),
                LocalDateTime.parse("2026-05-03T18:00:00")
        );
        when(bookingRepository.searchWithFiltersJpql(
                eq("central"),
                eq(PitchType.EIGHT),
                eq("alexey"),
                eq(BookingStatus.CREATED),
                eq(LocalDateTime.parse("2026-05-01T18:00:00")),
                eq(LocalDateTime.parse("2026-05-03T18:00:00")),
                eq(PageRequest.of(0, 50))
        )).thenReturn(new PageImpl<>(List.of(booking(1L)), PageRequest.of(0, 50), 1));

        BookingSearchResponseDto first = bookingSearchService.searchWithJpql(criteria, -3, 1000);
        BookingSearchResponseDto second = bookingSearchService.searchWithJpql(criteria, -3, 1000);

        assertThat(first.cacheHit()).isFalse();
        assertThat(first.pageNumber()).isEqualTo(0);
        assertThat(first.pageSize()).isEqualTo(50);
        assertThat(first.content()).hasSize(1);
        assertThat(second.cacheHit()).isTrue();
        verify(bookingRepository, times(1)).searchWithFiltersJpql(
                eq("central"),
                eq(PitchType.EIGHT),
                eq("alexey"),
                eq(BookingStatus.CREATED),
                eq(LocalDateTime.parse("2026-05-01T18:00:00")),
                eq(LocalDateTime.parse("2026-05-03T18:00:00")),
                eq(PageRequest.of(0, 50))
        );
    }

    @Test
    void shouldSearchWithNativeAndNormalizeNulls() {
        BookingSearchCriteria criteria = new BookingSearchCriteria(
                null,
                null,
                "   ",
                null,
                null,
                null
        );
        when(bookingRepository.searchWithFiltersNative(
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(PageRequest.of(0, 5))
        )).thenReturn(new PageImpl<>(List.of(booking(2L)), PageRequest.of(0, 5), 1));

        BookingSearchResponseDto response = bookingSearchService.searchWithNative(criteria, null, null);

        assertThat(response.queryType()).isEqualTo("native");
        assertThat(response.cacheHit()).isFalse();
        assertThat(response.pageNumber()).isEqualTo(0);
        assertThat(response.pageSize()).isEqualTo(5);
        assertThat(response.content()).extracting(dto -> dto.id()).containsExactly(2L);
    }

    @Test
    void shouldInvalidateCacheAndQueryAgain() {
        BookingSearchCriteria criteria = new BookingSearchCriteria(
                "Central",
                null,
                null,
                null,
                null,
                null
        );
        when(bookingRepository.searchWithFiltersJpql(
                eq("central"),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(booking(1L)), PageRequest.of(0, 5), 1));

        bookingSearchService.searchWithJpql(criteria, 0, 5);
        bookingSearchService.invalidateCache();
        bookingSearchService.searchWithJpql(criteria, 0, 5);

        verify(bookingRepository, times(2)).searchWithFiltersJpql(
                eq("central"),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(PageRequest.class)
        );
    }

    private Booking booking(Long id) {
        return new Booking(
                id,
                new Pitch(10L, "Arena", PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN),
                new User(7L, "Alexey", 78, UserRole.PLAYER),
                LocalDateTime.parse("2026-05-01T18:00:00"),
                LocalDateTime.parse("2026-05-01T20:00:00"),
                BookingStatus.CREATED
        );
    }
}
