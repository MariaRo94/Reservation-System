package rodionova.dev.reservation_system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rodionova.dev.reservation_system.reservations.Reservation;
import rodionova.dev.reservation_system.reservations.ReservationController;
import rodionova.dev.reservation_system.reservations.ReservationService;
import rodionova.dev.reservation_system.reservations.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private final Reservation testReservation = new Reservation(
            1L, 100L, 200L,
            LocalDate.of(2025, 10, 26),
            LocalDate.of(2025, 10, 28),
            ReservationStatus.PENDING
    );

    @Test
    void getReservationById_ShouldReturnReservation_WhenExists() {
        // Arrange
        when(reservationService.getReservationById(1L)).thenReturn(testReservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.getReservationById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testReservation, response.getBody());
        verify(reservationService).getReservationById(1L);
    }

    @Test
    void getReservationById_ShouldReturnNotFound_WhenNotExists() {
        // Arrange
        when(reservationService.getReservationById(999L))
                .thenThrow(new NoSuchElementException());

        // Act
        ResponseEntity<Reservation> response = reservationController.getReservationById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAllReservations_ShouldReturnAllReservations() {
        // Arrange
        List<Reservation> reservations = List.of(testReservation);
        when(reservationService.findAllReservations()).thenReturn(reservations);

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getAllReservations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservations, response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void createReservation_ShouldCreateAndReturnReservation() {
        // Arrange
        Reservation newReservation = new Reservation(
                null, 100L, 200L,
                LocalDate.of(2025, 10, 26),
                LocalDate.of(2025, 10, 28),
                null
        );

        when(reservationService.createReservation(newReservation)).thenReturn(testReservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.createReservation(newReservation);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testReservation, response.getBody());
        assertEquals("123", response.getHeaders().getFirst("test-header"));
        verify(reservationService).createReservation(newReservation);
    }

    @Test
    void updateReservation_ShouldUpdateAndReturnReservation() {
        // Arrange
        Reservation updatedReservation = new Reservation(
                1L, 100L, 200L,
                LocalDate.of(2025, 10, 27),
                LocalDate.of(2025, 10, 29),
                ReservationStatus.PENDING
        );

        when(reservationService.updateReservation(1L, updatedReservation)).thenReturn(updatedReservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.updateReservation(1L, updatedReservation);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedReservation, response.getBody());
        verify(reservationService).updateReservation(1L, updatedReservation);
    }

    @Test
    void deleteReservation_ShouldReturnNoContent_WhenExists() {
        // Arrange
        doNothing().when(reservationService).deleteReservation(1L);

        // Act
        ResponseEntity<Void> response = reservationController.deleteReservation(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reservationService).deleteReservation(1L);
    }

    @Test
    void deleteReservation_ShouldReturnNotFound_WhenNotExists() {
        // Arrange
        doThrow(new NoSuchElementException()).when(reservationService).deleteReservation(999L);

        // Act
        ResponseEntity<Void> response = reservationController.deleteReservation(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void approveReservation_ShouldApproveAndReturnReservation() {
        // Arrange
        Reservation approvedReservation = new Reservation(
                1L, 100L, 200L,
                LocalDate.of(2025, 10, 26),
                LocalDate.of(2025, 10, 28),
                ReservationStatus.APPROVED
        );

        when(reservationService.approveReservation(1L)).thenReturn(approvedReservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.approveReservation(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(approvedReservation, response.getBody());
        assertEquals(ReservationStatus.APPROVED, response.getBody().status());
    }
}