package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import pitchmarketplace.domain.enums.PitchType;

@Schema(description = "Payload for creating or updating a pitch")
public record PitchUpsertRequest(
        @NotBlank(message = "name is required")
        @Schema(description = "Pitch display name", example = "Arena Central")
        String name,
        @NotNull(message = "type is required")
        @Schema(description = "Pitch type")
        PitchType type,
        @NotBlank(message = "district is required")
        @Schema(description = "District where the pitch is located", example = "Central")
        String district,
        @NotBlank(message = "metro is required")
        @Schema(description = "Nearest metro station", example = "Nemiga")
        String metro,
        @NotNull(message = "pricePerHour is required")
        @DecimalMin(value = "0.01", message = "pricePerHour must be greater than zero")
        @Schema(description = "Hourly rental price", example = "120.00")
        BigDecimal pricePerHour
) {
}
