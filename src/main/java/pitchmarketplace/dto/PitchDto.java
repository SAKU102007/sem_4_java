package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import pitchmarketplace.domain.enums.PitchType;

@Schema(description = "Pitch response payload")
public record PitchDto(
        @Schema(description = "Pitch identifier", example = "11")
        Long id,
        @Schema(description = "Pitch display name", example = "Arena Central")
        String name,
        @Schema(description = "Pitch type")
        PitchType type,
        @Schema(description = "District where the pitch is located", example = "Central")
        String district,
        @Schema(description = "Nearest metro station", example = "Nemiga")
        String metro,
        @Schema(description = "Hourly rental price", example = "120.00")
        BigDecimal pricePerHour
) {
}
