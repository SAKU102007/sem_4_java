package pitchmarketplace;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                "МСК Арена",
                PitchType.FIVE_FUTSAL,
                "Центральный",
                "Немига",
                "120.00",
                10,
                "15.00",
                20,
                "8.00"
        ));
        Pitch pitchTwo = pitchRepository.save(createPitch(
                "Олимпик Парк",
                PitchType.EIGHT,
                "Советский",
                "Московская",
                "150.00",
                6,
                "12.00",
                14,
                "7.00"
        ));
        Pitch pitchThree = pitchRepository.save(createPitch(
                "Трактор Филд",
                PitchType.ELEVEN,
                "Партизанский",
                "Тракторный завод",
                "200.00",
                12,
                "17.00",
                24,
                "11.00"
        ));
        Pitch pitchFour = pitchRepository.save(createPitch(
                "Футзал Хаб",
                PitchType.FIVE_FUTSAL,
                "Центральный",
                "Фрунзенская",
                "110.00",
                8,
                "13.00",
                16,
                "8.00"
        ));
        Pitch pitchFive = pitchRepository.save(createPitch(
                "Парк Футбол",
                PitchType.FIVE_TURF,
                "Ленинский",
                "Пролетарская",
                "95.00",
                7,
                "10.00",
                12,
                "6.00"
        ));
        Pitch pitchSix = pitchRepository.save(createPitch(
                "Лошицкий Центр",
                PitchType.EIGHT,
                "Октябрьский",
                "Институт культуры",
                "145.00",
                9,
                "14.00",
                18,
                "9.00"
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
    }

    private boolean isDatabaseEmpty() {
        return userRepository.count() == 0
                && pitchRepository.count() == 0
                && bookingRepository.count() == 0
                && openGameRepository.count() == 0;
    }

    private Pitch createPitch(
            String name,
            PitchType type,
            String district,
            String metro,
            String pricePerHour,
            int ballStock,
            String ballRentPrice,
            int bibsStock,
            String bibsRentPrice
    ) {
        Pitch pitch = new Pitch(null, name, type, district, metro, new BigDecimal(pricePerHour));
        pitch.addEquipmentOffer(new EquipmentOffer(
                null,
                null,
                EquipmentItemType.BALL,
                ballStock,
                new BigDecimal(ballRentPrice)
        ));
        pitch.addEquipmentOffer(new EquipmentOffer(
                null,
                null,
                EquipmentItemType.BIBS,
                bibsStock,
                new BigDecimal(bibsRentPrice)
        ));
        return pitch;
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
}
