package pitchmarketplace.service.cache;

import java.util.Objects;
import pitchmarketplace.dto.BookingSearchCriteria;

public final class BookingSearchCacheKey {

    private final String queryType;
    private final BookingSearchCriteria criteria;
    private final int page;
    private final int size;

    public BookingSearchCacheKey(
            String queryType,
            BookingSearchCriteria criteria,
            int page,
            int size
    ) {
        this.queryType = queryType;
        this.criteria = criteria;
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
                && Objects.equals(criteria, that.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryType, criteria, page, size);
    }
}
