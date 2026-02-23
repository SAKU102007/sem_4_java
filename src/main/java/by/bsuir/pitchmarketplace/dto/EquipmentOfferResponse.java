package by.bsuir.pitchmarketplace.dto;

import by.bsuir.pitchmarketplace.domain.enums.EquipmentItemType;
import java.math.BigDecimal;

public record EquipmentOfferResponse(
        EquipmentItemType itemType,
        Integer stockTotal,
        BigDecimal rentFixedPrice
) {
}
