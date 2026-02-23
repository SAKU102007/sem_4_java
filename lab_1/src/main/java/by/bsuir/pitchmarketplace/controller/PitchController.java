package by.bsuir.pitchmarketplace.controller;

import by.bsuir.pitchmarketplace.dto.PitchDetailsResponse;
import by.bsuir.pitchmarketplace.dto.PitchSearchRequest;
import by.bsuir.pitchmarketplace.dto.PitchSearchResponse;
import by.bsuir.pitchmarketplace.domain.enums.PitchType;
import by.bsuir.pitchmarketplace.service.PitchService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pitches")
public class PitchController {

    private final PitchService pitchService;

    public PitchController(PitchService pitchService) {
        this.pitchService = pitchService;
    }

    @GetMapping("/search")
    public List<PitchSearchResponse> search(
            @RequestParam(required = false) PitchType pitchType,
            @RequestParam(required = false) BigDecimal priceFrom,
            @RequestParam(required = false) BigDecimal priceTo,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) Integer skillMin,
            @RequestParam(required = false) Integer skillMax,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String metro,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desiredStartAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desiredEndAt,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "false") Boolean needInventory,
            @RequestParam(required = false, defaultValue = "0") Integer ballQty,
            @RequestParam(required = false, defaultValue = "0") Integer bibsQty,
            @RequestHeader(name = "X-User-Id", required = false) Long requesterUserId
    ) {
        PitchSearchRequest request = new PitchSearchRequest();
        request.setPitchType(pitchType);
        request.setPriceFrom(priceFrom);
        request.setPriceTo(priceTo);
        request.setDurationMinutes(durationMinutes);
        request.setSkillMin(skillMin);
        request.setSkillMax(skillMax);
        request.setDistrict(district);
        request.setMetro(metro);
        request.setLat(lat);
        request.setLng(lng);
        request.setRadiusKm(radiusKm);
        request.setDesiredStartAt(desiredStartAt);
        request.setDesiredEndAt(desiredEndAt);
        request.setSort(sort);
        request.setNeedInventory(needInventory);
        request.setBallQty(ballQty);
        request.setBibsQty(bibsQty);
        return pitchService.search(request, requesterUserId);
    }

    @GetMapping("/{id}")
    public PitchDetailsResponse getById(@PathVariable Long id) {
        return pitchService.getById(id);
    }
}
