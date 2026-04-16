package pitchmarketplace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.OpenGame;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.PitchRepository;
import pitchmarketplace.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SeedDataTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PitchRepository pitchRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private OpenGameRepository openGameRepository;

    private SeedData seedData;

    @BeforeEach
    void setUp() {
        seedData = new SeedData(userRepository, pitchRepository, bookingRepository, openGameRepository);
    }

    @Test
    void shouldSkipSeedingWhenUsersAlreadyExist() throws Exception {
        when(userRepository.count()).thenReturn(1L);

        seedData.run();

        verify(userRepository).count();
        verifyNoInteractions(pitchRepository, bookingRepository, openGameRepository);
    }

    @Test
    void shouldSkipSeedingWhenPitchesAlreadyExist() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(pitchRepository.count()).thenReturn(1L);

        seedData.run();

        verify(userRepository).count();
        verify(pitchRepository).count();
        verifyNoInteractions(bookingRepository, openGameRepository);
    }

    @Test
    void shouldSkipSeedingWhenBookingsAlreadyExist() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(pitchRepository.count()).thenReturn(0L);
        when(bookingRepository.count()).thenReturn(1L);

        seedData.run();

        verify(bookingRepository).count();
        verifyNoInteractions(openGameRepository);
    }

    @Test
    void shouldSkipSeedingWhenOpenGamesAlreadyExist() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(pitchRepository.count()).thenReturn(0L);
        when(bookingRepository.count()).thenReturn(0L);
        when(openGameRepository.count()).thenReturn(1L);

        seedData.run();

        verify(openGameRepository).count();
    }

    @Test
    void shouldSeedDatabaseWhenItIsEmpty() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(pitchRepository.count()).thenReturn(0L);
        when(bookingRepository.count()).thenReturn(0L);
        when(openGameRepository.count()).thenReturn(0L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pitchRepository.save(any(Pitch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(openGameRepository.save(any(OpenGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        seedData.run();

        ArgumentCaptor<Pitch> pitchCaptor = ArgumentCaptor.forClass(Pitch.class);
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        ArgumentCaptor<OpenGame> openGameCaptor = ArgumentCaptor.forClass(OpenGame.class);

        verify(userRepository, times(10)).save(any(User.class));
        verify(pitchRepository, times(6)).save(pitchCaptor.capture());
        verify(bookingRepository, times(6)).save(bookingCaptor.capture());
        verify(openGameRepository, times(6)).save(openGameCaptor.capture());

        Pitch firstPitch = pitchCaptor.getAllValues().get(0);
        Booking firstBooking = bookingCaptor.getAllValues().get(0);
        OpenGame firstOpenGame = openGameCaptor.getAllValues().get(0);

        assertThat(firstPitch.getEquipmentOffers()).hasSize(2);
        assertThat(firstPitch.getEquipmentOffers())
                .allMatch(offer -> offer.getPitch() == firstPitch);
        assertThat(firstBooking.getEndAt())
                .isEqualTo(firstBooking.getStartAt().plus(2, ChronoUnit.HOURS));
        assertThat(firstOpenGame.getParticipants()).hasSize(4);
        assertThat(firstOpenGame.getBooking()).isSameAs(firstBooking);
        assertThat(firstOpenGame.getOrganizer()).isNotNull();
        assertThat(List.copyOf(firstOpenGame.getParticipants()))
                .extracting(User::getName)
                .contains("Алексей");
    }
}
