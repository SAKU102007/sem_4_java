package pitchmarketplace.service;

import pitchmarketplace.dto.PitchDto;
import pitchmarketplace.mapper.PitchMapper;
import pitchmarketplace.repository.PitchRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PitchService {

    private final PitchRepository repository;
    private final PitchMapper mapper;

    public PitchService(PitchRepository repository, PitchMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PitchDto> findAll(String district) {
        if (district == null || district.isBlank()) {
            return repository.findAll().stream()
                    .map(mapper::toDto)
                    .toList();
        }
        return repository.findByDistrict(district.trim()).stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<PitchDto> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto);
    }
}
