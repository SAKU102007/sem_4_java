package pitchmarketplace.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import pitchmarketplace.domain.enums.UserRole;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer rating;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
    private Set<Booking> organizedBookings = new HashSet<>();

    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
    private Set<OpenGame> organizedOpenGames = new HashSet<>();

    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private Set<OpenGame> participatingOpenGames = new HashSet<>();

    public User() {
    }

    public User(Long id, String name, Integer rating, UserRole role) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Set<Booking> getOrganizedBookings() {
        return organizedBookings;
    }

    public void setOrganizedBookings(Set<Booking> organizedBookings) {
        this.organizedBookings = organizedBookings;
    }

    public Set<OpenGame> getOrganizedOpenGames() {
        return organizedOpenGames;
    }

    public void setOrganizedOpenGames(Set<OpenGame> organizedOpenGames) {
        this.organizedOpenGames = organizedOpenGames;
    }

    public Set<OpenGame> getParticipatingOpenGames() {
        return participatingOpenGames;
    }

    public void setParticipatingOpenGames(Set<OpenGame> participatingOpenGames) {
        this.participatingOpenGames = participatingOpenGames;
    }
}
