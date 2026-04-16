package pitchmarketplace.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.EquipmentItemType;
import pitchmarketplace.domain.enums.OpenGameStatus;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingSearchRequest;
import pitchmarketplace.dto.BookingSearchResponseDto;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.dto.BulkBookingTransactionDemoResultDto;
import pitchmarketplace.dto.EntityCountSnapshotDto;
import pitchmarketplace.dto.EquipmentOfferDto;
import pitchmarketplace.dto.EquipmentOfferUpsertRequest;
import pitchmarketplace.dto.NPlusOneDemoResultDto;
import pitchmarketplace.dto.OpenGameDto;
import pitchmarketplace.dto.OpenGameUpsertRequest;
import pitchmarketplace.dto.PitchDto;
import pitchmarketplace.dto.PitchUpsertRequest;
import pitchmarketplace.dto.TransactionDemoResultDto;
import pitchmarketplace.dto.UserDto;
import pitchmarketplace.dto.UserUpsertRequest;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.EquipmentOfferRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.PitchRepository;
import pitchmarketplace.repository.UserRepository;
import pitchmarketplace.service.BookingSearchService;
import pitchmarketplace.service.BookingService;
import pitchmarketplace.service.EquipmentOfferService;
import pitchmarketplace.service.NPlusOneDemoService;
import pitchmarketplace.service.OpenGameService;
import pitchmarketplace.service.PitchService;
import pitchmarketplace.service.TransactionDemoService;
import pitchmarketplace.service.UserService;

class ControllerUnitTest {

    private StubBookingService bookingService;
    private StubBookingSearchService bookingSearchService;
    private StubNPlusOneDemoService nPlusOneDemoService;
    private StubTransactionDemoService transactionDemoService;
    private StubUserService userService;
    private StubPitchService pitchService;
    private StubEquipmentOfferService equipmentOfferService;
    private StubOpenGameService openGameService;

    private BookingController bookingController;
    private DemoController demoController;
    private UserController userController;
    private PitchController pitchController;
    private EquipmentOfferController equipmentOfferController;
    private OpenGameController openGameController;

    @BeforeEach
    void setUp() {
        bookingService = new StubBookingService();
        bookingSearchService = new StubBookingSearchService();
        nPlusOneDemoService = new StubNPlusOneDemoService();
        transactionDemoService = new StubTransactionDemoService();
        userService = new StubUserService();
        pitchService = new StubPitchService();
        equipmentOfferService = new StubEquipmentOfferService();
        openGameService = new StubOpenGameService();

        bookingController = new BookingController(bookingService, bookingSearchService);
        demoController = new DemoController(nPlusOneDemoService, transactionDemoService);
        userController = new UserController(userService);
        pitchController = new PitchController(pitchService);
        equipmentOfferController = new EquipmentOfferController(equipmentOfferService);
        openGameController = new OpenGameController(openGameService);
    }

