package rodionova.dev.reservation_system.web;

import java.time.LocalDateTime;

public record ErrorResposeDTO(
        String message,
        String detailedMessage,
        LocalDateTime errorTime
) {

}
