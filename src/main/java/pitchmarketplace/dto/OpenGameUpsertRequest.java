package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import pitchmarketplace.domain.enums.OpenGameStatus;

@Schema(description = "Payload for creating or updating an open game")
public record OpenGameUpsertRequest(
        @NotNull(message = "bookingId is required")
        @Positive(message = "bookingId must be positive")
        @Schema(description = "Booking identifier", example = "101")
        Long bookingId,
        @NotNull(message = "organizerId is required")
        @Positive(message = "organizerId must be positive")
        @Schema(description = "Organizer identifier", example = "7")
        Long organizerId,
        @NotNull(message = "targetSkillMin is required")
        @Min(value = 0, message = "targetSkillMin must be at least 0")
        @Max(value = 100, message = "targetSkillMin must be at most 100")
        @Schema(description = "Minimum target skill", example = "40")
        Integer targetSkillMin,
        @NotNull(message = "targetSkillMax is required")
        @Min(value = 0, message = "targetSkillMax must be at least 0")
        @Max(value = 100, message = "targetSkillMax must be at most 100")
        @Schema(description = "Maximum target skill", example = "80")
        Integer targetSkillMax,
        @NotNull(message = "maxPlayers is required")
        @Min(value = 2, message = "maxPlayers must be at least 2")
        @Max(value = 50, message = "maxPlayers must be at most 50")
        @Schema(description = "Maximum number of players", example = "12")
        Integer maxPlayers,
        @NotNull(message = "status is required")
        @Schema(description = "Open game status")
        OpenGameStatus status,
        @Schema(description = "Identifiers of participants to register")
        List<@Positive(message = "participantId must be positive") Long> participantIds
) {

    @AssertTrue(message = "targetSkillMin must be less than or equal to targetSkillMax")
    public boolean isSkillRangeValid() {
        if (targetSkillMin == null || targetSkillMax == null) {
            return true;
        }
        return targetSkillMin <= targetSkillMax;
    }
}
