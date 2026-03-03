package pitchmarketplace.repository;

import pitchmarketplace.domain.entity.Pitch;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class PitchRepository {

    private final Map<Long, Pitch> storage = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Pitch> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Pitch> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Pitch> findByDistrict(String district) {
        return storage.values().stream()
                .filter(pitch -> pitch.getDistrict().equalsIgnoreCase(district))
                .toList();
    }

    public Pitch save(Pitch pitch) {
        if (pitch.getId() == null) {
            pitch.setId(idGenerator.getAndIncrement());
        }
        storage.put(pitch.getId(), pitch);
        return pitch;
    }

    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }
}
