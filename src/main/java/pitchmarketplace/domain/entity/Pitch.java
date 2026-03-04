package pitchmarketplace.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import pitchmarketplace.domain.enums.PitchType;

@Entity
@Table(name = "pitches")
public class Pitch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private PitchType type;

    private String district;

    private String metro;

    private BigDecimal pricePerHour;

    @OneToMany(mappedBy = "pitch", fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(
            mappedBy = "pitch",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private Set<EquipmentOffer> equipmentOffers = new HashSet<>();

    public Pitch() {
    }

    public Pitch(Long id, String name, PitchType type, String district, String metro, BigDecimal pricePerHour) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.district = district;
        this.metro = metro;
        this.pricePerHour = pricePerHour;
    }

    public void addEquipmentOffer(EquipmentOffer equipmentOffer) {
        equipmentOffers.add(equipmentOffer);
        equipmentOffer.setPitch(this);
    }

    public void removeEquipmentOffer(EquipmentOffer equipmentOffer) {
        equipmentOffers.remove(equipmentOffer);
        equipmentOffer.setPitch(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PitchType getType() {
        return type;
    }

    public void setType(PitchType type) {
        this.type = type;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMetro() {
        return metro;
    }

    public void setMetro(String metro) {
        this.metro = metro;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    public Set<EquipmentOffer> getEquipmentOffers() {
        return equipmentOffers;
    }

    public void setEquipmentOffers(Set<EquipmentOffer> equipmentOffers) {
        this.equipmentOffers = equipmentOffers;
    }
}
