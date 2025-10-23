package rodionova.dev.reservation_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        try {
            log.info("Called the getReservationById method. Get reservation by id= " + id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservationService.getReservationById(id));
        } catch (NoSuchElementException e) {
            log.error("No found reservation by id= " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        log.info("Called the getAllReservations method");
        return ResponseEntity.ok(reservationService.findAllReservations());
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservationToCreate) {
        log.info("Called the createReservation method. Create reservation: " + reservationToCreate);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("test-header", "123")
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody Reservation reservationToUpdate
    ) {
        log.info("Called the updateReservation method. Update reservation= {}, id = {}",
                reservationToUpdate, reservationToUpdate.id());
        var updated = reservationService.updateReservation(reservationToUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteReservation(@PathVariable Long id) {
        try {
            log.info("Called the deleteReservation method. Delete reservation by id= " + id);
            reservationService.deleteReservation(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (NoSuchElementException e) {
            log.error("No found reservation by id= " + id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable Long id) {
        log.info("Called the approveReservation method. Approve reservation by id= " + id);
        var reservationToApprove = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservationToApprove);
    }
}