    @Test
    void shouldHandleBookingControllerEndpoints() {
        BookingDto dto = bookingDto(1L);
        BookingSearchRequest searchRequest = new BookingSearchRequest();
        BookingSearchResponseDto searchResponse = new BookingSearchResponseDto(
                "jpql",
                false,
                0,
                5,
                1,
                1,
                true,
                true,
                List.of(dto)
        );
        BookingUpsertRequest request = bookingRequest();

        bookingService.all = List.of(dto);
        bookingService.byId = dto;
        bookingService.created = dto;
        bookingService.bulkCreated = List.of(dto);
        bookingService.updated = dto;
        bookingSearchService.jpqlResponse = searchResponse;
        bookingSearchService.nativeResponse = searchResponse;

        searchRequest.setPage(0);
        searchRequest.setSize(5);

        assertThat(bookingController.getAll().getBody()).containsExactly(dto);
        assertThat(bookingController.searchWithJpql(searchRequest).getBody()).isEqualTo(searchResponse);
        assertThat(bookingController.searchWithNative(searchRequest).getBody()).isEqualTo(searchResponse);
        assertThat(bookingController.getById(1L).getBody()).isEqualTo(dto);
        assertThat(bookingController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(bookingController.create(request).getHeaders().getLocation()).hasToString("/api/v1/bookings/1");
        assertThat(bookingController.createBulk(List.of(request)).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(bookingController.update(1L, request).getBody()).isEqualTo(dto);
        assertThat(bookingController.delete(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(bookingService.deletedId).isEqualTo(1L);
    }

    @Test
    void shouldHandleDemoControllerSuccessScenarios() {
        EntityCountSnapshotDto before = new EntityCountSnapshotDto(1, 2, 3, 4, 5);
        EntityCountSnapshotDto after = new EntityCountSnapshotDto(2, 3, 4, 5, 6);
        NPlusOneDemoResultDto nPlusOne = new NPlusOneDemoResultDto("bad", 3, 4, 5);
        List<BookingUpsertRequest> requests = List.of(bookingRequest());

        nPlusOneDemoService.badCase = nPlusOne;
        nPlusOneDemoService.solvedCase = nPlusOne;
        transactionDemoService.snapshots = List.of(before, after, before, after, before, after, before, after);

        assertThat(demoController.demonstrateNPlusOneBadCase().getBody()).isEqualTo(nPlusOne);
        assertThat(demoController.demonstrateNPlusOneSolvedCase().getBody()).isEqualTo(nPlusOne);

        ResponseEntity<TransactionDemoResultDto> withoutTransaction = demoController.demonstrateWithoutTransaction();
        ResponseEntity<TransactionDemoResultDto> withTransaction = demoController.demonstrateWithTransaction();
        ResponseEntity<BulkBookingTransactionDemoResultDto> bulkWithout =
                demoController.demonstrateBulkWithoutTransaction(requests);
        ResponseEntity<BulkBookingTransactionDemoResultDto> bulkWith =
                demoController.demonstrateBulkWithTransaction(requests);

        assertThat(withoutTransaction.getBody().error()).isEqualTo("No error");
        assertThat(withTransaction.getBody().error()).isEqualTo("No error");
        assertThat(bulkWithout.getBody().requestedItems()).isEqualTo(1);
        assertThat(bulkWithout.getBody().error()).isEqualTo("No error");
        assertThat(bulkWith.getBody().error()).isEqualTo("No error");
    }

    @Test
    void shouldHandleDemoControllerFailures() {
        EntityCountSnapshotDto before = new EntityCountSnapshotDto(1, 2, 3, 4, 5);
        EntityCountSnapshotDto after = new EntityCountSnapshotDto(1, 2, 3, 4, 5);
        List<BookingUpsertRequest> requests = List.of(bookingRequest());

        transactionDemoService.snapshots = List.of(before, after, before, after, before, after, before, after);
        transactionDemoService.throwWithoutTransaction = new IllegalStateException("boom");
        transactionDemoService.throwWithTransaction = new IllegalStateException("boom");
        transactionDemoService.throwBulkWithoutTransaction = new IllegalStateException("boom");
        transactionDemoService.throwBulkWithTransaction = new IllegalStateException("boom");

        assertThat(demoController.demonstrateWithoutTransaction().getBody().error()).isEqualTo("boom");
        assertThat(demoController.demonstrateWithTransaction().getBody().error()).isEqualTo("boom");
        assertThat(demoController.demonstrateBulkWithoutTransaction(requests).getBody().error()).isEqualTo("boom");
        assertThat(demoController.demonstrateBulkWithTransaction(requests).getBody().error()).isEqualTo("boom");
    }

    @Test
    void shouldHandleUserControllerEndpoints() {
        UserDto dto = new UserDto(7L, "Alexey", 78, UserRole.PLAYER);
        UserUpsertRequest request = new UserUpsertRequest("Alexey", 78, UserRole.PLAYER);

        userService.all = List.of(dto);
        userService.byId = dto;
        userService.created = dto;
        userService.updated = dto;

        assertThat(userController.getAll().getBody()).containsExactly(dto);
        assertThat(userController.getById(7L).getBody()).isEqualTo(dto);
        assertThat(userController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userController.update(7L, request).getBody()).isEqualTo(dto);
        assertThat(userController.delete(7L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userService.deletedId).isEqualTo(7L);
    }

    @Test
    void shouldHandlePitchControllerEndpoints() {
        PitchDto dto = new PitchDto(5L, "Arena", PitchType.EIGHT, "Central", "Nemiga", new BigDecimal("120.00"));
        PitchUpsertRequest request = new PitchUpsertRequest(
                "Arena",
                PitchType.EIGHT,
                "Central",
                "Nemiga",
                new BigDecimal("120.00")
        );

        pitchService.all = List.of(dto);
        pitchService.byId = dto;
        pitchService.created = dto;
        pitchService.updated = dto;

        assertThat(pitchController.getAll("Central").getBody()).containsExactly(dto);
        assertThat(pitchController.getById(5L).getBody()).isEqualTo(dto);
        assertThat(pitchController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(pitchController.update(5L, request).getBody()).isEqualTo(dto);
        assertThat(pitchController.delete(5L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(pitchService.deletedId).isEqualTo(5L);
    }

    @Test
    void shouldHandleEquipmentOfferControllerEndpoints() {
        EquipmentOfferDto dto = new EquipmentOfferDto(
                6L,
                5L,
                EquipmentItemType.BALL,
                10,
                new BigDecimal("15.00")
        );
        EquipmentOfferUpsertRequest request = new EquipmentOfferUpsertRequest(
                5L,
                EquipmentItemType.BALL,
                10,
                new BigDecimal("15.00")
        );

        equipmentOfferService.all = List.of(dto);
        equipmentOfferService.byId = dto;
        equipmentOfferService.created = dto;
        equipmentOfferService.updated = dto;

        assertThat(equipmentOfferController.getAll().getBody()).containsExactly(dto);
        assertThat(equipmentOfferController.getById(6L).getBody()).isEqualTo(dto);
        assertThat(equipmentOfferController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(equipmentOfferController.update(6L, request).getBody()).isEqualTo(dto);
        assertThat(equipmentOfferController.delete(6L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(equipmentOfferService.deletedId).isEqualTo(6L);
    }

    @Test
    void shouldHandleOpenGameControllerEndpoints() {
        OpenGameDto dto = new OpenGameDto(3L, 1L, 7L, 40, 70, 12, OpenGameStatus.OPEN, List.of(7L, 8L));
        OpenGameUpsertRequest request = new OpenGameUpsertRequest(
                1L,
                7L,
                40,
                70,
                12,
                OpenGameStatus.OPEN,
                List.of(7L, 8L)
        );

        openGameService.all = List.of(dto);
        openGameService.byId = dto;
        openGameService.created = dto;
        openGameService.updated = dto;

        assertThat(openGameController.getAll().getBody()).containsExactly(dto);
        assertThat(openGameController.getById(3L).getBody()).isEqualTo(dto);
        assertThat(openGameController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(openGameController.update(3L, request).getBody()).isEqualTo(dto);
        assertThat(openGameController.delete(3L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(openGameService.deletedId).isEqualTo(3L);
    }

    private BookingDto bookingDto(Long id) {
        return new BookingDto(
                id,
                11L,
                7L,
                LocalDateTime.parse("2026-05-01T18:00:00"),
                LocalDateTime.parse("2026-05-01T20:00:00"),
                BookingStatus.CREATED
        );
    }

    private BookingUpsertRequest bookingRequest() {
        return new BookingUpsertRequest(
                11L,
                7L,
                LocalDateTime.parse("2026-05-01T18:00:00"),
                LocalDateTime.parse("2026-05-01T20:00:00"),
                BookingStatus.CREATED
        );
    }

    private static final class StubBookingService extends BookingService {

        private List<BookingDto> all = List.of();
        private BookingDto byId;
        private BookingDto created;
        private List<BookingDto> bulkCreated = List.of();
        private BookingDto updated;
        private Long deletedId;

        private StubBookingService() {
            super(
                    mock(BookingRepository.class),
                    mock(PitchRepository.class),
                    mock(UserRepository.class),
                    new BookingSearchService(mock(BookingRepository.class))
            );
        }

        @Override
        public List<BookingDto> findAll() {
            return all;
        }

        @Override
        public BookingDto findById(Long id) {
            return byId;
        }

        @Override
        public BookingDto create(BookingUpsertRequest request) {
            return created;
        }

        @Override
        public List<BookingDto> createBulk(List<BookingUpsertRequest> requests) {
            return bulkCreated;
        }

        @Override
        public BookingDto update(Long id, BookingUpsertRequest request) {
            return updated;
        }

        @Override
        public void delete(Long id) {
            deletedId = id;
        }
    }

    private static final class StubBookingSearchService extends BookingSearchService {

        private BookingSearchResponseDto jpqlResponse;
        private BookingSearchResponseDto nativeResponse;

        private StubBookingSearchService() {
            super(mock(BookingRepository.class));
        }

        @Override
        public BookingSearchResponseDto searchWithJpql(
                pitchmarketplace.dto.BookingSearchCriteria criteria,
                Integer page,
                Integer size
        ) {
            return jpqlResponse;
        }

        @Override
        public BookingSearchResponseDto searchWithNative(
                pitchmarketplace.dto.BookingSearchCriteria criteria,
                Integer page,
                Integer size
        ) {
            return nativeResponse;
        }
    }

    private static final class StubNPlusOneDemoService extends NPlusOneDemoService {

        private NPlusOneDemoResultDto badCase;
        private NPlusOneDemoResultDto solvedCase;

        private StubNPlusOneDemoService() {
            super(mock(OpenGameRepository.class), entityManagerFactory());
        }

        @Override
        public NPlusOneDemoResultDto demonstrateBadCase() {
            return badCase;
        }

        @Override
        public NPlusOneDemoResultDto demonstrateSolvedCase() {
            return solvedCase;
        }
    }

    private static final class StubTransactionDemoService extends TransactionDemoService {

        private List<EntityCountSnapshotDto> snapshots = List.of();
        private int snapshotIndex;
        private RuntimeException throwWithoutTransaction;
        private RuntimeException throwWithTransaction;
        private RuntimeException throwBulkWithoutTransaction;
        private RuntimeException throwBulkWithTransaction;

        private StubTransactionDemoService() {
            super(
                    mock(UserRepository.class),
                    mock(PitchRepository.class),
                    mock(BookingRepository.class),
                    mock(OpenGameRepository.class),
                    mock(EquipmentOfferRepository.class),
                    new StubBookingService()
            );
        }

        @Override
        public EntityCountSnapshotDto snapshot() {
            return snapshots.get(snapshotIndex++);
        }

        @Override
        public void saveRelatedEntitiesWithoutTransactionAndFail() {
            if (throwWithoutTransaction != null) {
                throw throwWithoutTransaction;
            }
        }

        @Override
        public void saveRelatedEntitiesWithTransactionAndFail() {
            if (throwWithTransaction != null) {
                throw throwWithTransaction;
            }
        }

        @Override
        public List<BookingDto> createBookingsBulkWithoutTransaction(List<BookingUpsertRequest> requests) {
            if (throwBulkWithoutTransaction != null) {
                throw throwBulkWithoutTransaction;
            }
            return List.of();
        }

        @Override
        public List<BookingDto> createBookingsBulkWithTransaction(List<BookingUpsertRequest> requests) {
            if (throwBulkWithTransaction != null) {
                throw throwBulkWithTransaction;
            }
            return List.of();
        }
    }

    private static final class StubUserService extends UserService {

        private List<UserDto> all = List.of();
        private UserDto byId;
        private UserDto created;
        private UserDto updated;
        private Long deletedId;

        private StubUserService() {
            super(mock(UserRepository.class), new BookingSearchService(mock(BookingRepository.class)));
        }

        @Override
        public List<UserDto> findAll() {
            return all;
        }

        @Override
        public UserDto findById(Long id) {
            return byId;
        }

        @Override
        public UserDto create(UserUpsertRequest request) {
            return created;
        }

        @Override
        public UserDto update(Long id, UserUpsertRequest request) {
            return updated;
        }

        @Override
        public void delete(Long id) {
            deletedId = id;
        }
    }

    private static final class StubPitchService extends PitchService {

        private List<PitchDto> all = List.of();
        private PitchDto byId;
        private PitchDto created;
        private PitchDto updated;
        private Long deletedId;

        private StubPitchService() {
            super(mock(PitchRepository.class), new BookingSearchService(mock(BookingRepository.class)));
        }

        @Override
        public List<PitchDto> findAll(String district) {
            return all;
        }

        @Override
        public PitchDto findById(Long id) {
            return byId;
        }

        @Override
        public PitchDto create(PitchUpsertRequest request) {
            return created;
        }

        @Override
        public PitchDto update(Long id, PitchUpsertRequest request) {
            return updated;
        }

        @Override
        public void delete(Long id) {
            deletedId = id;
        }
    }

    private static final class StubEquipmentOfferService extends EquipmentOfferService {

        private List<EquipmentOfferDto> all = List.of();
        private EquipmentOfferDto byId;
        private EquipmentOfferDto created;
        private EquipmentOfferDto updated;
        private Long deletedId;

        private StubEquipmentOfferService() {
            super(mock(EquipmentOfferRepository.class), mock(PitchRepository.class));
        }

        @Override
        public List<EquipmentOfferDto> findAll() {
            return all;
        }

        @Override
        public EquipmentOfferDto findById(Long id) {
            return byId;
        }

        @Override
        public EquipmentOfferDto create(EquipmentOfferUpsertRequest request) {
            return created;
        }

        @Override
        public EquipmentOfferDto update(Long id, EquipmentOfferUpsertRequest request) {
            return updated;
        }

        @Override
        public void delete(Long id) {
            deletedId = id;
        }
    }

    private static final class StubOpenGameService extends OpenGameService {

        private List<OpenGameDto> all = List.of();
        private OpenGameDto byId;
        private OpenGameDto created;
        private OpenGameDto updated;
        private Long deletedId;

        private StubOpenGameService() {
            super(mock(OpenGameRepository.class), mock(BookingRepository.class), mock(UserRepository.class));
        }

        @Override
        public List<OpenGameDto> findAll() {
            return all;
        }

        @Override
        public OpenGameDto findById(Long id) {
            return byId;
        }

        @Override
        public OpenGameDto create(OpenGameUpsertRequest request) {
            return created;
        }

        @Override
        public OpenGameDto update(Long id, OpenGameUpsertRequest request) {
            return updated;
        }

        @Override
        public void delete(Long id) {
            deletedId = id;
        }
    }

    private static EntityManagerFactory entityManagerFactory() {
        Statistics statistics = (Statistics) Proxy.newProxyInstance(
                Statistics.class.getClassLoader(),
                new Class[]{Statistics.class},
                (proxy, method, args) -> defaultValue(method.getReturnType())
        );
        SessionFactory sessionFactory = (SessionFactory) Proxy.newProxyInstance(
                SessionFactory.class.getClassLoader(),
                new Class[]{SessionFactory.class},
                (proxy, method, args) -> {
                    if ("getStatistics".equals(method.getName())) {
                        return statistics;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
        return (EntityManagerFactory) Proxy.newProxyInstance(
                EntityManagerFactory.class.getClassLoader(),
                new Class[]{EntityManagerFactory.class},
                (proxy, method, args) -> {
                    if ("unwrap".equals(method.getName()) && args != null && args.length == 1
                            && args[0] == SessionFactory.class) {
                        return sessionFactory;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == float.class) {
            return 0F;
        }
        return null;
    }
}
