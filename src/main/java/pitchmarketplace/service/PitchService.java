package pitchmarketplace.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.dto.PitchDto;
import pitchmarketplace.dto.PitchUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.PitchRepository;

@Service
@Transactional
public class PitchService {

    private final PitchRepository repository;

    public PitchService(PitchRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PitchDto> findAll(String district) {
        if (district == null || district.isBlank()) {
            return repository.findAll().stream()
                    .map(this::toDto)
                    .toList();
        }
        return repository.findByDistrictIgnoreCase(district.trim()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PitchDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    public PitchDto create(PitchUpsertRequest request) {
        Pitch pitch = new Pitch();
        applyRequest(pitch, request);
        return toDto(repository.save(pitch));
    }

    public PitchDto update(Long id, PitchUpsertRequest request) {
        Pitch pitch = getOrThrow(id);
        applyRequest(pitch, request);
        return toDto(repository.save(pitch));
    }

    public void delete(Long id) {
        Pitch pitch = getOrThrow(id);
        repository.delete(pitch);
    }

    private Pitch getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pitch not found. id=" + id));
    }

    private void applyRequest(Pitch pitch, PitchUpsertRequest request) {
        pitch.setName(request.name());
        pitch.setType(request.type());
        pitch.setDistrict(request.district());
        pitch.setMetro(request.metro());
        pitch.setPricePerHour(request.pricePerHour());
    }

    private PitchDto toDto(Pitch pitch) {
        return new PitchDto(
                pitch.getId(),
                pitch.getName(),
                pitch.getType(),
                pitch.getDistrict(),
                pitch.getMetro(),
                pitch.getPricePerHour()
        );
    }
}
