package pitchmarketplace.dto;

public record EntityCountSnapshotDto(
        long users,
        long pitches,
        long bookings,
        long openGames,
        long equipmentOffers
) {
}
