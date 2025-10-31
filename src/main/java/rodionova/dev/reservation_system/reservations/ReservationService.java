package rodionova.dev.reservation_system.reservations;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rodionova.dev.reservation_system.reservations.availability.ReservationAvailabilityController;
import rodionova.dev.reservation_system.reservations.availability.ReservationAvalabilityService;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ReservationService {
    ;

    private final ReservationRepository reservationRepository;

    private Logger log = Logger.getLogger(ReservationService.class.getName());

    private final ReservationMapper reservationMapper;

    private final ReservationAvalabilityService reservationAvalabilityService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationMapper reservationMapper,
                              ReservationAvalabilityService reservationAvailabilityService) {
        this.reservationMapper = reservationMapper;
        this.reservationRepository = reservationRepository;
        this.reservationAvalabilityService = reservationAvailabilityService;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = reservationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Not found reservation for Id= " + id));


        return reservationMapper.toDomainReservation(reservationEntity);

    }


    public List<Reservation> searchAllByFilter(ReservationSearchFilter reservationSearchFilter) {

        int pageSize = reservationSearchFilter.pageSize() != null ? reservationSearchFilter.pageSize() : 10;
        int pageNumber = reservationSearchFilter.pageNumber() != null ? reservationSearchFilter.pageNumber() : 0;
        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);
        List<ReservationEntity> allEntities = reservationRepository
                .searchAllByFilter(reservationSearchFilter.roomId(), reservationSearchFilter.userId(), Pageable.unpaged());

        return allEntities.stream().map(reservationMapper::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("End date should be after start date");
        }

        var entityToSave = reservationMapper.toDomainEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        var savedEntity = reservationRepository.save(entityToSave);
        return reservationMapper.toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {

        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No found reservation by id= " + id));
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation, this status should be PENDING");
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("End date should be after start date");
        }

        var reservationToSave = reservationMapper.toDomainEntity(reservationToUpdate);
        reservationToSave.setId(id);
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = reservationRepository.save(reservationToSave);
        log.info("Called the updateReservation method. Reservation updated");

        return reservationMapper.toDomainReservation(savedEntity);
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new EntityNotFoundException("\"No found reservation by id= \" + id");
        }
        log.info("Reservation is deleted");
        reservationRepository.deleteById(id);
    }

    public Reservation approveReservation(Long id) {

        var reservationToApprove = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No found reservation by id= " + id));

        if (reservationToApprove.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation, this status should be PENDING");
        }
        var isAvailable = reservationAvalabilityService.isReservationAvailable(
                reservationToApprove.getRoomId(),
                reservationToApprove.getStartDate(),
                reservationToApprove.getEndDate());
        if (!isAvailable) {
            throw new IllegalArgumentException("Cannot approve reservation, this reservation is conflict");
        }


        reservationToApprove.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservationToApprove);
        return reservationMapper.toDomainReservation(reservationToApprove);
    }


    @Transactional
    public Reservation cancelReservation(Long id) {

        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No found reservation for this id= " + id));

        if (!reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel reservation, this status should NOT BE APPROVED");
        }
        if (!reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel reservation, this reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        ReservationEntity cancelledReservation = reservationRepository.save(reservation);

        log.info("Reservation by id={} is cancelled");
        return reservationMapper.toDomainReservation(cancelledReservation);
    }
}