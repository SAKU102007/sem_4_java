package pitchmarketplace.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pitchmarketplace.domain.entity.OpenGame;
import pitchmarketplace.domain.enums.OpenGameStatus;

public interface OpenGameRepository extends JpaRepository<OpenGame, Long> {

    long countByBookingPitch_Id(Long pitchId);

    long countByBookingPitch_IdAndStatus(Long pitchId, OpenGameStatus status);

    @EntityGraph(attributePaths = {"participants"})
    @Query("select og from OpenGame og")
    List<OpenGame> findAllWithParticipantsEntityGraph();
}
