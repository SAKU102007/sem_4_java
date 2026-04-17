package pitchmarketplace.service;

import org.springframework.stereotype.Service;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.dto.AsyncTaskAcceptedDto;
import pitchmarketplace.dto.AsyncTaskStatusDto;
import pitchmarketplace.dto.ConcurrencyCounterStatsDto;
import pitchmarketplace.dto.PitchLoadReportRequest;
import pitchmarketplace.dto.RaceConditionDemoRequest;
import pitchmarketplace.dto.RaceConditionDemoResultDto;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.PitchRepository;

@Service
public class ConcurrencyDemoService {

    private final PitchRepository pitchRepository;
    private final ConcurrencyTaskRegistryService taskRegistryService;
    private final PitchLoadReportAsyncWorker pitchLoadReportAsyncWorker;
    private final RaceConditionDemoService raceConditionDemoService;

    public ConcurrencyDemoService(
            PitchRepository pitchRepository,
            ConcurrencyTaskRegistryService taskRegistryService,
            PitchLoadReportAsyncWorker pitchLoadReportAsyncWorker,
            RaceConditionDemoService raceConditionDemoService
    ) {
        this.pitchRepository = pitchRepository;
        this.taskRegistryService = taskRegistryService;
        this.pitchLoadReportAsyncWorker = pitchLoadReportAsyncWorker;
        this.raceConditionDemoService = raceConditionDemoService;
    }

    public AsyncTaskAcceptedDto startPitchLoadReport(PitchLoadReportRequest request) {
        Pitch pitch = pitchRepository.findById(request.pitchId())
                .orElseThrow(() -> new ResourceNotFoundException("Pitch not found. id=" + request.pitchId()));

        AsyncTaskAcceptedDto acceptedTask = taskRegistryService.registerTask();
        pitchLoadReportAsyncWorker.generatePitchLoadReport(acceptedTask.taskId(), pitch.getId(), pitch.getName());
        return acceptedTask;
    }

    public AsyncTaskStatusDto getPitchLoadReportStatus(long taskId) {
        return taskRegistryService.getTaskStatus(taskId);
    }

    public ConcurrencyCounterStatsDto getCounterStats() {
        return taskRegistryService.snapshotCounters();
    }

    public RaceConditionDemoResultDto demonstrateRaceCondition(RaceConditionDemoRequest request) {
        return raceConditionDemoService.demonstrate(request);
    }
}
