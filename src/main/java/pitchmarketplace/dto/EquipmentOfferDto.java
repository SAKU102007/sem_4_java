package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import pitchmarketplace.domain.enums.EquipmentItemType;

@Schema(description = "Equipment offer response payload")
public record EquipmentOfferDto(
        @Schema(description = "Equipment offer identifier", example = "15")
        Long id,
        @Schema(description = "Related pitch identifier", example = "11")
        Long pitchId,
        @Schema(description = "Type of rentable equipment")
        EquipmentItemType itemType,
        @Schema(description = "Available stock for rent", example = "12")
        Integer stockTotal,
        @Schema(description = "Fixed rental price", example = "9.99")
        BigDecimal rentFixedPrice
) {
}
