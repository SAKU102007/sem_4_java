package pitchmarketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pitchmarketplace.dto.UserDto;
import pitchmarketplace.dto.UserUpsertRequest;
import pitchmarketplace.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operations for marketplace users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns every user available in the marketplace.")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Returns a single user by its identifier.")
    public ResponseEntity<UserDto> getById(@PathVariable @Positive(message = "id must be positive") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user from the provided request body.")
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserUpsertRequest request) {
        UserDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user by identifier.")
    public ResponseEntity<UserDto> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody UserUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by identifier.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id must be positive") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
