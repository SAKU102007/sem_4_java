package by.bsuir.pitchmarketplace.dto;

import by.bsuir.pitchmarketplace.domain.enums.PitchType;
import java.math.BigDecimal;

public record PitchSearchResponse(
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
        Double skillDistanceToMe,
        Double distanceKm,
        InventoryAvailabilityResponse inventory
) {
}
