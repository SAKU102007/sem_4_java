package pitchmarketplace.mapper;

import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.dto.PitchDto;
import org.springframework.stereotype.Component;

@Component
public class PitchMapper {

    public PitchDto toDto(Pitch pitch) {
        if (pitch == null) {
            return null;
        }
        return new PitchDto(
                pitch.getId(),
                pitch.getName(),
                pitch.getType(),
                pitch.getDistrict(),
                pitch.getMetro(),
                pitch.getPricePerHour()
        );
    }
}
