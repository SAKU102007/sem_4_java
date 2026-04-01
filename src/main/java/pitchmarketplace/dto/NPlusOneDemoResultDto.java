package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of the N+1 demonstration endpoint")
public record NPlusOneDemoResultDto(
        @Schema(description = "Demo mode", example = "bad")
        String mode,
        @Schema(description = "Number of open games fetched", example = "3")
        int openGamesCount,
        @Schema(description = "Total number of participants", example = "18")
        long totalParticipants,
        @Schema(description = "SQL statements executed during the demo", example = "7")
        long executedStatements
) {
}
