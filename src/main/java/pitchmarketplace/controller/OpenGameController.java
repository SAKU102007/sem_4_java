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
import pitchmarketplace.dto.OpenGameDto;
import pitchmarketplace.dto.OpenGameUpsertRequest;
import pitchmarketplace.service.OpenGameService;

@RestController
@RequestMapping("/api/v1/open-games")
@Tag(name = "Open Games", description = "Operations for open football games")
public class OpenGameController {

    private final OpenGameService service;

    public OpenGameController(OpenGameService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all open games", description = "Returns every open game in the system.")
    public ResponseEntity<List<OpenGameDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get open game by id", description = "Returns one open game by identifier.")
    public ResponseEntity<OpenGameDto> getById(@PathVariable @Positive(message = "id must be positive") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create open game", description = "Creates an open game from the provided request body.")
    public ResponseEntity<OpenGameDto> create(@Valid @RequestBody OpenGameUpsertRequest request) {
        OpenGameDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/open-games/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update open game", description = "Updates an open game by identifier.")
    public ResponseEntity<OpenGameDto> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody OpenGameUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete open game", description = "Deletes an open game by identifier.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id must be positive") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
