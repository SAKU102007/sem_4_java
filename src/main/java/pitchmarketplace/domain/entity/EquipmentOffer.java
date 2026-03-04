package pitchmarketplace.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import pitchmarketplace.domain.enums.EquipmentItemType;

@Entity
@Table(name = "equipment_offers")
public class EquipmentOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pitch_id", nullable = false)
    private Pitch pitch;

    @Enumerated(EnumType.STRING)
    private EquipmentItemType itemType;

    private Integer stockTotal;

    private BigDecimal rentFixedPrice;

    public EquipmentOffer() {
    }

    public EquipmentOffer(Long id, Pitch pitch, EquipmentItemType itemType, Integer stockTotal, BigDecimal rentFixedPrice) {
        this.id = id;
        this.pitch = pitch;
        this.itemType = itemType;
        this.stockTotal = stockTotal;
        this.rentFixedPrice = rentFixedPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pitch getPitch() {
        return pitch;
    }

    public void setPitch(Pitch pitch) {
        this.pitch = pitch;
    }

    public EquipmentItemType getItemType() {
        return itemType;
    }

    public void setItemType(EquipmentItemType itemType) {
        this.itemType = itemType;
    }

    public Integer getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(Integer stockTotal) {
        this.stockTotal = stockTotal;
    }

    public BigDecimal getRentFixedPrice() {
        return rentFixedPrice;
    }

    public void setRentFixedPrice(BigDecimal rentFixedPrice) {
        this.rentFixedPrice = rentFixedPrice;
    }
}
