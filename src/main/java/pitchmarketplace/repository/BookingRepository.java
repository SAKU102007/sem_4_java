package pitchmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pitchmarketplace.domain.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
