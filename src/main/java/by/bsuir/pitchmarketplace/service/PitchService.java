package by.bsuir.pitchmarketplace.service;

import by.bsuir.pitchmarketplace.domain.entity.EquipmentOffer;
import by.bsuir.pitchmarketplace.domain.entity.Pitch;
import by.bsuir.pitchmarketplace.domain.entity.User;
import by.bsuir.pitchmarketplace.domain.enums.BookingStatus;
import by.bsuir.pitchmarketplace.domain.enums.EquipmentItemType;
import by.bsuir.pitchmarketplace.domain.enums.SortOption;
import by.bsuir.pitchmarketplace.dto.InventoryAvailabilityResponse;
import by.bsuir.pitchmarketplace.dto.PitchDetailsResponse;
import by.bsuir.pitchmarketplace.dto.PitchSearchRequest;
import by.bsuir.pitchmarketplace.dto.PitchSearchResponse;
import by.bsuir.pitchmarketplace.exception.BadRequestException;
import by.bsuir.pitchmarketplace.exception.NotFoundException;
import by.bsuir.pitchmarketplace.mapper.PitchMapper;
import by.bsuir.pitchmarketplace.repository.BookingRepository;
import by.bsuir.pitchmarketplace.repository.PitchRepository;
import by.bsuir.pitchmarketplace.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PitchService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(BookingStatus.LOCKED, BookingStatus.CONFIRMED);

    private final PitchRepository pitchRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PitchMapper pitchMapper;

    public PitchService(
            PitchRepository pitchRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            PitchMapper pitchMapper
    ) {
        this.pitchRepository = pitchRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.pitchMapper = pitchMapper;
    }

    public List<PitchSearchResponse> search(PitchSearchRequest request, Long requesterUserId) {
        validateRequest(request);
        SortOption sortOption = parseSort(request.getSort());
        User requester = resolveRequester(sortOption, requesterUserId);

        List<PitchCandidate> candidates = pitchRepository.findByActiveTrue().stream()
                .filter(pitch -> matchesPitchFilters(pitch, request))
                .filter(pitch -> matchesSkillFilter(pitch, request.getSkillMin(), request.getSkillMax()))
                .map(pitch -> toCandidate(pitch, request, requester))
                .filter(candidate -> candidate != null)
                .sorted(comparatorFor(sortOption))
                .toList();

        return candidates.stream()
                .map(candidate -> pitchMapper.toSearchResponse(
                        candidate.pitch(),
                        candidate.skillDistanceToMe(),
                        candidate.distanceKm(),
                        candidate.inventory()
                ))
                .toList();
    }

    public PitchDetailsResponse getById(Long id) {
        Pitch pitch = pitchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pitch with id %d was not found".formatted(id)));
        return pitchMapper.toDetailsResponse(pitch);
    }

    private PitchCandidate toCandidate(Pitch pitch, PitchSearchRequest request, User requester) {
        if (!matchesAvailabilityFilter(pitch, request.getDesiredStartAt(), request.getDesiredEndAt())) {
            return null;
        }

        Double distanceKm = calculateDistanceKm(request.getLat(), request.getLng(), pitch.getLat(), pitch.getLng());
        if (request.getRadiusKm() != null && distanceKm != null && distanceKm > request.getRadiusKm()) {
            return null;
        }

        InventoryAvailabilityResponse inventory = buildInventory(pitch, request);
        if (Boolean.TRUE.equals(request.getNeedInventory()) && !inventory.hasRequestedEquipment()) {
            return null;
        }

        Double skillDistanceToMe = null;
        if (requester != null) {
            skillDistanceToMe = Math.abs(requester.getRating() - pitch.getAverageSkill());
        }

        return new PitchCandidate(pitch, distanceKm, skillDistanceToMe, inventory);
    }

    private InventoryAvailabilityResponse buildInventory(Pitch pitch, PitchSearchRequest request) {
        Map<EquipmentItemType, EquipmentOffer> offers = new EnumMap<>(EquipmentItemType.class);
        for (EquipmentOffer offer : pitch.getEquipmentOffers()) {
            offers.put(offer.getItemType(), offer);
        }

        EquipmentOffer ballsOffer = offers.get(EquipmentItemType.BALL);
        EquipmentOffer bibsOffer = offers.get(EquipmentItemType.BIBS);

        int ballsAvailable = ballsOffer == null ? 0 : ballsOffer.getStockTotal();
        int bibsAvailable = bibsOffer == null ? 0 : bibsOffer.getStockTotal();

        int requestedBalls = request.getBallQty() == null ? 0 : request.getBallQty();
        int requestedBibs = request.getBibsQty() == null ? 0 : request.getBibsQty();

        boolean hasRequested = ballsAvailable >= requestedBalls && bibsAvailable >= requestedBibs;

        return new InventoryAvailabilityResponse(
                hasRequested,
                ballsAvailable,
                bibsAvailable,
                ballsOffer == null ? null : ballsOffer.getRentFixedPrice(),
                bibsOffer == null ? null : bibsOffer.getRentFixedPrice()
        );
    }

    private boolean matchesPitchFilters(Pitch pitch, PitchSearchRequest request) {
        if (request.getPitchType() != null && request.getPitchType() != pitch.getType()) {
            return false;
        }
        if (request.getPriceFrom() != null && pitch.getPricePerHour().compareTo(request.getPriceFrom()) < 0) {
            return false;
        }
        if (request.getPriceTo() != null && pitch.getPricePerHour().compareTo(request.getPriceTo()) > 0) {
            return false;
        }
        boolean matchesDistrict = request.getDistrict() == null
                || request.getDistrict().isBlank()
                || pitch.getDistrict().equalsIgnoreCase(request.getDistrict().trim());
        boolean matchesMetro = request.getMetro() == null
                || request.getMetro().isBlank()
                || pitch.getMetro().equalsIgnoreCase(request.getMetro().trim());
        return matchesDistrict && matchesMetro;
    }

    private boolean matchesSkillFilter(Pitch pitch, Integer skillMin, Integer skillMax) {
        return (skillMin == null || pitch.getAverageSkill() >= skillMin)
                && (skillMax == null || pitch.getAverageSkill() <= skillMax);
    }

    private boolean matchesAvailabilityFilter(Pitch pitch, LocalDateTime desiredStartAt, LocalDateTime desiredEndAt) {
        if (desiredStartAt == null || desiredEndAt == null) {
            return true;
        }
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                pitch.getId(),
                desiredStartAt,
                desiredEndAt,
                BLOCKING_STATUSES
        );
        return !hasOverlap;
    }

    private Comparator<PitchCandidate> comparatorFor(SortOption sortOption) {
        return switch (sortOption) {
            case PRICE_ASC -> Comparator.comparing(candidate -> candidate.pitch().getPricePerHour());
            case PRICE_DESC -> Comparator.comparing((PitchCandidate candidate) -> candidate.pitch().getPricePerHour()).reversed();
            case AVG_SKILL_ASC -> Comparator.comparing(candidate -> candidate.pitch().getAverageSkill());
            case AVG_SKILL_DESC -> Comparator.comparing((PitchCandidate candidate) -> candidate.pitch().getAverageSkill()).reversed();
            case DISTANCE_TO_ME_ASC -> Comparator.comparing(PitchCandidate::skillDistanceToMe, Comparator.nullsLast(Double::compareTo));
            case DISTANCE_TO_ME_DESC -> Comparator.comparing(PitchCandidate::skillDistanceToMe, Comparator.nullsLast(Double::compareTo)).reversed();
        };
    }

    private User resolveRequester(SortOption sortOption, Long requesterUserId) {
        if (sortOption == SortOption.DISTANCE_TO_ME_ASC || sortOption == SortOption.DISTANCE_TO_ME_DESC) {
            if (requesterUserId == null) {
                throw new BadRequestException("X-User-Id header is required for distance_to_me sorting");
            }
            return userRepository.findById(requesterUserId)
                    .orElseThrow(() -> new NotFoundException("User with id %d was not found".formatted(requesterUserId)));
        }
        return null;
    }

    private SortOption parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return SortOption.PRICE_ASC;
        }
        try {
            return SortOption.valueOf(sort.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported sort option: " + sort);
        }
    }

    private void validateRequest(PitchSearchRequest request) {
        validatePrice(request.getPriceFrom(), request.getPriceTo());
        validateDuration(request.getDurationMinutes());
        validateSkill(request.getSkillMin(), request.getSkillMax());
        validateTime(request.getDesiredStartAt(), request.getDesiredEndAt(), request.getDurationMinutes());
        validateGeo(request.getLat(), request.getLng(), request.getRadiusKm());
        validateInventory(request.getBallQty(), request.getBibsQty());
    }

    private void validatePrice(BigDecimal priceFrom, BigDecimal priceTo) {
        if (priceFrom != null && priceTo != null && priceFrom.compareTo(priceTo) > 0) {
            throw new BadRequestException("priceFrom must be less than or equal to priceTo");
        }
    }

    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes == null) {
            return;
        }
        if (durationMinutes < 60 || durationMinutes > 180 || durationMinutes % 30 != 0) {
            throw new BadRequestException("durationMinutes must be between 60 and 180 with 30-minute step");
        }
    }

    private void validateSkill(Integer skillMin, Integer skillMax) {
        if (skillMin != null && (skillMin < 1 || skillMin > 100)) {
            throw new BadRequestException("skillMin must be between 1 and 100");
        }
        if (skillMax != null && (skillMax < 1 || skillMax > 100)) {
            throw new BadRequestException("skillMax must be between 1 and 100");
        }
        if (skillMin != null && skillMax != null && skillMin > skillMax) {
            throw new BadRequestException("skillMin must be less than or equal to skillMax");
        }
    }

    private void validateTime(LocalDateTime desiredStartAt, LocalDateTime desiredEndAt, Integer durationMinutes) {
        if ((desiredStartAt == null && desiredEndAt != null) || (desiredStartAt != null && desiredEndAt == null)) {
            throw new BadRequestException("desiredStartAt and desiredEndAt must be provided together");
        }
        if (desiredStartAt == null) {
            return;
        }
        if (!desiredStartAt.isBefore(desiredEndAt)) {
            throw new BadRequestException("desiredStartAt must be before desiredEndAt");
        }
        long minutes = Duration.between(desiredStartAt, desiredEndAt).toMinutes();
        if (minutes < 60 || minutes > 180 || minutes % 30 != 0) {
            throw new BadRequestException("Time window must be between 60 and 180 minutes with 30-minute step");
        }
        if (durationMinutes != null && minutes != durationMinutes) {
            throw new BadRequestException("durationMinutes must match desired time window length");
        }
    }

    private void validateGeo(Double lat, Double lng, Double radiusKm) {
        if (radiusKm != null) {
            if (lat == null || lng == null) {
                throw new BadRequestException("lat and lng are required when radiusKm is provided");
            }
            if (radiusKm <= 0) {
                throw new BadRequestException("radiusKm must be greater than 0");
            }
        }
    }

    private void validateInventory(Integer ballQty, Integer bibsQty) {
        if (ballQty != null && ballQty < 0) {
            throw new BadRequestException("ballQty must be greater than or equal to 0");
        }
        if (bibsQty != null && bibsQty < 0) {
            throw new BadRequestException("bibsQty must be greater than or equal to 0");
        }
    }

    private Double calculateDistanceKm(Double fromLat, Double fromLng, Double toLat, Double toLng) {
        if (fromLat == null || fromLng == null || toLat == null || toLng == null) {
            return null;
        }
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(toLat - fromLat);
        double dLng = Math.toRadians(toLng - fromLng);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private record PitchCandidate(
            Pitch pitch,
            Double distanceKm,
            Double skillDistanceToMe,
            InventoryAvailabilityResponse inventory
    ) {
    }
}
