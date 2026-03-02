package by.bsuir.pitchmarketplace.repository;

import by.bsuir.pitchmarketplace.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
