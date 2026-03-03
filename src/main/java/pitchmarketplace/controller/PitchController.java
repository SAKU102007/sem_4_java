package pitchmarketplace.controller;

import pitchmarketplace.dto.PitchDto;
import pitchmarketplace.service.PitchService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pitches")
public class PitchController {

    private final PitchService service;

    public PitchController(PitchService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PitchDto>> getAll(@RequestParam(required = false) String district) {
        return ResponseEntity.ok(service.findAll(district));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PitchDto> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
