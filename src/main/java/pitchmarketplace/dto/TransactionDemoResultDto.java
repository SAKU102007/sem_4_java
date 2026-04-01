package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of the transaction behaviour demo")
public record TransactionDemoResultDto(
        @Schema(description = "Demo mode", example = "with_transaction")
        String mode,
        @Schema(description = "Captured error message, if any", example = "Simulated transaction failure")
        String error,
        @Schema(description = "Entity counters before the demo action")
        EntityCountSnapshotDto before,
        @Schema(description = "Entity counters after the demo action")
        EntityCountSnapshotDto after
) {
}
