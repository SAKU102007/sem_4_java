package pitchmarketplace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pitchmarketplace.dto.EntityCountSnapshotDto;
import pitchmarketplace.dto.NPlusOneDemoResultDto;
import pitchmarketplace.dto.TransactionDemoResultDto;
import pitchmarketplace.service.NPlusOneDemoService;
import pitchmarketplace.service.TransactionDemoService;

@RestController
@RequestMapping("/api/v1/demos")
public class DemoController {

    private final NPlusOneDemoService nPlusOneDemoService;
    private final TransactionDemoService transactionDemoService;

    public DemoController(NPlusOneDemoService nPlusOneDemoService, TransactionDemoService transactionDemoService) {
        this.nPlusOneDemoService = nPlusOneDemoService;
        this.transactionDemoService = transactionDemoService;
    }

    @GetMapping("/n-plus-one/bad")
    public ResponseEntity<NPlusOneDemoResultDto> demonstrateNPlusOneBadCase() {
        return ResponseEntity.ok(nPlusOneDemoService.demonstrateBadCase());
    }

    @GetMapping("/n-plus-one/solved")
    public ResponseEntity<NPlusOneDemoResultDto> demonstrateNPlusOneSolvedCase() {
        return ResponseEntity.ok(nPlusOneDemoService.demonstrateSolvedCase());
    }

    @PostMapping("/transactions/without-transaction")
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
}
