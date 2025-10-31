package rodionova.dev.reservation_system;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rodionova.dev.reservation_system.reservations.Reservation;
import rodionova.dev.reservation_system.reservations.ReservationEntity;
import rodionova.dev.reservation_system.reservations.ReservationRepository;
import rodionova.dev.reservation_system.reservations.ReservationService;
import rodionova.dev.reservation_system.reservations.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private final LocalDate startDate = LocalDate.of(2025, 10, 26);
    private final LocalDate endDate = LocalDate.of(2025, 10, 28);

    private ReservationEntity createTestEntity(Long id, ReservationStatus status) {
        return new ReservationEntity(id, 100L, 200L, startDate, endDate, status);
    }

    @Test
    void getReservationById_ShouldReturnReservation_WhenExists() {
        // Arrange
        ReservationEntity entity = createTestEntity(1L, ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(entity));

        // Act
        Reservation result = reservationService.getReservationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(100L, result.userId());
        assertEquals(200L, result.roomId());
        assertEquals(ReservationStatus.PENDING, result.status());
        verify(reservationRepository).findById(1L);
    }

    @Test
    void getReservationById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reservationService.getReservationById(999L)
        );

        assertEquals("Not found reservation for Id= 999", exception.getMessage());
    }

    @Test
    void findAllReservations_ShouldReturnAllReservations() {
        // Arrange
        List<ReservationEntity> entities = List.of(
                createTestEntity(1L, ReservationStatus.PENDING),
                createTestEntity(2L, ReservationStatus.APPROVED)
        );
        when(reservationRepository.findAll()).thenReturn(entities);

        // Act
        List<Reservation> result = reservationService.findAllReservations();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
        verify(reservationRepository).findAll();
    }

    @Test
    void createReservation_ShouldCreateReservation_WhenValid() {
        // Arrange
        Reservation reservationToCreate = new Reservation(null, 100L, 200L, startDate, endDate, null);
        ReservationEntity entityToSave = new ReservationEntity(null, 100L, 200L, startDate, endDate, ReservationStatus.PENDING);
        ReservationEntity savedEntity = new ReservationEntity(1L, 100L, 200L, startDate, endDate, ReservationStatus.PENDING);

        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedEntity);

        // Act
        Reservation result = reservationService.createReservation(reservationToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(ReservationStatus.PENDING, result.status());
        verify(reservationRepository).save(any(ReservationEntity.class));
    }

    @Test
    void createReservation_ShouldThrowException_WhenIdNotNull() {
        // Arrange
        Reservation reservationWithId = new Reservation(1L, 100L, 200L, startDate, endDate, null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(reservationWithId)
        );

        assertEquals("Id should be empty", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_ShouldThrowException_WhenStatusNotNull() {
        // Arrange
        Reservation reservationWithStatus = new Reservation(null, 100L, 200L, startDate, endDate, ReservationStatus.APPROVED);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(reservationWithStatus)
        );

        assertEquals("Status should be empty", exception.getMessage());
    }

    @Test
    void updateReservation_ShouldUpdateReservation_WhenPending() {
        // Arrange
        ReservationEntity existingEntity = createTestEntity(1L, ReservationStatus.PENDING);
        Reservation reservationToUpdate = new Reservation(1L, 101L, 201L, startDate.plusDays(1), endDate.plusDays(1), ReservationStatus.PENDING);
        ReservationEntity updatedEntity = new ReservationEntity(1L, 101L, 201L, startDate.plusDays(1), endDate.plusDays(1), ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(updatedEntity);

        // Act
        Reservation result = reservationService.updateReservation(1L, reservationToUpdate);

        // Assert
        assertEquals(101L, result.userId());
        assertEquals(201L, result.roomId());
        verify(reservationRepository).findById(1L);
        verify(reservationRepository).save(any(ReservationEntity.class));
    }

    @Test
    void updateReservation_ShouldThrowException_WhenNotPending() {
        // Arrange
        ReservationEntity approvedEntity = createTestEntity(1L, ReservationStatus.APPROVED);
        Reservation reservationToUpdate = new Reservation(1L, 101L, 201L, startDate, endDate, ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(approvedEntity));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reservationService.updateReservation(1L, reservationToUpdate)
        );

        assertEquals("Cannot modify reservation, this status should be PENDING", exception.getMessage());
    }

    @Test
    void deleteReservation_ShouldDelete_WhenExists() {
        // Arrange
        when(reservationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(1L);

        // Act
        reservationService.deleteReservation(1L);

        // Assert
        verify(reservationRepository).existsById(1L);
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void deleteReservation_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(reservationRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reservationService.deleteReservation(999L)
        );

        assertEquals("No found reservation by id= 999", exception.getMessage());
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    void approveReservation_ShouldApprove_WhenPendingAndNoConflict() {
        // Arrange
        ReservationEntity pendingEntity = createTestEntity(1L, ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(pendingEntity);

        // Act
        Reservation result = reservationService.approveReservation(1L);

        // Assert
        assertEquals(ReservationStatus.APPROVED, result.status());
        verify(reservationRepository).save(pendingEntity);
    }

    @Test
    void approveReservation_ShouldThrowException_WhenConflictExists() {
        // Arrange
        ReservationEntity pendingEntity = createTestEntity(1L, ReservationStatus.PENDING);
        ReservationEntity conflictingEntity = new ReservationEntity(
                2L, 101L, 200L, // same roomId
                LocalDate.of(2025, 10, 27),
                LocalDate.of(2025, 10, 29),
                ReservationStatus.APPROVED
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(reservationRepository.findAll()).thenReturn(List.of(conflictingEntity));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.approveReservation(1L)
        );

        assertEquals("Cannot approve reservation, this reservation is conflict", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }
}