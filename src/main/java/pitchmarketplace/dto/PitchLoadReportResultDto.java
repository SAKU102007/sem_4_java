package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Calculated pitch load report")
public record PitchLoadReportResultDto(
        @Schema(description = "Pitch identifier", example = "1")
        Long pitchId,
        @Schema(description = "Pitch name", example = "Arena Nemiga")
        String pitchName,
        @Schema(description = "Total number of bookings", example = "12")
        long totalBookings,
        @Schema(description = "Number of confirmed bookings", example = "8")
        long confirmedBookings,
        @Schema(description = "Number of cancelled bookings", example = "1")
        long cancelledBookings,
        @Schema(description = "Total number of open game records", example = "4")
        long totalOpenGames,
        @Schema(description = "Number of open games still open for players", example = "2")
        long openOpenGames
) {
}
