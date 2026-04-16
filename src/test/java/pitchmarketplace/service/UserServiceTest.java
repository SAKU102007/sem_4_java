package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.dto.UserDto;
import pitchmarketplace.dto.UserUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    private UserService userService;
    private TrackingBookingSearchService bookingSearchService;

    @BeforeEach
    void setUp() {
        bookingSearchService = new TrackingBookingSearchService();
        userService = new UserService(repository, bookingSearchService);
    }

    @Test
    void shouldFindAllUsers() {
        when(repository.findAll()).thenReturn(List.of(
                new User(1L, "Alexey", 78, UserRole.PLAYER),
                new User(2L, "Kirill", 55, UserRole.VENUE_OWNER)
        ));

        assertThat(userService.findAll())
                .extracting(UserDto::id, UserDto::name, UserDto::role)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, "Alexey", UserRole.PLAYER),
                        org.assertj.core.groups.Tuple.tuple(2L, "Kirill", UserRole.VENUE_OWNER)
                );
    }

    @Test
    void shouldFindUserById() {
        when(repository.findById(7L)).thenReturn(Optional.of(new User(7L, "Alexey", 78, UserRole.PLAYER)));

        UserDto user = userService.findById(7L);

        assertThat(user.id()).isEqualTo(7L);
        assertThat(user.name()).isEqualTo("Alexey");
        assertThat(user.rating()).isEqualTo(78);
        assertThat(user.role()).isEqualTo(UserRole.PLAYER);
    }

    @Test
    void shouldThrowWhenUserIsMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found. id=99");
    }

    @Test
    void shouldCreateUserAndInvalidateCache() {
        UserUpsertRequest request = new UserUpsertRequest("Denis", 64, UserRole.ADMIN);
        when(repository.save(org.mockito.ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserDto created = userService.create(request);

        assertThat(created.id()).isEqualTo(10L);
        assertThat(created.name()).isEqualTo("Denis");
        assertThat(created.rating()).isEqualTo(64);
        assertThat(created.role()).isEqualTo(UserRole.ADMIN);
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldUpdateUserAndInvalidateCache() {
        User existing = new User(10L, "Old Name", 10, UserRole.PLAYER);
        UserUpsertRequest request = new UserUpsertRequest("New Name", 91, UserRole.ADMIN);

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserDto updated = userService.update(10L, request);

        assertThat(updated.id()).isEqualTo(10L);
        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.rating()).isEqualTo(91);
        assertThat(updated.role()).isEqualTo(UserRole.ADMIN);
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldDeleteUserAndInvalidateCache() {
        User existing = new User(10L, "Alexey", 78, UserRole.PLAYER);
        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        userService.delete(10L);

        verify(repository).delete(existing);
        verify(repository).flush();
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    private static final class TrackingBookingSearchService extends BookingSearchService {

        private boolean invalidated;

        private TrackingBookingSearchService() {
            super(org.mockito.Mockito.mock(pitchmarketplace.repository.BookingRepository.class));
        }

        @Override
        public void invalidateCache() {
            invalidated = true;
        }
    }
}
