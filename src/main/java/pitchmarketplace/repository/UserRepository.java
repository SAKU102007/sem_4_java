package pitchmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pitchmarketplace.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
