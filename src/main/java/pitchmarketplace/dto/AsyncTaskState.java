package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle state of an asynchronous task")
public enum AsyncTaskState {
    ACCEPTED,
    RUNNING,
    COMPLETED,
    FAILED
}
