package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.OpenGame;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.OpenGameStatus;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.dto.OpenGameDto;
import pitchmarketplace.dto.OpenGameUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OpenGameServiceTest {

    @Mock
    private OpenGameRepository openGameRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    private OpenGameService service;

    @BeforeEach
    void setUp() {
        service = new OpenGameService(openGameRepository, bookingRepository, userRepository);
    }

    @Test
    void shouldFindAllOpenGamesWithSortedParticipants() {
        OpenGame openGame = openGame(1L, 10L, 7L, Set.of(user(9L), user(8L)));
        when(openGameRepository.findAll()).thenReturn(List.of(openGame));

        assertThat(service.findAll())
                .extracting(OpenGameDto::id, OpenGameDto::participantIds)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1L, List.of(8L, 9L)));
    }

    @Test
    void shouldFindOpenGameById() {
        when(openGameRepository.findById(1L)).thenReturn(Optional.of(openGame(1L, 10L, 7L, Set.of(user(9L), user(8L)))));

        OpenGameDto result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.bookingId()).isEqualTo(10L);
        assertThat(result.organizerId()).isEqualTo(7L);
        assertThat(result.participantIds()).containsExactly(8L, 9L);
    }

    @Test
    void shouldThrowWhenOpenGameIsMissing() {
        when(openGameRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Open game not found. id=42");
    }

    @Test
    void shouldCreateOpenGameWhenParticipantsAreNull() {
        Booking booking = booking(10L, 7L);
        User organizer = user(7L);
        OpenGameUpsertRequest request = new OpenGameUpsertRequest(
                10L,
                7L,
                40,
                70,
                12,
                OpenGameStatus.OPEN,
                null
        );

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(7L)).thenReturn(Optional.of(organizer));
        when(openGameRepository.save(org.mockito.ArgumentMatchers.any(OpenGame.class))).thenAnswer(invocation -> {
            OpenGame openGame = invocation.getArgument(0);
            openGame.setId(3L);
            return openGame;
        });

        OpenGameDto created = service.create(request);

        assertThat(created.id()).isEqualTo(3L);
        assertThat(created.bookingId()).isEqualTo(10L);
        assertThat(created.organizerId()).isEqualTo(7L);
        assertThat(created.participantIds()).isEmpty();
    }

    @Test
    void shouldUpdateOpenGameAndDeduplicateParticipants() {
        OpenGame existing = openGame(3L, 10L, 7L, Set.of(user(5L)));
        Booking booking = booking(10L, 7L);
        User organizer = user(7L);
        User participantNine = user(9L);
        User participantEight = user(8L);
        OpenGameUpsertRequest request = new OpenGameUpsertRequest(
                10L,
                7L,
                50,
                90,
                14,
                OpenGameStatus.FULL,
                List.of(9L, 8L, 9L)
        );

        when(openGameRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(7L)).thenReturn(Optional.of(organizer));
        when(userRepository.findById(9L)).thenReturn(Optional.of(participantNine));
        when(userRepository.findById(8L)).thenReturn(Optional.of(participantEight));
        when(openGameRepository.save(existing)).thenReturn(existing);

        OpenGameDto updated = service.update(3L, request);

        assertThat(updated.id()).isEqualTo(3L);
        assertThat(updated.targetSkillMin()).isEqualTo(50);
        assertThat(updated.targetSkillMax()).isEqualTo(90);
        assertThat(updated.maxPlayers()).isEqualTo(14);
        assertThat(updated.status()).isEqualTo(OpenGameStatus.FULL);
        assertThat(updated.participantIds()).containsExactly(8L, 9L);
    }

    @Test
    void shouldDeleteOpenGame() {
        OpenGame existing = openGame(3L, 10L, 7L, Set.of());
        when(openGameRepository.findById(3L)).thenReturn(Optional.of(existing));

        service.delete(3L);

        verify(openGameRepository).delete(existing);
    }

    @Test
    void shouldThrowWhenBookingIsMissingDuringCreate() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new OpenGameUpsertRequest(
                10L,
                7L,
                40,
                70,
                12,
                OpenGameStatus.OPEN,
                List.of()
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Booking not found. id=10");
    }

    @Test
    void shouldThrowWhenOrganizerIsMissingDuringCreate() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking(10L, 7L)));
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new OpenGameUpsertRequest(
                10L,
                7L,
                40,
                70,
                12,
                OpenGameStatus.OPEN,
                List.of()
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found. id=7");
    }

    @Test
    void shouldThrowWhenParticipantIsMissingDuringUpdate() {
        OpenGame existing = openGame(3L, 10L, 7L, Set.of());
        Booking booking = booking(10L, 7L);
        User organizer = user(7L);

        when(openGameRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(7L)).thenReturn(Optional.of(organizer));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(3L, new OpenGameUpsertRequest(
                10L,
                7L,
                40,
                70,
                12,
                OpenGameStatus.OPEN,
                List.of(99L)
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found. id=99");
    }

    private OpenGame openGame(Long id, Long bookingId, Long organizerId, Set<User> participants) {
        OpenGame openGame = new OpenGame(
                id,
                booking(bookingId, organizerId),
                user(organizerId),
                40,
                70,
                12,
                OpenGameStatus.OPEN
        );
        openGame.setParticipants(participants);
        openGame.setOrganizer(user(organizerId));
        return openGame;
    }

    private Booking booking(Long bookingId, Long organizerId) {
        return new Booking(
                bookingId,
                new Pitch(1L, "Arena", PitchType.EIGHT, "Central", "Nemiga", java.math.BigDecimal.TEN),
                user(organizerId),
                LocalDateTime.parse("2026-05-01T18:00:00"),
                LocalDateTime.parse("2026-05-01T20:00:00"),
                BookingStatus.CREATED
        );
    }

    private User user(Long id) {
        return new User(id, "User " + id, 70, UserRole.PLAYER);
    }
}
