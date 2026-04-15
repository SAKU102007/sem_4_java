package pitchmarketplace.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.PitchRepository;
import pitchmarketplace.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PitchRepository pitchRepository;
    private final UserRepository userRepository;
    private final BookingSearchService bookingSearchService;

    public BookingService(
            BookingRepository bookingRepository,
            PitchRepository pitchRepository,
            UserRepository userRepository,
            BookingSearchService bookingSearchService
    ) {
        this.bookingRepository = bookingRepository;
        this.pitchRepository = pitchRepository;
        this.userRepository = userRepository;
        this.bookingSearchService = bookingSearchService;
    }

    @Transactional(readOnly = true)
    public List<BookingDto> findAll() {
        return bookingRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingDto findById(Long id) {
        return toDto(getBookingOrThrow(id));
    }

    @Transactional
    public BookingDto create(BookingUpsertRequest request) {
        Booking booking = new Booking();
        applyRequest(booking, request);
        Booking savedBooking = bookingRepository.save(booking);
        bookingSearchService.invalidateCache();
        return toDto(savedBooking);
    }

    @Transactional
    public BookingDto update(Long id, BookingUpsertRequest request) {
        Booking booking = getBookingOrThrow(id);
        applyRequest(booking, request);
        Booking savedBooking = bookingRepository.save(booking);
        bookingSearchService.invalidateCache();
        return toDto(savedBooking);
    }

    @Transactional
    public void delete(Long id) {
        Booking booking = getBookingOrThrow(id);
        bookingRepository.delete(booking);
        bookingRepository.flush();
        bookingSearchService.invalidateCache();
    }

    @Transactional
    public List<BookingDto> createBulk(List<BookingUpsertRequest> requests) {
        List<BookingDto> createdBookings = normalizeBulkRequests(requests).stream()
                .map(this::saveAndConvert)
                .toList();
        bookingSearchService.invalidateCache();
        return createdBookings;
    }

    public List<BookingDto> createBulkWithoutTransaction(List<BookingUpsertRequest> requests) {
        List<BookingDto> createdBookings = new ArrayList<>();
        try {
            for (BookingUpsertRequest request : normalizeBulkRequests(requests)) {
                createdBookings.add(saveAndConvert(request));
            }
            return List.copyOf(createdBookings);
        } finally {
            if (!createdBookings.isEmpty()) {
                bookingSearchService.invalidateCache();
            }
        }
    }

    private List<BookingUpsertRequest> normalizeBulkRequests(List<BookingUpsertRequest> requests) {
        return Optional.ofNullable(requests)
                .map(List::copyOf)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("At least one booking request is required"));
    }

    private BookingDto saveAndConvert(BookingUpsertRequest request) {
        Booking booking = new Booking();
        applyRequest(booking, request);
        return toDto(bookingRepository.save(booking));
    }

    private void applyRequest(Booking booking, BookingUpsertRequest request) {
        Pitch pitch = pitchRepository.findById(request.pitchId())
                .orElseThrow(() -> new ResourceNotFoundException("Pitch not found. id=" + request.pitchId()));
        User organizer = userRepository.findById(request.organizerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found. id=" + request.organizerId()));
        booking.setPitch(pitch);
        booking.setOrganizer(organizer);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setStatus(request.status());
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found. id=" + id));
    }

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getPitch().getId(),
                booking.getOrganizer().getId(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getStatus()
        );
    }
}
