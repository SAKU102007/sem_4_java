package pitchmarketplace.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.OpenGame;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.dto.OpenGameDto;
import pitchmarketplace.dto.OpenGameUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.BookingRepository;
import pitchmarketplace.repository.OpenGameRepository;
import pitchmarketplace.repository.UserRepository;

@Service
@Transactional
public class OpenGameService {

    private final OpenGameRepository openGameRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public OpenGameService(
            OpenGameRepository openGameRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository
    ) {
        this.openGameRepository = openGameRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OpenGameDto> findAll() {
        return openGameRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OpenGameDto findById(Long id) {
        return toDto(getOpenGameOrThrow(id));
    }

    public OpenGameDto create(OpenGameUpsertRequest request) {
        OpenGame openGame = new OpenGame();
        applyRequest(openGame, request);
        return toDto(openGameRepository.save(openGame));
    }

    public OpenGameDto update(Long id, OpenGameUpsertRequest request) {
        OpenGame openGame = getOpenGameOrThrow(id);
        applyRequest(openGame, request);
        return toDto(openGameRepository.save(openGame));
    }

    public void delete(Long id) {
        OpenGame openGame = getOpenGameOrThrow(id);
        openGameRepository.delete(openGame);
    }

    private void applyRequest(OpenGame openGame, OpenGameUpsertRequest request) {
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found. id=" + request.bookingId()));
        User organizer = userRepository.findById(request.organizerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found. id=" + request.organizerId()));

        Set<User> participants = new LinkedHashSet<>();
        List<Long> participantIds = request.participantIds() == null ? List.of() : request.participantIds();
        for (Long participantId : participantIds) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found. id=" + participantId));
            participants.add(participant);
        }

        openGame.setBooking(booking);
        openGame.setOrganizer(organizer);
        openGame.setTargetSkillMin(request.targetSkillMin());
        openGame.setTargetSkillMax(request.targetSkillMax());
        openGame.setMaxPlayers(request.maxPlayers());
        openGame.setStatus(request.status());
        openGame.setParticipants(participants);
    }

    private OpenGame getOpenGameOrThrow(Long id) {
        return openGameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Open game not found. id=" + id));
    }

    private OpenGameDto toDto(OpenGame openGame) {
        List<Long> participantIds = openGame.getParticipants().stream()
                .map(User::getId)
                .sorted()
                .toList();
        return new OpenGameDto(
                openGame.getId(),
                openGame.getBooking().getId(),
                openGame.getOrganizer().getId(),
                openGame.getTargetSkillMin(),
                openGame.getTargetSkillMax(),
                openGame.getMaxPlayers(),
                openGame.getStatus(),
                participantIds
        );
    }
}
