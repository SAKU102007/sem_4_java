package pitchmarketplace.dto;

import java.math.BigDecimal;
import pitchmarketplace.domain.enums.PitchType;

public record PitchUpsertRequest(
        String name,
        PitchType type,
        String district,
        String metro,
        BigDecimal pricePerHour
) {
}
