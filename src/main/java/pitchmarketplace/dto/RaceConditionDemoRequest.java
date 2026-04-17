package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Parameters for race condition demonstration")
public record RaceConditionDemoRequest(
        @NotNull(message = "threads is required")
        @Min(value = 50, message = "threads must be at least 50")
        @Schema(description = "Number of concurrent threads", example = "64")
        Integer threads,
        @NotNull(message = "incrementsPerThread is required")
        @Min(value = 100, message = "incrementsPerThread must be at least 100")
        @Schema(description = "Increment operations per thread", example = "2000")
        Integer incrementsPerThread
) {
}
