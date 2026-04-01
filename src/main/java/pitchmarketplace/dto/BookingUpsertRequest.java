package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import pitchmarketplace.domain.enums.BookingStatus;

@Schema(description = "Payload for creating or updating a booking")
public record BookingUpsertRequest(
        @NotNull(message = "pitchId is required")
        @Positive(message = "pitchId must be positive")
        @Schema(description = "Pitch identifier", example = "11")
        Long pitchId,
        @NotNull(message = "organizerId is required")
        @Positive(message = "organizerId must be positive")
        @Schema(description = "Organizer identifier", example = "7")
        Long organizerId,
        @NotNull(message = "startAt is required")
        @Schema(description = "Booking start timestamp", example = "2026-05-01T18:00:00")
        LocalDateTime startAt,
        @NotNull(message = "endAt is required")
        @Schema(description = "Booking end timestamp", example = "2026-05-01T20:00:00")
        LocalDateTime endAt,
        @NotNull(message = "status is required")
        @Schema(description = "Booking status")
        BookingStatus status
) {

    @AssertTrue(message = "endAt must be after startAt")
    public boolean isTimeRangeValid() {
        if (startAt == null || endAt == null) {
            return true;
        }
        return endAt.isAfter(startAt);
    }
}
