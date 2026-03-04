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

    public UserService(UserRepository repository) {
        this.repository = repository;
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
        return toDto(repository.save(user));
    }

    public UserDto update(Long id, UserUpsertRequest request) {
        User user = getOrThrow(id);
        applyRequest(user, request);
        return toDto(repository.save(user));
    }

    public void delete(Long id) {
        User user = getOrThrow(id);
        repository.delete(user);
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
