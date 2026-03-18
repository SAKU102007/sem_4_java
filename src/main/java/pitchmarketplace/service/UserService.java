package pitchmarketplace.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.dto.UserDto;
import pitchmarketplace.dto.UserUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final BookingSearchService bookingSearchService;

    public UserService(UserRepository repository, BookingSearchService bookingSearchService) {
        this.repository = repository;
        this.bookingSearchService = bookingSearchService;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    public UserDto create(UserUpsertRequest request) {
        User user = new User();
        applyRequest(user, request);
        User savedUser = repository.save(user);
        bookingSearchService.invalidateCache();
        return toDto(savedUser);
    }

    public UserDto update(Long id, UserUpsertRequest request) {
        User user = getOrThrow(id);
        applyRequest(user, request);
        User savedUser = repository.save(user);
        bookingSearchService.invalidateCache();
        return toDto(savedUser);
    }

    public void delete(Long id) {
        User user = getOrThrow(id);
        repository.delete(user);
        repository.flush();
        bookingSearchService.invalidateCache();
    }

    private User getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found. id=" + id));
    }

    private void applyRequest(User user, UserUpsertRequest request) {
        user.setName(request.name());
        user.setRating(request.rating());
        user.setRole(request.role());
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getRating(),
                user.getRole()
        );
    }
}
