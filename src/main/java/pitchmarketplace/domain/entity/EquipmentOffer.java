package pitchmarketplace.domain.entity;

import pitchmarketplace.domain.enums.EquipmentItemType;
import jakarta.persistence.Column;
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

@Entity
@Table(name = "equipment_offers")
public class EquipmentOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pitch_id", nullable = false)
    private Pitch pitch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentItemType itemType;

    @Column(nullable = false)
    private Integer stockTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rentFixedPrice;

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
