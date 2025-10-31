package rodionova.dev.reservation_system.reservations;

import org.springframework.stereotype.Component;


public record ReservationSearchFilter(
        Long roomId,
        Long userId,
        Integer pageSize,
        Integer pageNumber
) {

}
