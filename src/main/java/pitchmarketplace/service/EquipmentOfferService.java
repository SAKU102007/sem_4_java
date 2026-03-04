package pitchmarketplace.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.EquipmentOffer;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.dto.EquipmentOfferDto;
import pitchmarketplace.dto.EquipmentOfferUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.EquipmentOfferRepository;
import pitchmarketplace.repository.PitchRepository;

@Service
@Transactional
public class EquipmentOfferService {

    private final EquipmentOfferRepository equipmentOfferRepository;
    private final PitchRepository pitchRepository;

    public EquipmentOfferService(EquipmentOfferRepository equipmentOfferRepository, PitchRepository pitchRepository) {
        this.equipmentOfferRepository = equipmentOfferRepository;
        this.pitchRepository = pitchRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipmentOfferDto> findAll() {
        return equipmentOfferRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentOfferDto findById(Long id) {
        return toDto(getOfferOrThrow(id));
    }

    public EquipmentOfferDto create(EquipmentOfferUpsertRequest request) {
        EquipmentOffer offer = new EquipmentOffer();
        applyRequest(offer, request);
        return toDto(equipmentOfferRepository.save(offer));
    }

    public EquipmentOfferDto update(Long id, EquipmentOfferUpsertRequest request) {
        EquipmentOffer offer = getOfferOrThrow(id);
        applyRequest(offer, request);
        return toDto(equipmentOfferRepository.save(offer));
    }

    public void delete(Long id) {
        EquipmentOffer offer = getOfferOrThrow(id);
        equipmentOfferRepository.delete(offer);
    }

    private void applyRequest(EquipmentOffer offer, EquipmentOfferUpsertRequest request) {
        Pitch pitch = pitchRepository.findById(request.pitchId())
                .orElseThrow(() -> new ResourceNotFoundException("Pitch not found. id=" + request.pitchId()));
        offer.setPitch(pitch);
        offer.setItemType(request.itemType());
        offer.setStockTotal(request.stockTotal());
        offer.setRentFixedPrice(request.rentFixedPrice());
    }

    private EquipmentOffer getOfferOrThrow(Long id) {
        return equipmentOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment offer not found. id=" + id));
    }

    private EquipmentOfferDto toDto(EquipmentOffer offer) {
        return new EquipmentOfferDto(
                offer.getId(),
                offer.getPitch().getId(),
                offer.getItemType(),
                offer.getStockTotal(),
                offer.getRentFixedPrice()
        );
    }
}
