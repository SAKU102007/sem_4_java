package by.bsuir.pitchmarketplace.repository;

import by.bsuir.pitchmarketplace.domain.entity.Pitch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PitchRepository extends JpaRepository<Pitch, Long> {

    @EntityGraph(attributePaths = {"venue", "equipmentOffers"})
    List<Pitch> findByActiveTrue();

    @EntityGraph(attributePaths = {"venue", "equipmentOffers"})
    Optional<Pitch> findById(Long id);
}
