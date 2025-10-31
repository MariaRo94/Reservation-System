package rodionova.dev.reservation_system.reservations.availability;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations/availability")
public class ReservationAvailabilityController {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    private final ReservationAvalabilityService reservationAvalabilityService;

    public ReservationAvailabilityController(ReservationAvalabilityService reservationAvalabilityService) {
        this.reservationAvalabilityService = reservationAvalabilityService;
    }

    ;

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailabilityResponse(
            @Valid CheckAvailabilityRequest checkAvailabilityRequest) {
        log.info("Called checkAvailabilityResponse method");
        boolean isAvailable = reservationAvalabilityService.isReservationAvailable(
                checkAvailabilityRequest.roomId(),
                checkAvailabilityRequest.startDate(),
                checkAvailabilityRequest.endDate());


        var message = isAvailable ? "Available" : "Not available";

        var status = isAvailable ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.RESERVED;

        return ResponseEntity.ok(new CheckAvailabilityResponse(message, status));

    }
}