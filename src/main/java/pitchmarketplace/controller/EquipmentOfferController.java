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
import pitchmarketplace.dto.EquipmentOfferDto;
import pitchmarketplace.dto.EquipmentOfferUpsertRequest;
import pitchmarketplace.service.EquipmentOfferService;

@RestController
@RequestMapping("/api/v1/equipment-offers")
@Tag(name = "Equipment Offers", description = "Operations for rentable equipment offers")
public class EquipmentOfferController {

    private final EquipmentOfferService service;

    public EquipmentOfferController(EquipmentOfferService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all equipment offers", description = "Returns all equipment rental offers.")
    public ResponseEntity<List<EquipmentOfferDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment offer by id", description = "Returns one equipment offer by identifier.")
    public ResponseEntity<EquipmentOfferDto> getById(@PathVariable @Positive(message = "id must be positive") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create equipment offer", description = "Creates a new equipment rental offer.")
    public ResponseEntity<EquipmentOfferDto> create(@Valid @RequestBody EquipmentOfferUpsertRequest request) {
        EquipmentOfferDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/equipment-offers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update equipment offer", description = "Updates an equipment rental offer by identifier.")
    public ResponseEntity<EquipmentOfferDto> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody EquipmentOfferUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete equipment offer", description = "Deletes an equipment rental offer by identifier.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id must be positive") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
