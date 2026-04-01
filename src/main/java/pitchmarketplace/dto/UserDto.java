package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pitchmarketplace.domain.enums.UserRole;

@Schema(description = "User response payload")
public record UserDto(
        @Schema(description = "User identifier", example = "7")
        Long id,
        @Schema(description = "User display name", example = "Алексей")
        String name,
        @Schema(description = "Player rating from 0 to 100", example = "78")
        Integer rating,
        @Schema(description = "User role")
        UserRole role
) {
}
