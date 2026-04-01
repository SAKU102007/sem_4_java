package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pitchmarketplace.domain.enums.UserRole;

@Schema(description = "Payload for creating or updating a user")
public record UserUpsertRequest(
        @NotBlank(message = "name is required")
        @Schema(description = "User display name", example = "Алексей")
        String name,
        @NotNull(message = "rating is required")
        @Min(value = 0, message = "rating must be at least 0")
        @Max(value = 100, message = "rating must be at most 100")
        @Schema(description = "Player rating from 0 to 100", example = "78")
        Integer rating,
        @NotNull(message = "role is required")
        @Schema(description = "Role assigned to the user")
        UserRole role
) {
}
