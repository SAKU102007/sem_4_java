package pitchmarketplace.dto;

import pitchmarketplace.domain.enums.PitchType;
import java.math.BigDecimal;

public record PitchDto(
        Long id,
        String name,
        PitchType type,
        String district,
        String metro,
        BigDecimal pricePerHour
) {
}
