package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Snapshot of thread-safe concurrency counters")
public record ConcurrencyCounterStatsDto(
        @Schema(description = "Last issued asynchronous task id", example = "5")
        long lastIssuedTaskId,
        @Schema(description = "Total submitted asynchronous tasks", example = "5")
        long submittedTasks,
        @Schema(description = "Successfully completed asynchronous tasks", example = "4")
        long completedTasks,
        @Schema(description = "Failed asynchronous tasks", example = "1")
        long failedTasks
) {
}
