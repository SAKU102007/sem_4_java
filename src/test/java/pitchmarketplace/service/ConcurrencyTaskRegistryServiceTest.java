package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pitchmarketplace.dto.AsyncTaskAcceptedDto;
import pitchmarketplace.dto.AsyncTaskState;
import pitchmarketplace.dto.AsyncTaskStatusDto;
import pitchmarketplace.dto.PitchLoadReportResultDto;
import pitchmarketplace.exception.ResourceNotFoundException;

class ConcurrencyTaskRegistryServiceTest {

    private ConcurrencyTaskRegistryService service;

    @BeforeEach
    void setUp() {
        service = new ConcurrencyTaskRegistryService();
    }

    @Test
    void shouldRegisterTaskAndExposeInitialCounters() {
        AsyncTaskAcceptedDto accepted = service.registerTask();

        assertThat(accepted.taskId()).isEqualTo(1L);
        assertThat(accepted.status()).isEqualTo(AsyncTaskState.ACCEPTED);
        assertThat(service.getTaskStatus(1L).status()).isEqualTo(AsyncTaskState.ACCEPTED);
        assertThat(service.snapshotCounters().submittedTasks()).isEqualTo(1L);
        assertThat(service.snapshotCounters().completedTasks()).isZero();
        assertThat(service.snapshotCounters().failedTasks()).isZero();
    }

    @Test
    void shouldMarkTaskRunningAndCompleted() {
        AsyncTaskAcceptedDto accepted = service.registerTask();
        PitchLoadReportResultDto result = new PitchLoadReportResultDto(1L, "Arena", 3, 2, 1, 1, 1);

        service.markRunning(accepted.taskId());
        service.markCompleted(accepted.taskId(), result);

        AsyncTaskStatusDto status = service.getTaskStatus(accepted.taskId());
        assertThat(status.status()).isEqualTo(AsyncTaskState.COMPLETED);
        assertThat(status.startedAt()).isNotNull();
        assertThat(status.finishedAt()).isNotNull();
        assertThat(status.result()).isEqualTo(result);
        assertThat(service.snapshotCounters().completedTasks()).isEqualTo(1L);
    }

    @Test
    void shouldMarkTaskFailed() {
        AsyncTaskAcceptedDto accepted = service.registerTask();

        service.markRunning(accepted.taskId());
        service.markFailed(accepted.taskId(), "boom");

        AsyncTaskStatusDto status = service.getTaskStatus(accepted.taskId());
        assertThat(status.status()).isEqualTo(AsyncTaskState.FAILED);
        assertThat(status.error()).isEqualTo("boom");
        assertThat(status.finishedAt()).isNotNull();
        assertThat(service.snapshotCounters().failedTasks()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenTaskDoesNotExist() {
        assertThatThrownBy(() -> service.getTaskStatus(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Async task not found. id=99");

        assertThatThrownBy(() -> service.markRunning(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Async task not found. id=99");
    }
}
