package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of the bulk booking transaction demo")
public record BulkBookingTransactionDemoResultDto(
        @Schema(description = "Demo mode", example = "bulk_with_transaction")
        String mode,
        @Schema(description = "Number of booking requests submitted", example = "3")
        int requestedItems,
        @Schema(description = "Captured error message, if any", example = "User not found. id=999999")
        String error,
        @Schema(description = "Entity counters before the demo action")
        EntityCountSnapshotDto before,
        @Schema(description = "Entity counters after the demo action")
        EntityCountSnapshotDto after
) {
}
