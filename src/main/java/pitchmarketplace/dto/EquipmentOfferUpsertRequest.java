package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import pitchmarketplace.domain.enums.EquipmentItemType;

@Schema(description = "Payload for creating or updating an equipment offer")
public record EquipmentOfferUpsertRequest(
        @NotNull(message = "pitchId is required")
        @Positive(message = "pitchId must be positive")
        @Schema(description = "Pitch identifier", example = "11")
        Long pitchId,
        @NotNull(message = "itemType is required")
        @Schema(description = "Type of rentable equipment")
        EquipmentItemType itemType,
        @NotNull(message = "stockTotal is required")
        @PositiveOrZero(message = "stockTotal must be zero or positive")
        @Schema(description = "Available stock", example = "12")
        Integer stockTotal,
        @NotNull(message = "rentFixedPrice is required")
        @DecimalMin(value = "0.01", message = "rentFixedPrice must be greater than zero")
        @Schema(description = "Fixed rental price", example = "9.99")
        BigDecimal rentFixedPrice
) {
}
