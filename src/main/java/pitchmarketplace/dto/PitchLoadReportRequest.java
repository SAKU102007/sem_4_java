package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload for starting an asynchronous pitch load report")
public record PitchLoadReportRequest(
        @NotNull(message = "pitchId is required")
        @Positive(message = "pitchId must be positive")
        @Schema(description = "Pitch identifier", example = "1")
        Long pitchId
) {
}
