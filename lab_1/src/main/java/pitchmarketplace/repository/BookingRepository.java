package pitchmarketplace.repository;

import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.enums.BookingStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select case when count(b) > 0 then true else false end
            from Booking b
            where b.pitch.id = :pitchId
              and b.status in :statuses
              and b.startAt < :endAt
              and b.endAt > :startAt
            """)
    boolean existsOverlappingBooking(
            @Param("pitchId") Long pitchId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("statuses") Collection<BookingStatus> statuses
    );
}
