package pitchmarketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.dto.BulkBookingTransactionDemoResultDto;
import pitchmarketplace.dto.EntityCountSnapshotDto;
import pitchmarketplace.dto.NPlusOneDemoResultDto;
import pitchmarketplace.dto.TransactionDemoResultDto;
import pitchmarketplace.service.NPlusOneDemoService;
import pitchmarketplace.service.TransactionDemoService;

@RestController
@RequestMapping("/api/v1/demos")
@Tag(name = "Demos", description = "Educational endpoints for JPA and transaction behaviour")
public class DemoController {

    private final NPlusOneDemoService nPlusOneDemoService;
    private final TransactionDemoService transactionDemoService;

    public DemoController(NPlusOneDemoService nPlusOneDemoService, TransactionDemoService transactionDemoService) {
        this.nPlusOneDemoService = nPlusOneDemoService;
        this.transactionDemoService = transactionDemoService;
    }

    @GetMapping("/n-plus-one/bad")
    @Operation(summary = "Show N+1 problem", description = "Demonstrates the inefficient N+1 loading scenario.")
    public ResponseEntity<NPlusOneDemoResultDto> demonstrateNPlusOneBadCase() {
        return ResponseEntity.ok(nPlusOneDemoService.demonstrateBadCase());
    }

    @GetMapping("/n-plus-one/solved")
    @Operation(summary = "Show solved N+1 problem", description = "Demonstrates the optimized query strategy.")
    public ResponseEntity<NPlusOneDemoResultDto> demonstrateNPlusOneSolvedCase() {
        return ResponseEntity.ok(nPlusOneDemoService.demonstrateSolvedCase());
    }

    @PostMapping("/transactions/without-transaction")
    @Operation(
            summary = "Demonstrate missing transaction",
            description = "Shows what remains persisted when related changes fail without a transaction."
    )
    public ResponseEntity<TransactionDemoResultDto> demonstrateWithoutTransaction() {
        EntityCountSnapshotDto before = transactionDemoService.snapshot();
        String error;
        try {
            transactionDemoService.saveRelatedEntitiesWithoutTransactionAndFail();
            error = "No error";
        } catch (RuntimeException ex) {
            error = ex.getMessage();
        }
        EntityCountSnapshotDto after = transactionDemoService.snapshot();
        return ResponseEntity.ok(new TransactionDemoResultDto("without_transaction", error, before, after));
    }

    @PostMapping("/transactions/with-transaction")
    @Operation(
            summary = "Demonstrate transactional rollback",
            description = "Shows how transactional rollback prevents partial persistence on failure."
    )
    public ResponseEntity<TransactionDemoResultDto> demonstrateWithTransaction() {
        EntityCountSnapshotDto before = transactionDemoService.snapshot();
        String error;
        try {
            transactionDemoService.saveRelatedEntitiesWithTransactionAndFail();
            error = "No error";
        } catch (RuntimeException ex) {
            error = ex.getMessage();
        }
        EntityCountSnapshotDto after = transactionDemoService.snapshot();
        return ResponseEntity.ok(new TransactionDemoResultDto("with_transaction", error, before, after));
    }

    @PostMapping("/transactions/bulk-bookings/without-transaction")
    @Operation(
            summary = "Demonstrate bulk booking without transaction",
            description = "Shows that part of a bulk booking import may remain persisted when one item fails."
    )
    public ResponseEntity<BulkBookingTransactionDemoResultDto> demonstrateBulkWithoutTransaction(
            @Valid @RequestBody List<@Valid BookingUpsertRequest> requests
    ) {
        EntityCountSnapshotDto before = transactionDemoService.snapshot();
        String error;
        try {
            transactionDemoService.createBookingsBulkWithoutTransaction(requests);
            error = "No error";
        } catch (RuntimeException ex) {
            error = ex.getMessage();
        }
        EntityCountSnapshotDto after = transactionDemoService.snapshot();
        return ResponseEntity.ok(new BulkBookingTransactionDemoResultDto(
                "bulk_without_transaction",
                requests.size(),
                error,
                before,
                after
        ));
    }

    @PostMapping("/transactions/bulk-bookings/with-transaction")
    @Operation(
            summary = "Demonstrate bulk booking with transaction",
            description = "Shows that a failing bulk booking import is fully rolled back when wrapped in a transaction."
    )
    public ResponseEntity<BulkBookingTransactionDemoResultDto> demonstrateBulkWithTransaction(
            @Valid @RequestBody List<@Valid BookingUpsertRequest> requests
    ) {
        EntityCountSnapshotDto before = transactionDemoService.snapshot();
        String error;
        try {
            transactionDemoService.createBookingsBulkWithTransaction(requests);
            error = "No error";
        } catch (RuntimeException ex) {
            error = ex.getMessage();
        }
        EntityCountSnapshotDto after = transactionDemoService.snapshot();
        return ResponseEntity.ok(new BulkBookingTransactionDemoResultDto(
                "bulk_with_transaction",
                requests.size(),
                error,
                before,
                after
        ));
    }
}
