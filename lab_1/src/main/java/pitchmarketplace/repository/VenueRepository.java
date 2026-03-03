package pitchmarketplace.repository;

import pitchmarketplace.domain.entity.Venue;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    @EntityGraph(attributePaths = {"pitches", "pitches.equipmentOffers"})
    Optional<Venue> findById(Long id);
}
