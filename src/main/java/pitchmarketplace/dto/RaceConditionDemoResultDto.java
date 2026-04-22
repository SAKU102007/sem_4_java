package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of unsafe and safe counter comparison under concurrency")
public record RaceConditionDemoResultDto(
        @Schema(description = "Number of concurrent threads used", example = "64")
        int threads,
        @Schema(description = "Increment operations per thread", example = "2000")
        int incrementsPerThread,
        @Schema(description = "Expected final counter value", example = "128000")
        long expected,
        @Schema(description = "Result of the unsafe counter", example = "104532")
        long unsafeCounter,
        @Schema(description = "Result of the safe counter", example = "128000")
        long safeCounter,
        @Schema(description = "How many increments were lost by the unsafe counter", example = "23468")
        long unsafeLostUpdates
) {
}
