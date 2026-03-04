package pitchmarketplace.dto;

import java.util.List;
import pitchmarketplace.domain.enums.OpenGameStatus;

public record OpenGameDto(
        Long id,
        Long bookingId,
        Long organizerId,
        Integer targetSkillMin,
        Integer targetSkillMax,
        Integer maxPlayers,
        OpenGameStatus status,
        List<Long> participantIds
) {
}
