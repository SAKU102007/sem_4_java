package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.dto.AsyncTaskAcceptedDto;
import pitchmarketplace.dto.AsyncTaskState;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.OpenGameRepository;

@ExtendWith(MockitoExtension.class)
class PitchLoadReportAsyncWorkerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private OpenGameRepository openGameRepository;

    private ConcurrencyTaskRegistryService registryService;
    private PitchLoadReportAsyncWorker worker;

    @BeforeEach
    void setUp() {
        registryService = new ConcurrencyTaskRegistryService();
        worker = new PitchLoadReportAsyncWorker(bookingRepository, openGameRepository, registryService);
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
    }

    @Test
    void shouldGeneratePitchLoadReportAndCompleteTask() {
        AsyncTaskAcceptedDto accepted = registryService.registerTask();
        when(bookingRepository.countByPitch_Id(1L)).thenReturn(5L);
        when(bookingRepository.countByPitch_IdAndStatus(1L, pitchmarketplace.domain.enums.BookingStatus.CONFIRMED))
                .thenReturn(3L);
        when(bookingRepository.countByPitch_IdAndStatus(1L, pitchmarketplace.domain.enums.BookingStatus.CANCELLED))
                .thenReturn(1L);
        when(openGameRepository.countByBookingPitch_Id(1L)).thenReturn(2L);
        when(openGameRepository.countByBookingPitch_IdAndStatus(1L, pitchmarketplace.domain.enums.OpenGameStatus.OPEN))
                .thenReturn(1L);

        CompletableFuture<Void> future = worker.generatePitchLoadReport(accepted.taskId(), 1L, "Arena");
        future.join();

        assertThat(registryService.getTaskStatus(accepted.taskId()).status()).isEqualTo(AsyncTaskState.COMPLETED);
        assertThat(registryService.getTaskStatus(accepted.taskId()).result().pitchName()).isEqualTo("Arena");
        assertThat(registryService.getTaskStatus(accepted.taskId()).result().totalBookings()).isEqualTo(5L);
        assertThat(registryService.snapshotCounters().completedTasks()).isEqualTo(1L);
    }

    @Test
    void shouldMarkTaskFailedWhenWorkerThrowsRuntimeException() {
        AsyncTaskAcceptedDto accepted = registryService.registerTask();
        when(bookingRepository.countByPitch_Id(1L)).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> worker.generatePitchLoadReport(accepted.taskId(), 1L, "Arena"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        assertThat(registryService.getTaskStatus(accepted.taskId()).status()).isEqualTo(AsyncTaskState.FAILED);
        assertThat(registryService.getTaskStatus(accepted.taskId()).error()).isEqualTo("boom");
        assertThat(registryService.snapshotCounters().failedTasks()).isEqualTo(1L);
    }

    @Test
    void shouldMarkTaskFailedWhenThreadIsInterrupted() {
        AsyncTaskAcceptedDto accepted = registryService.registerTask();
        Thread.currentThread().interrupt();

        worker.generatePitchLoadReport(accepted.taskId(), 1L, "Arena").join();

        assertThat(registryService.getTaskStatus(accepted.taskId()).status()).isEqualTo(AsyncTaskState.FAILED);
        assertThat(registryService.getTaskStatus(accepted.taskId()).error()).isEqualTo("Task interrupted");
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        assertThat(registryService.snapshotCounters().failedTasks()).isEqualTo(1L);
    }
}
