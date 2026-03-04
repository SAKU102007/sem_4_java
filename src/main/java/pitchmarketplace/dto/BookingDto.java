package pitchmarketplace.dto;

import java.time.LocalDateTime;
import pitchmarketplace.domain.enums.BookingStatus;

public record BookingDto(
        Long id,
        Long pitchId,
        Long organizerId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        BookingStatus status
) {
}
