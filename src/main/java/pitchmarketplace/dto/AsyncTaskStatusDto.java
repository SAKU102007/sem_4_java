package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Current state and result of an asynchronous task")
public record AsyncTaskStatusDto(
        @Schema(description = "Task identifier", example = "1")
        long taskId,
        @Schema(description = "Current task state")
        AsyncTaskState status,
        @Schema(description = "Task creation timestamp")
        Instant createdAt,
        @Schema(description = "Task start timestamp")
        Instant startedAt,
        @Schema(description = "Task finish timestamp")
        Instant finishedAt,
        @Schema(description = "Error message when task failed", example = "Pitch report generation failed")
        String error,
        @Schema(description = "Calculated report when task completed successfully")
        PitchLoadReportResultDto result
) {
}
