package pitchmarketplace.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.EquipmentOffer;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.EquipmentItemType;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.dto.EntityCountSnapshotDto;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.EquipmentOfferRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.PitchRepository;
import pitchmarketplace.repository.UserRepository;

@Service
public class TransactionDemoService {

    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;
    private final BookingRepository bookingRepository;
    private final OpenGameRepository openGameRepository;
    private final EquipmentOfferRepository equipmentOfferRepository;

    public TransactionDemoService(
            UserRepository userRepository,
            PitchRepository pitchRepository,
            BookingRepository bookingRepository,
            OpenGameRepository openGameRepository,
            EquipmentOfferRepository equipmentOfferRepository
    ) {
        this.userRepository = userRepository;
        this.pitchRepository = pitchRepository;
        this.bookingRepository = bookingRepository;
        this.openGameRepository = openGameRepository;
        this.equipmentOfferRepository = equipmentOfferRepository;
    }

    public EntityCountSnapshotDto snapshot() {
        return new EntityCountSnapshotDto(
                userRepository.count(),
                pitchRepository.count(),
                bookingRepository.count(),
                openGameRepository.count(),
                equipmentOfferRepository.count()
        );
    }

    public void saveRelatedEntitiesWithoutTransactionAndFail() {
        saveRelatedEntitiesAndFail();
    }

    @Transactional
    public void saveRelatedEntitiesWithTransactionAndFail() {
        saveRelatedEntitiesAndFail();
    }

    private void saveRelatedEntitiesAndFail() {
        String suffix = String.valueOf(System.nanoTime());

        User organizer = userRepository.save(new User(null, "Tx Organizer " + suffix, 60, UserRole.PLAYER));

        Pitch pitch = new Pitch(
                null,
                "Tx Pitch " + suffix,
                PitchType.FIVE_FUTSAL,
                "Центральный",
                "Немига",
                new BigDecimal("140.00")
        );
        pitch.addEquipmentOffer(new EquipmentOffer(null, null, EquipmentItemType.BALL, 8, new BigDecimal("12.00")));
        pitch.addEquipmentOffer(new EquipmentOffer(null, null, EquipmentItemType.BIBS, 16, new BigDecimal("9.00")));
        Pitch savedPitch = pitchRepository.save(pitch);

        bookingRepository.save(new Booking(
                null,
                savedPitch,
                organizer,
                LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(1).withHour(21).withMinute(0).withSecond(0).withNano(0),
                BookingStatus.CREATED
        ));

        throw new IllegalStateException("Synthetic failure after partial save of related entities");
    }
}
