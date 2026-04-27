package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.dto.PitchLoadReportRequest;
import pitchmarketplace.dto.RaceConditionDemoRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.PitchRepository;

@ExtendWith(MockitoExtension.class)
class ConcurrencyDemoServiceTest {

    @Mock
    private PitchRepository pitchRepository;

    private ConcurrencyTaskRegistryService registryService;
    private RecordingPitchLoadReportAsyncWorker worker;
    private ConcurrencyDemoService service;

    @BeforeEach
    void setUp() {
        registryService = new ConcurrencyTaskRegistryService();
        worker = new RecordingPitchLoadReportAsyncWorker(registryService);
        service = new ConcurrencyDemoService(
                pitchRepository,
                registryService,
                worker,
                new RaceConditionDemoService()
        );
    }

    @Test
    void shouldStartPitchLoadReportAndScheduleWorker() {
        Pitch pitch = new Pitch(5L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN);
        when(pitchRepository.findById(5L)).thenReturn(Optional.of(pitch));

        var accepted = service.startPitchLoadReport(new PitchLoadReportRequest(5L));

        assertThat(accepted.taskId()).isEqualTo(1L);
        assertThat(worker.lastTaskId).isEqualTo(1L);
        assertThat(worker.lastPitchId).isEqualTo(5L);
        assertThat(worker.lastPitchName).isEqualTo("Arena");
        assertThat(service.getCounterStats().submittedTasks()).isEqualTo(1L);
        assertThat(service.getPitchLoadReportStatus(1L).taskId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenPitchIsMissing() {
        when(pitchRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startPitchLoadReport(new PitchLoadReportRequest(77L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pitch not found. id=77");

        assertThat(service.getCounterStats().submittedTasks()).isZero();
    }

    @Test
    void shouldDelegateRaceConditionDemo() {
        var result = service.demonstrateRaceCondition(new RaceConditionDemoRequest(50, 500));

        assertThat(result.expected()).isEqualTo(25000L);
        assertThat(result.safeCounter()).isEqualTo(25000L);
        assertThat(result.unsafeLostUpdates()).isPositive();
    }

    private static final class RecordingPitchLoadReportAsyncWorker extends PitchLoadReportAsyncWorker {

        private long lastTaskId;
        private Long lastPitchId;
        private String lastPitchName;

        private RecordingPitchLoadReportAsyncWorker(ConcurrencyTaskRegistryService registryService) {
            super(mock(BookingRepository.class), mock(OpenGameRepository.class), registryService, 0L);
        }

        @Override
        public CompletableFuture<Void> generatePitchLoadReport(long taskId, Long pitchId, String pitchName) {
            lastTaskId = taskId;
            lastPitchId = pitchId;
            lastPitchName = pitchName;
            return CompletableFuture.completedFuture(null);
        }
    }
}
