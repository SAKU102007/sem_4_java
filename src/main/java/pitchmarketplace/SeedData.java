package pitchmarketplace;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.EquipmentOffer;
import pitchmarketplace.domain.entity.OpenGame;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.EquipmentItemType;
import pitchmarketplace.domain.enums.OpenGameStatus;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.PitchRepository;
import pitchmarketplace.repository.UserRepository;

@Component
public class SeedData implements CommandLineRunner {

    private static final List<PitchSeedName> CLEAN_PITCH_NAMES = List.of(
            new PitchSeedName("Дворец футбола", "Центральный", "Немига"),
            new PitchSeedName("Минск-Арена Футбол", "Центральный", "Спортивная"),
            new PitchSeedName("Сокол Арена", "Октябрьский", "Ковальская Слобода"),
            new PitchSeedName("Уручье Парк", "Первомайский", "Уручье"),
            new PitchSeedName("Чижовка Футбол", "Заводской", "Автозаводская"),
            new PitchSeedName("Веснянка Спорт", "Центральный", "Молодежная"),
            new PitchSeedName("Лошица Арена", "Ленинский", "Пролетарская"),
            new PitchSeedName("Комаровка Футзал", "Советский", "Площадь Якуба Коласа")
    );

    private static final List<String> CLEAN_USER_NAMES = List.of(
            "Алексей",
            "Максим",
            "Илья",
            "Денис",
            "Егор",
            "Артем",
            "Павел",
            "Никита"
    );

    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;
    private final BookingRepository bookingRepository;
    private final OpenGameRepository openGameRepository;

    public SeedData(
            UserRepository userRepository,
            PitchRepository pitchRepository,
            BookingRepository bookingRepository,
            OpenGameRepository openGameRepository
    ) {
        this.userRepository = userRepository;
        this.pitchRepository = pitchRepository;
        this.bookingRepository = bookingRepository;
        this.openGameRepository = openGameRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!isDatabaseEmpty()) {
            normalizeExistingDemoData();
            return;
        }

        User alexey = userRepository.save(new User(null, "Алексей", 68, UserRole.PLAYER));
        User maxim = userRepository.save(new User(null, "Максим", 70, UserRole.PLAYER));
        User ilya = userRepository.save(new User(null, "Илья", 72, UserRole.PLAYER));
        User denis = userRepository.save(new User(null, "Денис", 64, UserRole.PLAYER));
        User egor = userRepository.save(new User(null, "Егор", 59, UserRole.PLAYER));
        User artem = userRepository.save(new User(null, "Артем", 75, UserRole.PLAYER));
        User pavel = userRepository.save(new User(null, "Павел", 66, UserRole.PLAYER));
        User nikita = userRepository.save(new User(null, "Никита", 81, UserRole.PLAYER));
        User kirill = userRepository.save(new User(null, "Кирилл", 54, UserRole.VENUE_OWNER));
        User andrey = userRepository.save(new User(null, "Андрей", 62, UserRole.ADMIN));

