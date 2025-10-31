package rodionova.dev.reservation_system.reservations;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
//    @Query(value = "SELECT * FROM reservation WHERE status = :status", nativeQuery = true)
//    List<ReservationEntity> findAllByStatusIs(ReservationStatus status);

    @Transactional
    @Modifying
    @Query("""
            update ReservationEntity r
            set
            r.status = :status
            where r.id = :id
            """)
    void setStatus(@Param("id") Long id, @Param("status") ReservationStatus reservationStatus);

    @Modifying
    @Query("UPDATE ReservationEntity r SET r.status = :status WHERE r.id = :id")
    void updateStatusById(@Param("id") Long id, @Param("status") ReservationStatus status);

    @Query("""
            SELECT r.id FROM ReservationEntity r
            WHERE r.roomId = :roomId
            AND r.startDate <= :endDate
            AND r.endDate >= :startDate
            AND r.status = :status
            """)
    List<Long> findConflictReservations(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
        @Param("status") ReservationStatus status);

    @Query("""
            SELECT r FROM ReservationEntity r
            WHERE (:roomId IS NULL OR r.roomId = :roomId)
            AND (:r.userId IS NULL OR r.userId = :userId
            """)
    List<ReservationEntity> searchAllByFilter(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            Pageable pageable
    );
}
