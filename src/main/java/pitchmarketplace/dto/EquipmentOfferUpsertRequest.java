package pitchmarketplace.dto;

import java.math.BigDecimal;
import pitchmarketplace.domain.enums.EquipmentItemType;

public record EquipmentOfferUpsertRequest(
        Long pitchId,
        EquipmentItemType itemType,
        Integer stockTotal,
        BigDecimal rentFixedPrice
) {
}
