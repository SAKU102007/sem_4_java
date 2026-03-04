package pitchmarketplace.dto;

import pitchmarketplace.domain.enums.UserRole;

public record UserUpsertRequest(
        String name,
        Integer rating,
        UserRole role
) {
}
