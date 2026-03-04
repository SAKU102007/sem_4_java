package pitchmarketplace.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pitchmarketplace.domain.entity.Pitch;

public interface PitchRepository extends JpaRepository<Pitch, Long> {

    List<Pitch> findByDistrictIgnoreCase(String district);
}
