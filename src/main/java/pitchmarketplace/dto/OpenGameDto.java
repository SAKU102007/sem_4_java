package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import pitchmarketplace.domain.enums.OpenGameStatus;

@Schema(description = "Open game response payload")
public record OpenGameDto(
        @Schema(description = "Open game identifier", example = "21")
        Long id,
        @Schema(description = "Related booking identifier", example = "101")
        Long bookingId,
        @Schema(description = "Organizer identifier", example = "7")
        Long organizerId,
        @Schema(description = "Minimum target skill", example = "40")
        Integer targetSkillMin,
        @Schema(description = "Maximum target skill", example = "80")
        Integer targetSkillMax,
        @Schema(description = "Maximum number of players", example = "12")
        Integer maxPlayers,
        @Schema(description = "Open game status")
        OpenGameStatus status,
        @Schema(description = "Identifiers of registered participants")
        List<Long> participantIds
) {
}
