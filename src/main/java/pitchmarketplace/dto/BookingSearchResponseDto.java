package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated booking search result")
public record BookingSearchResponseDto(
        @Schema(description = "Query implementation used to build the result", example = "jpql")
        String queryType,
        @Schema(description = "Indicates whether the response came from cache", example = "false")
        boolean cacheHit,
        @Schema(description = "Current page number", example = "0")
        int pageNumber,
        @Schema(description = "Current page size", example = "5")
        int pageSize,
        @Schema(description = "Total matching elements", example = "12")
        long totalElements,
        @Schema(description = "Total number of pages", example = "3")
        int totalPages,
        @Schema(description = "Whether the current page is the first one", example = "true")
        boolean first,
        @Schema(description = "Whether the current page is the last one", example = "false")
        boolean last,
        @Schema(description = "Page content")
        List<BookingDto> content
) {

    public BookingSearchResponseDto markCacheHit() {
        return new BookingSearchResponseDto(
                queryType,
                true,
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                first,
                last,
                content
        );
    }
}
