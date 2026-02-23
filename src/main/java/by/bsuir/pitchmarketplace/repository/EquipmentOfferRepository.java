package by.bsuir.pitchmarketplace.repository;

import by.bsuir.pitchmarketplace.domain.entity.EquipmentOffer;
import by.bsuir.pitchmarketplace.domain.enums.EquipmentItemType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentOfferRepository extends JpaRepository<EquipmentOffer, Long> {

    List<EquipmentOffer> findByPitchId(Long pitchId);

    Optional<EquipmentOffer> findByPitchIdAndItemType(Long pitchId, EquipmentItemType itemType);
}
