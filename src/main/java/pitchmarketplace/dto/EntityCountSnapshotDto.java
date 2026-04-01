package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Entity counters captured before or after a demo operation")
public record EntityCountSnapshotDto(
        @Schema(description = "Number of users", example = "10")
        long users,
        @Schema(description = "Number of pitches", example = "5")
        long pitches,
        @Schema(description = "Number of bookings", example = "17")
        long bookings,
        @Schema(description = "Number of open games", example = "4")
        long openGames,
        @Schema(description = "Number of equipment offers", example = "8")
        long equipmentOffers
) {
}
