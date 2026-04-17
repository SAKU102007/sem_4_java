package pitchmarketplace.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import pitchmarketplace.dto.AsyncTaskAcceptedDto;
import pitchmarketplace.dto.AsyncTaskState;
import pitchmarketplace.dto.AsyncTaskStatusDto;
import pitchmarketplace.dto.ConcurrencyCounterStatsDto;
import pitchmarketplace.dto.PitchLoadReportResultDto;
import pitchmarketplace.exception.ResourceNotFoundException;

@Service
public class ConcurrencyTaskRegistryService {

    private final AtomicLong taskIdSequence = new AtomicLong();
    private final AtomicLong submittedTasks = new AtomicLong();
    private final AtomicLong completedTasks = new AtomicLong();
    private final AtomicLong failedTasks = new AtomicLong();
    private final Map<Long, AsyncTaskStatusDto> tasks = new ConcurrentHashMap<>();

    public AsyncTaskAcceptedDto registerTask() {
        long taskId = taskIdSequence.incrementAndGet();
        Instant createdAt = Instant.now();
        tasks.put(taskId, new AsyncTaskStatusDto(
                taskId,
                AsyncTaskState.ACCEPTED,
                createdAt,
                null,
                null,
                null,
                null
        ));
        submittedTasks.incrementAndGet();
        return new AsyncTaskAcceptedDto(taskId, AsyncTaskState.ACCEPTED, createdAt);
    }

    public AsyncTaskStatusDto getTaskStatus(long taskId) {
        return requireTask(taskId);
    }

    public void markRunning(long taskId) {
        AsyncTaskStatusDto current = requireTask(taskId);
        tasks.put(taskId, new AsyncTaskStatusDto(
                taskId,
                AsyncTaskState.RUNNING,
                current.createdAt(),
                current.startedAt() == null ? Instant.now() : current.startedAt(),
                null,
                null,
                null
        ));
    }

    public void markCompleted(long taskId, PitchLoadReportResultDto result) {
        AsyncTaskStatusDto current = requireTask(taskId);
        tasks.put(taskId, new AsyncTaskStatusDto(
                taskId,
                AsyncTaskState.COMPLETED,
                current.createdAt(),
                current.startedAt() == null ? Instant.now() : current.startedAt(),
                Instant.now(),
                null,
                result
        ));
        completedTasks.incrementAndGet();
    }

    public void markFailed(long taskId, String error) {
        AsyncTaskStatusDto current = requireTask(taskId);
        tasks.put(taskId, new AsyncTaskStatusDto(
                taskId,
                AsyncTaskState.FAILED,
                current.createdAt(),
                current.startedAt() == null ? Instant.now() : current.startedAt(),
                Instant.now(),
                error,
                null
        ));
        failedTasks.incrementAndGet();
    }

    public ConcurrencyCounterStatsDto snapshotCounters() {
        return new ConcurrencyCounterStatsDto(
                taskIdSequence.get(),
                submittedTasks.get(),
                completedTasks.get(),
                failedTasks.get()
        );
    }

    private AsyncTaskStatusDto requireTask(long taskId) {
        AsyncTaskStatusDto task = tasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Async task not found. id=" + taskId);
        }
        return task;
    }
}
