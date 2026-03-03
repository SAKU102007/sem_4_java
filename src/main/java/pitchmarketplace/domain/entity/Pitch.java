package pitchmarketplace.domain.entity;

import pitchmarketplace.domain.enums.PitchType;
import java.math.BigDecimal;

public class Pitch {

    private Long id;
    private String name;
    private PitchType type;
    private String district;
    private String metro;
    private BigDecimal pricePerHour;

    public Pitch(Long id, String name, PitchType type, String district, String metro, BigDecimal pricePerHour) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.district = district;
        this.metro = metro;
        this.pricePerHour = pricePerHour;
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
}
