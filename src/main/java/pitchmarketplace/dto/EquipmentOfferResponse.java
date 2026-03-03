package pitchmarketplace.dto;

import pitchmarketplace.domain.enums.EquipmentItemType;
import java.math.BigDecimal;

public record EquipmentOfferResponse(
        EquipmentItemType itemType,
        Integer stockTotal,
        BigDecimal rentFixedPrice
) {
}
