package pitchmarketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pitchmarketplace.dto.AsyncTaskAcceptedDto;
import pitchmarketplace.dto.AsyncTaskStatusDto;
import pitchmarketplace.dto.ConcurrencyCounterStatsDto;
import pitchmarketplace.dto.PitchLoadReportRequest;
import pitchmarketplace.dto.RaceConditionDemoRequest;
import pitchmarketplace.dto.RaceConditionDemoResultDto;
import pitchmarketplace.service.ConcurrencyDemoService;

@RestController
@Validated
@RequestMapping("/api/v1/concurrency")
@Tag(name = "Concurrency", description = "Asynchronous operations and concurrency demonstrations")
public class ConcurrencyController {

    private final ConcurrencyDemoService concurrencyDemoService;

    public ConcurrencyController(ConcurrencyDemoService concurrencyDemoService) {
        this.concurrencyDemoService = concurrencyDemoService;
    }

    @PostMapping("/pitch-load-reports")
    @Operation(
            summary = "Start asynchronous pitch load report",
            description = "Creates an asynchronous business task that calculates booking and open game load for a pitch."
    )
    public ResponseEntity<AsyncTaskAcceptedDto> startPitchLoadReport(@Valid @RequestBody PitchLoadReportRequest request) {
        AsyncTaskAcceptedDto acceptedTask = concurrencyDemoService.startPitchLoadReport(request);
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/concurrency/pitch-load-reports/" + acceptedTask.taskId()))
                .body(acceptedTask);
    }

    @GetMapping("/pitch-load-reports/counters")
    @Operation(
            summary = "Get thread-safe async task counters",
            description = "Shows current values of Atomic-based counters for submitted and completed asynchronous tasks."
    )
    public ResponseEntity<ConcurrencyCounterStatsDto> getCounterStats() {
        return ResponseEntity.ok(concurrencyDemoService.getCounterStats());
    }

    @GetMapping("/pitch-load-reports/{taskId}")
    @Operation(
            summary = "Get asynchronous task status",
            description = "Returns the current state and result of a previously created asynchronous task."
    )
    public ResponseEntity<AsyncTaskStatusDto> getPitchLoadReportStatus(
            @PathVariable @Positive(message = "taskId must be positive") Long taskId
    ) {
        return ResponseEntity.ok(concurrencyDemoService.getPitchLoadReportStatus(taskId));
    }

    @PostMapping("/race-condition")
    @Operation(
            summary = "Demonstrate race condition and safe counters",
            description = """
                    Runs 50+ concurrent threads to show lost updates for an unsafe counter
                    and the correct result for synchronized and Atomic counters.
                    """
    )
    public ResponseEntity<RaceConditionDemoResultDto> demonstrateRaceCondition(
            @Valid @RequestBody RaceConditionDemoRequest request
    ) {
        return ResponseEntity.ok(concurrencyDemoService.demonstrateRaceCondition(request));
    }
}
