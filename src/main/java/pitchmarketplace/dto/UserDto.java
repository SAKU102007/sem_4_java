package pitchmarketplace.dto;

import pitchmarketplace.domain.enums.UserRole;

public record UserDto(
        Long id,
        String name,
        Integer rating,
        UserRole role
) {
}
