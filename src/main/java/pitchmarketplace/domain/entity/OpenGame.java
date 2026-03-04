package pitchmarketplace.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import pitchmarketplace.domain.enums.OpenGameStatus;

@Entity
@Table(name = "open_games")
public class OpenGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    private Integer targetSkillMin;

    private Integer targetSkillMax;

    private Integer maxPlayers;

    @Enumerated(EnumType.STRING)
    private OpenGameStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "open_game_participants",
            joinColumns = @JoinColumn(name = "open_game_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    public OpenGame() {
    }

    public OpenGame(
            Long id,
            Booking booking,
            User organizer,
            Integer targetSkillMin,
            Integer targetSkillMax,
            Integer maxPlayers,
            OpenGameStatus status
    ) {
        this.id = id;
        this.booking = booking;
        this.organizer = organizer;
        this.targetSkillMin = targetSkillMin;
        this.targetSkillMax = targetSkillMax;
        this.maxPlayers = maxPlayers;
        this.status = status;
    }

    public void addParticipant(User user) {
        participants.add(user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public Integer getTargetSkillMin() {
        return targetSkillMin;
    }

    public void setTargetSkillMin(Integer targetSkillMin) {
        this.targetSkillMin = targetSkillMin;
    }

    public Integer getTargetSkillMax() {
        return targetSkillMax;
    }

    public void setTargetSkillMax(Integer targetSkillMax) {
        this.targetSkillMax = targetSkillMax;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public OpenGameStatus getStatus() {
        return status;
    }

    public void setStatus(OpenGameStatus status) {
        this.status = status;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }
}
