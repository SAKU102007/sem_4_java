package pitchmarketplace.service;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.OpenGameStatus;
import pitchmarketplace.dto.PitchLoadReportResultDto;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.OpenGameRepository;

@Service
public class PitchLoadReportAsyncWorker {

    private static final long REPORT_GENERATION_DELAY_MS = 1000L;

    private final BookingRepository bookingRepository;
    private final OpenGameRepository openGameRepository;
    private final ConcurrencyTaskRegistryService taskRegistryService;

    public PitchLoadReportAsyncWorker(
            BookingRepository bookingRepository,
            OpenGameRepository openGameRepository,
            ConcurrencyTaskRegistryService taskRegistryService
    ) {
        this.bookingRepository = bookingRepository;
        this.openGameRepository = openGameRepository;
        this.taskRegistryService = taskRegistryService;
    }

    @Async("concurrencyTaskExecutor")
    public CompletableFuture<Void> generatePitchLoadReport(long taskId, Long pitchId, String pitchName) {
        taskRegistryService.markRunning(taskId);
        try {
            Thread.sleep(REPORT_GENERATION_DELAY_MS);
            PitchLoadReportResultDto result = new PitchLoadReportResultDto(
                    pitchId,
                    pitchName,
                    bookingRepository.countByPitch_Id(pitchId),
                    bookingRepository.countByPitch_IdAndStatus(pitchId, BookingStatus.CONFIRMED),
                    bookingRepository.countByPitch_IdAndStatus(pitchId, BookingStatus.CANCELLED),
                    openGameRepository.countByBookingPitch_Id(pitchId),
                    openGameRepository.countByBookingPitch_IdAndStatus(pitchId, OpenGameStatus.OPEN)
            );
            taskRegistryService.markCompleted(taskId, result);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            taskRegistryService.markFailed(taskId, "Task interrupted");
        } catch (RuntimeException ex) {
            taskRegistryService.markFailed(taskId, ex.getMessage());
            throw ex;
        }
        return CompletableFuture.completedFuture(null);
    }
}
