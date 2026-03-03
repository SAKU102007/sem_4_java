package pitchmarketplace.dto;

import pitchmarketplace.domain.enums.PitchType;
import java.math.BigDecimal;
import java.util.List;

public record PitchDetailsResponse(
        Long pitchId,
        Long venueId,
        String venueName,
        String venueAddress,
        PitchType pitchType,
        String surface,
        BigDecimal pricePerHour,
        String district,
        String metro,
        Double lat,
        Double lng,
        Double averageSkill,
        List<EquipmentOfferResponse> equipmentOffers
) {
}
