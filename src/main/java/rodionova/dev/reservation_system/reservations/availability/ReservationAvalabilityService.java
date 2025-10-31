package rodionova.dev.reservation_system.reservations.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rodionova.dev.reservation_system.reservations.ReservationRepository;
import rodionova.dev.reservation_system.reservations.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvalabilityService {

    private final ReservationRepository reservationRepository;
    private final Logger log = LoggerFactory.getLogger(ReservationAvalabilityService.class);

    public ReservationAvalabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;


    }

    public boolean isReservationAvailable(Long roomId, LocalDate startDate, LocalDate endDate) {

        List<Long> conflictIds = reservationRepository
                .findConflictReservations(roomId, startDate, endDate, ReservationStatus.APPROVED);
        if (conflictIds.isEmpty()) {
            return true;
        } else {
            log.info("Conflicting with ids=" + conflictIds);
            return false;
        }
    }
}
