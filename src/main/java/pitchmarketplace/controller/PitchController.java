package pitchmarketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pitchmarketplace.dto.PitchDto;
import pitchmarketplace.dto.PitchUpsertRequest;
import pitchmarketplace.service.PitchService;

@RestController
@RequestMapping("/api/v1/pitches")
@Tag(name = "Pitches", description = "Operations for football pitches")
public class PitchController {

    private final PitchService service;

    public PitchController(PitchService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get pitches", description = "Returns all pitches or filters them by district.")
    public ResponseEntity<List<PitchDto>> getAll(
            @RequestParam(required = false)
            @Size(max = 100, message = "district must be at most 100 characters") String district
    ) {
        return ResponseEntity.ok(service.findAll(district));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pitch by id", description = "Returns a single pitch by its identifier.")
    public ResponseEntity<PitchDto> getById(@PathVariable @Positive(message = "id must be positive") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create pitch", description = "Creates a new football pitch.")
    public ResponseEntity<PitchDto> create(@Valid @RequestBody PitchUpsertRequest request) {
        PitchDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/pitches/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update pitch", description = "Updates a football pitch by identifier.")
    public ResponseEntity<PitchDto> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody PitchUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pitch", description = "Deletes a football pitch by identifier.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id must be positive") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
