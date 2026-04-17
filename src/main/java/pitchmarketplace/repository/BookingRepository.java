package pitchmarketplace.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.PitchType;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByPitch_Id(Long pitchId);

    long countByPitch_IdAndStatus(Long pitchId, BookingStatus status);

    @Query(
            value = """
                    select b
                    from Booking b
                    join b.pitch p
                    join b.organizer o
                    where lower(p.district) = lower(coalesce(:district, p.district))
                      and p.type = coalesce(:pitchType, p.type)
                      and lower(o.name) like lower(concat('%', coalesce(:organizerName, o.name), '%'))
                      and b.status = coalesce(:status, b.status)
                      and b.startAt >= coalesce(:startFrom, b.startAt)
                      and b.startAt <= coalesce(:startTo, b.startAt)
                    order by b.startAt asc, b.id asc
                    """,
            countQuery = """
                    select count(b)
                    from Booking b
                    join b.pitch p
                    join b.organizer o
                    where lower(p.district) = lower(coalesce(:district, p.district))
                      and p.type = coalesce(:pitchType, p.type)
                      and lower(o.name) like lower(concat('%', coalesce(:organizerName, o.name), '%'))
                      and b.status = coalesce(:status, b.status)
                      and b.startAt >= coalesce(:startFrom, b.startAt)
                      and b.startAt <= coalesce(:startTo, b.startAt)
                    """
    )
    Page<Booking> searchWithFiltersJpql(
            @Param("district") String district,
            @Param("pitchType") PitchType pitchType,
            @Param("organizerName") String organizerName,
            @Param("status") BookingStatus status,
            @Param("startFrom") LocalDateTime startFrom,
            @Param("startTo") LocalDateTime startTo,
            Pageable pageable
    );

    @Query(
            value = """
                    select b.*
                    from bookings b
                    join pitches p on p.id = b.pitch_id
                    join users u on u.id = b.organizer_id
                    where lower(p.district) = lower(coalesce(:district, p.district))
                      and p.type = coalesce(:pitchType, p.type)
                      and lower(u.name) like lower(concat('%', coalesce(:organizerName, u.name), '%'))
                      and b.status = coalesce(:status, b.status)
                      and b.start_at >= coalesce(:startFrom, b.start_at)
                      and b.start_at <= coalesce(:startTo, b.start_at)
                    order by b.start_at asc, b.id asc
                    """,
            countQuery = """
                    select count(*)
                    from bookings b
                    join pitches p on p.id = b.pitch_id
                    join users u on u.id = b.organizer_id
                    where lower(p.district) = lower(coalesce(:district, p.district))
                      and p.type = coalesce(:pitchType, p.type)
                      and lower(u.name) like lower(concat('%', coalesce(:organizerName, u.name), '%'))
                      and b.status = coalesce(:status, b.status)
                      and b.start_at >= coalesce(:startFrom, b.start_at)
                      and b.start_at <= coalesce(:startTo, b.start_at)
                    """,
            nativeQuery = true
    )
    Page<Booking> searchWithFiltersNative(
            @Param("district") String district,
            @Param("pitchType") String pitchType,
            @Param("organizerName") String organizerName,
            @Param("status") String status,
            @Param("startFrom") LocalDateTime startFrom,
            @Param("startTo") LocalDateTime startTo,
            Pageable pageable
    );
}
