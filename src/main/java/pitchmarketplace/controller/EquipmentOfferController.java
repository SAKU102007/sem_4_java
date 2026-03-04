package pitchmarketplace.controller;

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
public class EquipmentOfferController {

    private final EquipmentOfferService service;

    public EquipmentOfferController(EquipmentOfferService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentOfferDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentOfferDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<EquipmentOfferDto> create(@RequestBody EquipmentOfferUpsertRequest request) {
        EquipmentOfferDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/equipment-offers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentOfferDto> update(@PathVariable Long id, @RequestBody EquipmentOfferUpsertRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
