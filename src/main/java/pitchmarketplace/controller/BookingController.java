package pitchmarketplace.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingSearchResponseDto;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.service.BookingSearchService;
import pitchmarketplace.service.BookingService;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService service;
    private final BookingSearchService bookingSearchService;

    public BookingController(BookingService service, BookingSearchService bookingSearchService) {
        this.service = service;
        this.bookingSearchService = bookingSearchService;
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/search/jpql")
    public ResponseEntity<BookingSearchResponseDto> searchWithJpql(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) PitchType pitchType,
            @RequestParam(required = false) String organizerName,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size
    ) {
        return ResponseEntity.ok(bookingSearchService.searchWithJpql(
                district,
                pitchType,
                organizerName,
                status,
                startFrom,
                startTo,
                page,
                size
        ));
    }

    @GetMapping("/search/native")
    public ResponseEntity<BookingSearchResponseDto> searchWithNative(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) PitchType pitchType,
            @RequestParam(required = false) String organizerName,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size
    ) {
        return ResponseEntity.ok(bookingSearchService.searchWithNative(
                district,
                pitchType,
                organizerName,
                status,
                startFrom,
                startTo,
                page,
                size
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookingDto> create(@RequestBody BookingUpsertRequest request) {
        BookingDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDto> update(@PathVariable Long id, @RequestBody BookingUpsertRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
