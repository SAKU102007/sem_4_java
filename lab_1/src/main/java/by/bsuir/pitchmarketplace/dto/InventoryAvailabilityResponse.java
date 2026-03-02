package by.bsuir.pitchmarketplace.dto;

import java.math.BigDecimal;

public record InventoryAvailabilityResponse(
        boolean hasRequestedEquipment,
        Integer availableBalls,
        Integer availableBibs,
        BigDecimal ballRentPrice,
        BigDecimal bibsRentPrice
) {
}
