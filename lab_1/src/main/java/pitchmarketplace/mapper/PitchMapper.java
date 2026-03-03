package pitchmarketplace.mapper;

import pitchmarketplace.domain.entity.EquipmentOffer;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.dto.EquipmentOfferResponse;
import pitchmarketplace.dto.InventoryAvailabilityResponse;
import pitchmarketplace.dto.PitchDetailsResponse;
import pitchmarketplace.dto.PitchSearchResponse;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PitchMapper {

    public PitchSearchResponse toSearchResponse(
            Pitch pitch,
            Double skillDistanceToMe,
            Double distanceKm,
            InventoryAvailabilityResponse inventory
    ) {
        return new PitchSearchResponse(
                pitch.getId(),
                pitch.getVenue().getId(),
                pitch.getVenue().getName(),
                pitch.getVenue().getAddress(),
                pitch.getType(),
                pitch.getSurface(),
                pitch.getPricePerHour(),
                pitch.getDistrict(),
                pitch.getMetro(),
                pitch.getLat(),
                pitch.getLng(),
                pitch.getAverageSkill(),
                skillDistanceToMe,
                distanceKm,
                inventory
        );
    }

    public PitchDetailsResponse toDetailsResponse(Pitch pitch) {
        List<EquipmentOfferResponse> equipment = pitch.getEquipmentOffers().stream()
                .sorted(Comparator.comparing(EquipmentOffer::getItemType))
                .map(this::toEquipmentOfferResponse)
                .toList();

        return new PitchDetailsResponse(
                pitch.getId(),
                pitch.getVenue().getId(),
                pitch.getVenue().getName(),
                pitch.getVenue().getAddress(),
                pitch.getType(),
                pitch.getSurface(),
                pitch.getPricePerHour(),
                pitch.getDistrict(),
                pitch.getMetro(),
                pitch.getLat(),
                pitch.getLng(),
                pitch.getAverageSkill(),
                equipment
        );
    }

    private EquipmentOfferResponse toEquipmentOfferResponse(EquipmentOffer offer) {
        return new EquipmentOfferResponse(
                offer.getItemType(),
                offer.getStockTotal(),
                offer.getRentFixedPrice()
        );
    }
}
