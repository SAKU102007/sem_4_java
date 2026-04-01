package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Locale;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;

@Schema(description = "Normalized internal criteria for booking search")
public record BookingSearchCriteria(
        @Schema(description = "Normalized district filter")
        String district,
        @Schema(description = "Pitch type filter")
        PitchType pitchType,
        @Schema(description = "Normalized organizer name filter")
        String organizerName,
        @Schema(description = "Booking status filter")
        BookingStatus status,
        @Schema(description = "Lower boundary for booking start time")
        LocalDateTime startFrom,
        @Schema(description = "Upper boundary for booking start time")
        LocalDateTime startTo
) {

    public BookingSearchCriteria {
        district = normalizeText(district);
        organizerName = normalizeText(organizerName);
    }

    private static String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
