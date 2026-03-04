package pitchmarketplace.dto;

public record NPlusOneDemoResultDto(
        String mode,
        int openGamesCount,
        long totalParticipants,
        long executedStatements
) {
}
