package pitchmarketplace.service;

import java.util.List;
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
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PitchRepository pitchRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            PitchRepository pitchRepository,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.pitchRepository = pitchRepository;
        this.userRepository = userRepository;
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

    public BookingDto create(BookingUpsertRequest request) {
        Booking booking = new Booking();
        applyRequest(booking, request);
        return toDto(bookingRepository.save(booking));
    }

    public BookingDto update(Long id, BookingUpsertRequest request) {
        Booking booking = getBookingOrThrow(id);
        applyRequest(booking, request);
        return toDto(bookingRepository.save(booking));
    }

    public void delete(Long id) {
        Booking booking = getBookingOrThrow(id);
        bookingRepository.delete(booking);
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
