package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Unified error response returned by the API")
public record ApiErrorResponse(
        @Schema(description = "Timestamp when the error was generated", example = "2026-05-01T18:00:00Z")
        Instant timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP status text", example = "Bad Request")
        String error,
        @Schema(description = "High-level error message", example = "Validation failed")
        String message,
        @Schema(description = "Request path that produced the error", example = "/api/v1/pitches")
        String path,
        @Schema(description = "Detailed validation or processing errors")
        List<String> details
) {
}
