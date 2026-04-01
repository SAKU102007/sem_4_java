package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import pitchmarketplace.domain.enums.BookingStatus;

@Schema(description = "Booking response payload")
public record BookingDto(
        @Schema(description = "Booking identifier", example = "101")
        Long id,
        @Schema(description = "Related pitch identifier", example = "11")
        Long pitchId,
        @Schema(description = "Organizer identifier", example = "7")
        Long organizerId,
        @Schema(description = "Booking start timestamp", example = "2026-05-01T18:00:00")
        LocalDateTime startAt,
        @Schema(description = "Booking end timestamp", example = "2026-05-01T20:00:00")
        LocalDateTime endAt,
        @Schema(description = "Booking status")
        BookingStatus status
) {
}
