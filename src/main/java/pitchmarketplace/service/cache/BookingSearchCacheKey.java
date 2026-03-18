package pitchmarketplace.service.cache;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;

public final class BookingSearchCacheKey {

    private final String queryType;
    private final String district;
    private final PitchType pitchType;
    private final String organizerName;
    private final BookingStatus status;
    private final LocalDateTime startFrom;
    private final LocalDateTime startTo;
    private final int page;
    private final int size;

    public BookingSearchCacheKey(
            String queryType,
            String district,
            PitchType pitchType,
            String organizerName,
            BookingStatus status,
            LocalDateTime startFrom,
            LocalDateTime startTo,
            int page,
            int size
    ) {
        this.queryType = normalizeText(queryType);
        this.district = normalizeText(district);
        this.pitchType = pitchType;
        this.organizerName = normalizeText(organizerName);
        this.status = status;
        this.startFrom = startFrom;
        this.startTo = startTo;
        this.page = page;
        this.size = size;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BookingSearchCacheKey that)) {
            return false;
        }
        return page == that.page
                && size == that.size
                && Objects.equals(queryType, that.queryType)
                && Objects.equals(district, that.district)
                && pitchType == that.pitchType
                && Objects.equals(organizerName, that.organizerName)
                && status == that.status
                && Objects.equals(startFrom, that.startFrom)
                && Objects.equals(startTo, that.startTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryType, district, pitchType, organizerName, status, startFrom, startTo, page, size);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
