package rodionova.dev.reservation_system.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResposeDTO> handleGenericException(Exception ex) {
        log.error("Handle internal server error exception", ex);

        var errorDto = new ErrorResposeDTO("Interal Server Error",
                ex.getMessage(), LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(exception = {EntityNotFoundException.class})
    public ResponseEntity<ErrorResposeDTO> handleEntityNotFoundException(
            Exception e
    ) {
        log.error("Handle entity not found exception", e);
        var errorDto = new ErrorResposeDTO("Entity ot found",
                e.getMessage(), LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(exception = {IllegalArgumentException.class,
            IllegalStateException.class,
    MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResposeDTO> handleIllegalArgumentException(Exception e) {
        log.error("Handle illegal argument exception", e);
        var errorDto = new ErrorResposeDTO("Bad request",
                e.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}

