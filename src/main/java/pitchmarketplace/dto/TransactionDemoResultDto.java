package pitchmarketplace.dto;

public record TransactionDemoResultDto(
        String mode,
        String error,
        EntityCountSnapshotDto before,
        EntityCountSnapshotDto after
) {
}
