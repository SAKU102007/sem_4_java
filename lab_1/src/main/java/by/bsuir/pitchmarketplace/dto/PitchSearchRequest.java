package by.bsuir.pitchmarketplace.dto;

import by.bsuir.pitchmarketplace.domain.enums.PitchType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class PitchSearchRequest {

    private PitchType pitchType;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private Integer durationMinutes;
    private Integer skillMin;
    private Integer skillMax;
    private String district;
    private String metro;
    private Double lat;
    private Double lng;
    private Double radiusKm;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime desiredStartAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime desiredEndAt;

    private String sort;
    private Boolean needInventory = false;
    private Integer ballQty = 0;
    private Integer bibsQty = 0;

    public PitchType getPitchType() {
        return pitchType;
    }

    public void setPitchType(PitchType pitchType) {
        this.pitchType = pitchType;
    }

    public BigDecimal getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(BigDecimal priceFrom) {
        this.priceFrom = priceFrom;
    }

    public BigDecimal getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(BigDecimal priceTo) {
        this.priceTo = priceTo;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getSkillMin() {
        return skillMin;
    }

    public void setSkillMin(Integer skillMin) {
        this.skillMin = skillMin;
    }

    public Integer getSkillMax() {
        return skillMax;
    }

    public void setSkillMax(Integer skillMax) {
        this.skillMax = skillMax;
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public LocalDateTime getDesiredStartAt() {
        return desiredStartAt;
    }

    public void setDesiredStartAt(LocalDateTime desiredStartAt) {
        this.desiredStartAt = desiredStartAt;
    }

    public LocalDateTime getDesiredEndAt() {
        return desiredEndAt;
    }

    public void setDesiredEndAt(LocalDateTime desiredEndAt) {
        this.desiredEndAt = desiredEndAt;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Boolean getNeedInventory() {
        return needInventory;
    }

    public void setNeedInventory(Boolean needInventory) {
        this.needInventory = needInventory;
    }

    public Integer getBallQty() {
        return ballQty;
    }

    public void setBallQty(Integer ballQty) {
        this.ballQty = ballQty;
    }

    public Integer getBibsQty() {
        return bibsQty;
    }

    public void setBibsQty(Integer bibsQty) {
        this.bibsQty = bibsQty;
    }
}
