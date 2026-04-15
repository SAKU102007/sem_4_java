package pitchmarketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingSearchRequest;
import pitchmarketplace.dto.BookingSearchResponseDto;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.service.BookingSearchService;
import pitchmarketplace.service.BookingService;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Operations for bookings and booking search")
public class BookingController {

    private final BookingService service;
    private final BookingSearchService bookingSearchService;

    public BookingController(BookingService service, BookingSearchService bookingSearchService) {
        this.service = service;
        this.bookingSearchService = bookingSearchService;
    }

    @GetMapping
    @Operation(summary = "Get all bookings", description = "Returns every booking available in the system.")
    public ResponseEntity<List<BookingDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/search/jpql")
    @Operation(
            summary = "Search bookings with JPQL",
            description = "Searches bookings using JPQL filters and paginated results."
    )
    public ResponseEntity<BookingSearchResponseDto> searchWithJpql(
            @ParameterObject @Valid @ModelAttribute BookingSearchRequest request
    ) {
        return ResponseEntity.ok(bookingSearchService.searchWithJpql(
                request.toCriteria(),
                request.getPage(),
                request.getSize()
        ));
    }

    @GetMapping("/search/native")
    @Operation(
            summary = "Search bookings with native SQL",
            description = "Searches bookings using native SQL filters and paginated results."
    )
    public ResponseEntity<BookingSearchResponseDto> searchWithNative(
            @ParameterObject @Valid @ModelAttribute BookingSearchRequest request
    ) {
        return ResponseEntity.ok(bookingSearchService.searchWithNative(
                request.toCriteria(),
                request.getPage(),
                request.getSize()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by id", description = "Returns a booking by its identifier.")
    public ResponseEntity<BookingDto> getById(@PathVariable @Positive(message = "id must be positive") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create booking", description = "Creates a booking from the provided request body.")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody BookingUpsertRequest request) {
        BookingDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + created.id())).body(created);
    }

    @PostMapping("/bulk")
    @Operation(
            summary = "Create bookings in bulk",
            description = "Creates multiple bookings in one request and saves them atomically."
    )
    public ResponseEntity<List<BookingDto>> createBulk(
            @Valid @RequestBody List<@Valid BookingUpsertRequest> requests
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBulk(requests));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update booking", description = "Updates an existing booking by identifier.")
    public ResponseEntity<BookingDto> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody BookingUpsertRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete booking", description = "Deletes a booking by identifier.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id must be positive") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
