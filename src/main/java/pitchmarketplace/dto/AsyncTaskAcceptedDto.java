package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Response returned after an asynchronous task is accepted")
public record AsyncTaskAcceptedDto(
        @Schema(description = "Generated task identifier", example = "1")
        long taskId,
        @Schema(description = "Initial task state")
        AsyncTaskState status,
        @Schema(description = "Task creation timestamp")
        Instant createdAt
) {
}
