package pitchmarketplace.dto;

import java.time.LocalDateTime;
import java.util.Locale;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;

public record BookingSearchCriteria(
        String district,
        PitchType pitchType,
        String organizerName,
        BookingStatus status,
        LocalDateTime startFrom,
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
