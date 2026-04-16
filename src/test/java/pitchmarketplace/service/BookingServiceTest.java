package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    void shouldFindAllBookings() {
        Booking first = booking(1L, 11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CREATED);
        Booking second = booking(2L, 12L, 8L, "2026-05-02T18:00:00", "2026-05-02T20:00:00", BookingStatus.CONFIRMED);

        when(bookingRepository.findAll()).thenReturn(List.of(first, second));

        assertThat(bookingService.findAll())
                .extracting(BookingDto::id, BookingDto::pitchId, BookingDto::organizerId, BookingDto::status)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, 11L, 7L, BookingStatus.CREATED),
                        org.assertj.core.groups.Tuple.tuple(2L, 12L, 8L, BookingStatus.CONFIRMED)
                );
    }

    @Test
    void shouldFindBookingById() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(
                booking(10L, 11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CREATED)
        ));

        BookingDto result = bookingService.findById(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.pitchId()).isEqualTo(11L);
        assertThat(result.organizerId()).isEqualTo(7L);
    }

    @Test
    void shouldThrowWhenBookingNotFoundById() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Booking not found. id=99");
    }

    @Test
    void shouldCreateBooking() {
        Pitch pitch = new Pitch(11L, "Arena", PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN);
        User user = new User(7L, "Alexey", 77, UserRole.PLAYER);
        BookingUpsertRequest request = request(11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CREATED);

        when(pitchRepository.findById(11L)).thenReturn(Optional.of(pitch));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(101L);
            return booking;
        });

        BookingDto created = bookingService.create(request);

        assertThat(created.id()).isEqualTo(101L);
        assertThat(created.pitchId()).isEqualTo(11L);
        assertThat(created.organizerId()).isEqualTo(7L);
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldUpdateBooking() {
        Pitch existingPitch = new Pitch(10L, "Old Arena", PitchType.FIVE_TURF, "Old", "Old", java.math.BigDecimal.ONE);
        Pitch newPitch = new Pitch(11L, "Arena", PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN);
        User existingUser = new User(6L, "Old User", 55, UserRole.PLAYER);
        User newUser = new User(7L, "Alexey", 77, UserRole.PLAYER);
        Booking existing = new Booking(
                15L,
                existingPitch,
                existingUser,
                LocalDateTime.parse("2026-04-01T18:00:00"),
                LocalDateTime.parse("2026-04-01T20:00:00"),
                BookingStatus.CREATED
        );
        BookingUpsertRequest request = request(11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CONFIRMED);

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(existing));
        when(pitchRepository.findById(11L)).thenReturn(Optional.of(newPitch));
        when(userRepository.findById(7L)).thenReturn(Optional.of(newUser));
        when(bookingRepository.save(existing)).thenReturn(existing);

        BookingDto updated = bookingService.update(15L, request);

        assertThat(updated.id()).isEqualTo(15L);
        assertThat(updated.pitchId()).isEqualTo(11L);
        assertThat(updated.organizerId()).isEqualTo(7L);
        assertThat(updated.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(existing.getPitch()).isSameAs(newPitch);
        assertThat(existing.getOrganizer()).isSameAs(newUser);
        assertThat(existing.getStartAt()).isEqualTo(LocalDateTime.parse("2026-05-01T18:00:00"));
        assertThat(existing.getEndAt()).isEqualTo(LocalDateTime.parse("2026-05-01T20:00:00"));
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldDeleteBookingAndInvalidateCache() {
        Booking existing = booking(15L, 11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CREATED);

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(existing));

        bookingService.delete(15L);

        verify(bookingRepository).delete(existing);
        verify(bookingRepository).flush();
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldThrowWhenPitchIsMissingDuringCreate() {
        when(pitchRepository.findById(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(
                request(11L, 7L, "2026-05-01T18:00:00", "2026-05-01T20:00:00", BookingStatus.CREATED)
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pitch not found. id=11");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void shouldRejectNullBulkRequest() {
        assertThatThrownBy(() -> bookingService.createBulk(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one booking request is required");

        verifyNoInteractions(bookingRepository, pitchRepository, userRepository);
        assertThat(bookingSearchService.invalidated).isFalse();
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

    @Test
    void shouldCreateBulkWithoutTransactionWhenAllItemsAreValid() {
        Pitch pitch = new Pitch(11L, "Arena", PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN);
        User user = new User(7L, "Alexey", 77, UserRole.PLAYER);

        when(pitchRepository.findById(anyLong())).thenReturn(Optional.of(pitch));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        AtomicLong sequence = new AtomicLong(300L);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(sequence.getAndIncrement());
            return booking;
        });

        List<BookingDto> created = bookingService.createBulkWithoutTransaction(List.of(
                request(11L, 7L, "2026-05-03T18:00:00", "2026-05-03T20:00:00", BookingStatus.CREATED),
                request(11L, 7L, "2026-05-04T18:00:00", "2026-05-04T20:00:00", BookingStatus.CONFIRMED)
        ));

        assertThat(created)
                .extracting(BookingDto::id, BookingDto::status)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(300L, BookingStatus.CREATED),
                        org.assertj.core.groups.Tuple.tuple(301L, BookingStatus.CONFIRMED)
                );
        verify(bookingRepository, times(2)).save(any(Booking.class));
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldRejectEmptyBulkRequestWithoutTransactionAndKeepCacheUntouched() {
        assertThatThrownBy(() -> bookingService.createBulkWithoutTransaction(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one booking request is required");

        verifyNoInteractions(bookingRepository, pitchRepository, userRepository);
        assertThat(bookingSearchService.invalidated).isFalse();
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

    private Booking booking(
            Long id,
            Long pitchId,
            Long organizerId,
            String startAt,
            String endAt,
            BookingStatus status
    ) {
        return new Booking(
                id,
                new Pitch(pitchId, "Arena " + pitchId, PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN),
                new User(organizerId, "User " + organizerId, 70, UserRole.PLAYER),
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