        Pitch pitchOne = pitchRepository.save(createPitch(
                "Дворец футбола",
                PitchType.FIVE_FUTSAL,
                "Центральный",
                "Немига",
                "120.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 10, "15.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 20, "8.00")
        ));
        Pitch pitchTwo = pitchRepository.save(createPitch(
                "Минск-Арена Футбол",
                PitchType.EIGHT,
                "Центральный",
                "Спортивная",
                "150.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 6, "12.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 14, "7.00")
        ));
        Pitch pitchThree = pitchRepository.save(createPitch(
                "Сокол Арена",
                PitchType.ELEVEN,
                "Октябрьский",
                "Ковальская Слобода",
                "200.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 12, "17.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 24, "11.00")
        ));
        Pitch pitchFour = pitchRepository.save(createPitch(
                "Уручье Парк",
                PitchType.FIVE_FUTSAL,
                "Первомайский",
                "Уручье",
                "110.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 8, "13.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 16, "8.00")
        ));
        Pitch pitchFive = pitchRepository.save(createPitch(
                "Чижовка Футбол",
                PitchType.FIVE_TURF,
                "Заводской",
                "Автозаводская",
                "95.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 7, "10.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 12, "6.00")
        ));
        Pitch pitchSix = pitchRepository.save(createPitch(
                "Веснянка Спорт",
                PitchType.EIGHT,
                "Центральный",
                "Молодежная",
                "145.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 9, "14.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 18, "9.00")
        ));
        Pitch pitchSeven = pitchRepository.save(createPitch(
                "Лошица Арена",
                PitchType.EIGHT,
                "Ленинский",
                "Пролетарская",
                "130.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 8, "12.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 16, "8.00")
        ));
        Pitch pitchEight = pitchRepository.save(createPitch(
                "Комаровка Футзал",
                PitchType.FIVE_FUTSAL,
                "Советский",
                "Площадь Якуба Коласа",
                "115.00",
                new EquipmentSeedSpec(EquipmentItemType.BALL, 7, "11.00"),
                new EquipmentSeedSpec(EquipmentItemType.BIBS, 14, "7.00")
        ));

        LocalDateTime baseStart = LocalDateTime.of(2026, 3, 10, 18, 0);

        Booking bookingOne = bookingRepository.save(createBooking(
                pitchOne,
                alexey,
                baseStart,
                BookingStatus.CONFIRMED
        ));
        Booking bookingTwo = bookingRepository.save(createBooking(
                pitchTwo,
                maxim,
                baseStart.plusHours(1),
                BookingStatus.CONFIRMED
        ));
        Booking bookingThree = bookingRepository.save(createBooking(
                pitchThree,
                alexey,
                baseStart.plusDays(1),
                BookingStatus.CONFIRMED
        ));
        Booking bookingFour = bookingRepository.save(createBooking(
                pitchFour,
                maxim,
                baseStart.plusDays(1).plusHours(1),
                BookingStatus.CREATED
        ));
        Booking bookingFive = bookingRepository.save(createBooking(
                pitchFive,
                alexey,
                baseStart.plusDays(2),
                BookingStatus.CONFIRMED
        ));
        Booking bookingSix = bookingRepository.save(createBooking(
                pitchSix,
                maxim,
                baseStart.plusDays(3),
                BookingStatus.CANCELLED
        ));
        Booking bookingSeven = bookingRepository.save(createBooking(
                pitchSeven,
                ilya,
                baseStart.plusDays(4),
                BookingStatus.CONFIRMED
        ));
        Booking bookingEight = bookingRepository.save(createBooking(
                pitchEight,
                denis,
                baseStart.plusDays(5),
                BookingStatus.CREATED
        ));

        openGameRepository.save(createOpenGame(
                bookingOne,
                alexey,
                50,
                80,
                10,
                OpenGameStatus.OPEN,
                alexey,
                ilya,
                denis,
                pavel
        ));
        openGameRepository.save(createOpenGame(
                bookingTwo,
                maxim,
                55,
                85,
                12,
                OpenGameStatus.OPEN,
                maxim,
                artem,
                egor
        ));
        openGameRepository.save(createOpenGame(
                bookingThree,
                alexey,
                60,
                90,
                14,
                OpenGameStatus.FULL,
                alexey,
                nikita,
                kirill,
                andrey
        ));
        openGameRepository.save(createOpenGame(
                bookingFour,
                maxim,
                45,
                75,
                10,
                OpenGameStatus.OPEN,
                maxim,
                ilya,
                egor,
                pavel
        ));
        openGameRepository.save(createOpenGame(
                bookingFive,
                alexey,
                40,
                70,
                8,
                OpenGameStatus.CANCELLED,
                alexey,
                denis
        ));
        openGameRepository.save(createOpenGame(
                bookingSix,
                maxim,
                50,
                88,
                12,
                OpenGameStatus.OPEN,
                maxim,
                artem,
                andrey,
                kirill
        ));
        openGameRepository.save(createOpenGame(
                bookingSeven,
                ilya,
                45,
                78,
                12,
                OpenGameStatus.OPEN,
                ilya,
                alexey,
                pavel
        ));
        openGameRepository.save(createOpenGame(
                bookingEight,
                denis,
                50,
                82,
                10,
                OpenGameStatus.OPEN,
                denis,
                maxim,
                artem
        ));
    }

    private boolean isDatabaseEmpty() {
        return userRepository.count() == 0
                && pitchRepository.count() == 0
                && bookingRepository.count() == 0
                && openGameRepository.count() == 0;
    }

    private void normalizeExistingDemoData() {
        List<Pitch> pitches = pitchRepository.findAll();
        for (int i = 0; i < pitches.size(); i++) {
            Pitch pitch = pitches.get(i);
            if (isGeneratedPitchName(pitch.getName())) {
                PitchSeedName cleanName = CLEAN_PITCH_NAMES.get(i % CLEAN_PITCH_NAMES.size());
                pitch.setName(cleanName.name());
                pitch.setDistrict(cleanName.district());
                pitch.setMetro(cleanName.metro());
            }
        }

        List<User> users = userRepository.findAll();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (isGeneratedUserName(user.getName())) {
                user.setName(CLEAN_USER_NAMES.get(i % CLEAN_USER_NAMES.size()));
            }
        }
    }

    private boolean isGeneratedPitchName(String name) {
        return name.startsWith("Tx Pitch ")
                || name.startsWith("Bulk Tx Pitch ")
                || name.startsWith("Cascade Demo Pitch")
                || name.matches(".*\\d{6,}.*");
    }

    private boolean isGeneratedUserName(String name) {
        return name.startsWith("Tx Organizer ")
                || name.matches(".*\\d{6,}.*");
    }

    private Pitch createPitch(
            String name,
            PitchType type,
            String district,
            String metro,
            String pricePerHour,
            EquipmentSeedSpec firstOffer,
            EquipmentSeedSpec secondOffer
    ) {
        Pitch pitch = new Pitch(null, name, type, district, metro, new BigDecimal(pricePerHour));
        pitch.addEquipmentOffer(createEquipmentOffer(firstOffer));
        pitch.addEquipmentOffer(createEquipmentOffer(secondOffer));
        return pitch;
    }

    private EquipmentOffer createEquipmentOffer(EquipmentSeedSpec spec) {
        return new EquipmentOffer(
                null,
                null,
                spec.itemType(),
                spec.stockTotal(),
                new BigDecimal(spec.rentPrice())
        );
    }

    private Booking createBooking(Pitch pitch, User organizer, LocalDateTime startAt, BookingStatus status) {
        return new Booking(
                null,
                pitch,
                organizer,
                startAt,
                startAt.plusHours(2),
                status
        );
    }

    private OpenGame createOpenGame(
            Booking booking,
            User organizer,
            int targetSkillMin,
            int targetSkillMax,
            int maxPlayers,
            OpenGameStatus status,
            User... participants
    ) {
        OpenGame openGame = new OpenGame(
                null,
                booking,
                organizer,
                targetSkillMin,
                targetSkillMax,
                maxPlayers,
                status
        );
        for (User participant : participants) {
            openGame.addParticipant(participant);
        }
        return openGame;
    }

    private record EquipmentSeedSpec(
            EquipmentItemType itemType,
            int stockTotal,
            String rentPrice
    ) {
    }

    private record PitchSeedName(
            String name,
            String district,
            String metro
    ) {
    }
}
