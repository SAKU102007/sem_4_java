package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.dto.EntityCountSnapshotDto;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.EquipmentOfferRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.PitchRepository;
import pitchmarketplace.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionDemoServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PitchRepository pitchRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private OpenGameRepository openGameRepository;

    @Mock
    private EquipmentOfferRepository equipmentOfferRepository;

    private TransactionDemoService transactionDemoService;
    private RecordingBookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new RecordingBookingService(
                bookingRepository,
                pitchRepository,
                userRepository,
                new BookingSearchService(bookingRepository)
        );
        transactionDemoService = new TransactionDemoService(
                userRepository,
                pitchRepository,
                bookingRepository,
                openGameRepository,
                equipmentOfferRepository,
                bookingService
        );
    }

    @Test
    void shouldReturnEntitySnapshot() {
        when(userRepository.count()).thenReturn(5L);
        when(pitchRepository.count()).thenReturn(4L);
        when(bookingRepository.count()).thenReturn(9L);
        when(openGameRepository.count()).thenReturn(3L);
        when(equipmentOfferRepository.count()).thenReturn(7L);

        EntityCountSnapshotDto snapshot = transactionDemoService.snapshot();

        assertThat(snapshot.users()).isEqualTo(5L);
        assertThat(snapshot.pitches()).isEqualTo(4L);
        assertThat(snapshot.bookings()).isEqualTo(9L);
        assertThat(snapshot.openGames()).isEqualTo(3L);
        assertThat(snapshot.equipmentOffers()).isEqualTo(7L);
    }

    @Test
    void shouldDelegateBulkCreationWithoutTransaction() {
        List<BookingUpsertRequest> requests = List.of();

        transactionDemoService.createBookingsBulkWithoutTransaction(requests);

        assertThat(bookingService.lastWithoutTransactionRequests).isSameAs(requests);
    }

    @Test
    void shouldDelegateBulkCreationWithTransaction() {
        List<BookingUpsertRequest> requests = List.of();

        transactionDemoService.createBookingsBulkWithTransaction(requests);

        assertThat(bookingService.lastWithTransactionRequests).isSameAs(requests);
    }

    private static final class RecordingBookingService extends BookingService {

        private List<BookingUpsertRequest> lastWithoutTransactionRequests;
        private List<BookingUpsertRequest> lastWithTransactionRequests;

        private RecordingBookingService(
                BookingRepository bookingRepository,
                PitchRepository pitchRepository,
                UserRepository userRepository,
                BookingSearchService bookingSearchService
        ) {
            super(bookingRepository, pitchRepository, userRepository, bookingSearchService);
        }

        @Override
        public List<BookingDto> createBulk(List<BookingUpsertRequest> requests) {
            lastWithTransactionRequests = requests;
            return List.of();
        }

        @Override
        public List<BookingDto> createBulkWithoutTransaction(List<BookingUpsertRequest> requests) {
            lastWithoutTransactionRequests = requests;
            return List.of();
        }
    }
}
