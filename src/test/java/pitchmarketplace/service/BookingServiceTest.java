package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.PitchRepository;
import pitchmarketplace.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PitchRepository pitchRepository;

    @Mock
    private UserRepository userRepository;

    private BookingService bookingService;
    private TrackingBookingSearchService bookingSearchService;

    @BeforeEach
    void setUp() {
        bookingSearchService = new TrackingBookingSearchService(bookingRepository);
        bookingService = new BookingService(
                bookingRepository,
                pitchRepository,
                userRepository,
                bookingSearchService
        );
    }

    @Test
    void shouldCreateBookingsInBulkAndInvalidateCache() {
        Pitch pitch = new Pitch(11L, "Arena", PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN);
        User user = new User(7L, "Alexey", 77, UserRole.PLAYER);

        when(pitchRepository.findById(anyLong())).thenReturn(Optional.of(pitch));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        AtomicLong sequence = new AtomicLong(100L);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(sequence.getAndIncrement());
            return booking;
        });

        List<BookingDto> created = bookingService.createBulk(List.of(
                request(11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CREATED),
                request(11L, 7L, "2026-05-02T18:00:00", "2026-05-02T20:00:00", BookingStatus.CONFIRMED)
        ));

        assertThat(created)
                .hasSize(2)
                .extracting(BookingDto::id)
                .containsExactly(100L, 101L);
        assertThat(created)
                .extracting(BookingDto::pitchId, BookingDto::organizerId)
                .containsOnly(org.assertj.core.groups.Tuple.tuple(11L, 7L));

        org.mockito.Mockito.verify(bookingRepository, times(2)).save(any(Booking.class));
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldRejectEmptyBulkRequest() {
        assertThatThrownBy(() -> bookingService.createBulk(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one booking request is required");

        verifyNoInteractions(bookingRepository, pitchRepository, userRepository);
        assertThat(bookingSearchService.invalidated).isFalse();
    }

    @Test
    void shouldLeavePreviouslySavedItemsWhenBulkFailsWithoutTransaction() {
        Pitch pitch = new Pitch(11L, "Arena", PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN);
        User user = new User(7L, "Alexey", 77, UserRole.PLAYER);

        when(pitchRepository.findById(anyLong())).thenReturn(Optional.of(pitch));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userRepository.findById(999999L)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(500L);
            return booking;
        });

        assertThatThrownBy(() -> bookingService.createBulkWithoutTransaction(List.of(
                request(11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CREATED),
                request(11L, 999999L, "2026-05-02T18:00:00", "2026-05-02T20:00:00", BookingStatus.CONFIRMED)
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found. id=999999");

        org.mockito.Mockito.verify(bookingRepository, times(1)).save(any(Booking.class));
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    private BookingUpsertRequest request(
            Long pitchId,
            Long organizerId,
            String startAt,
            String endAt,
            BookingStatus status
    ) {
        return new BookingUpsertRequest(
                pitchId,
                organizerId,
                LocalDateTime.parse(startAt),
                LocalDateTime.parse(endAt),
                status
        );
    }

    private static final class TrackingBookingSearchService extends BookingSearchService {

        private boolean invalidated;

        private TrackingBookingSearchService(BookingRepository bookingRepository) {
            super(bookingRepository);
        }

        @Override
        public void invalidateCache() {
            invalidated = true;
            super.invalidateCache();
        }
    }
}
