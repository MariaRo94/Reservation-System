package rodionova.dev.reservation_system.reservations.availability;

import org.springframework.boot.availability.AvailabilityState;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus availabilityStatus
) {
}
