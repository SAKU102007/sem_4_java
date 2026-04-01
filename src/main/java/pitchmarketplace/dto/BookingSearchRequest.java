package pitchmarketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;

@Schema(description = "Query parameters for booking search endpoints")
public class BookingSearchRequest {

    @Schema(description = "Optional district filter", example = "central")
    @Size(max = 100, message = "district must be at most 100 characters")
    private String district;

    @Schema(description = "Optional pitch type filter")
    private PitchType pitchType;

    @Schema(description = "Optional organizer name filter", example = "Алексей")
    @Size(max = 100, message = "organizerName must be at most 100 characters")
    private String organizerName;

    @Schema(description = "Optional booking status filter")
    private BookingStatus status;

    @Schema(description = "Lower boundary for booking start time", example = "2026-05-01T18:00:00")
    private LocalDateTime startFrom;

    @Schema(description = "Upper boundary for booking start time", example = "2026-05-01T20:00:00")
    private LocalDateTime startTo;

    @Schema(description = "Page number", example = "0", defaultValue = "0")
    @PositiveOrZero(message = "page must be zero or positive")
    private Integer page = 0;

    @Schema(description = "Page size", example = "5", defaultValue = "5")
    @Min(value = 1, message = "size must be at least 1")
    @Max(value = 50, message = "size must be at most 50")
    private Integer size = 5;

    @AssertTrue(message = "startTo must be after or equal to startFrom")
    public boolean isDateRangeValid() {
        if (startFrom == null || startTo == null) {
            return true;
        }
        return !startTo.isBefore(startFrom);
    }

    public BookingSearchCriteria toCriteria() {
        return new BookingSearchCriteria(district, pitchType, organizerName, status, startFrom, startTo);
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public PitchType getPitchType() {
        return pitchType;
    }

    public void setPitchType(PitchType pitchType) {
        this.pitchType = pitchType;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(LocalDateTime startFrom) {
        this.startFrom = startFrom;
    }

    public LocalDateTime getStartTo() {
        return startTo;
    }

    public void setStartTo(LocalDateTime startTo) {
        this.startTo = startTo;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
