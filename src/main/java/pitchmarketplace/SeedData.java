package pitchmarketplace;

import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.repository.PitchRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements CommandLineRunner {

    private final PitchRepository repository;

    public SeedData(PitchRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.clear();

        repository.save(new Pitch(null, "МСК Арена", PitchType.FIVE_FUTSAL, "Центральный", "Немига", new BigDecimal("120.00")));
        repository.save(new Pitch(null, "Олимпик Парк", PitchType.EIGHT, "Советский", "Московская", new BigDecimal("150.00")));
        repository.save(new Pitch(null, "Трактор Филд", PitchType.ELEVEN, "Партизанский", "Тракторный завод", new BigDecimal("200.00")));
        repository.save(new Pitch(null, "Футзал Хаб", PitchType.FIVE_FUTSAL, "Центральный", "Фрунзенская", new BigDecimal("110.00")));
        repository.save(new Pitch(null, "Парк Футбол", PitchType.FIVE_TURF, "Ленинский", "Пролетарская", new BigDecimal("95.00")));
    }
}
