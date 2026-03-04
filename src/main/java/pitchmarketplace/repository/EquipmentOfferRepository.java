package pitchmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pitchmarketplace.domain.entity.EquipmentOffer;

public interface EquipmentOfferRepository extends JpaRepository<EquipmentOffer, Long> {
}
