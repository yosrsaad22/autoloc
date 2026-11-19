package tn.esprit.autoloc.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IReservationService;
import tn.esprit.autoloc.service.factory.TarificationStrategyFactory;
import tn.esprit.autoloc.service.observer.ReservationSubject;
import tn.esprit.autoloc.service.strategy.ITarificationStrategy;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;
import tn.esprit.autoloc.web.mapper.ReservationMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;
    private final IClientRepository clientRepository;
    private final IVehiculeRepository vehiculeRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationSubject reservationSubject;
    private final TarificationStrategyFactory tarificationStrategyFactory;

    @Override
    public ReservationResponseDTO create(ReservationRequestDTO dto) {
        Client client = trouverClient(dto.getClientId());
        Vehicule vehicule = trouverVehicule(dto.getVehiculeId());

        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setVehicule(vehicule);
        reservation.setDateDebut(dto.getDateDebut());
        reservation.setDateFin(dto.getDateFin());
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation saved = reservationRepository.save(reservation);
        if (reservationSubject != null) {
            reservationSubject.notifierConfirmation(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO findById(Long id) {
        return toResponse(trouverReservation(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findAll() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ReservationResponseDTO update(Long id, ReservationRequestDTO dto) {
        Reservation existante = trouverReservation(id);
        existante.setClient(trouverClient(dto.getClientId()));
        existante.setVehicule(trouverVehicule(dto.getVehiculeId()));
        existante.setDateDebut(dto.getDateDebut());
        existante.setDateFin(dto.getDateFin());
        return toResponse(reservationRepository.save(existante));
    }

    @Override
    public void deleteById(Long id) {
        Reservation existante = trouverReservation(id);
        if (reservationSubject != null) {
            reservationSubject.notifierAnnulation(existante);
        }
        reservationRepository.delete(existante);
    }

    /**
     * Crée une réservation et notifie tous les observateurs (Atelier 6 - Observer Pattern)
     */
    @Override
    public Reservation creerReservation(Client client) {
        Reservation reservation = Reservation.builder()
                .client(client)
                .dateDebut(LocalDate.now())
                .statut(StatutReservation.CONFIRMEE)
                .build();
        if (reservationSubject != null) {
            reservationSubject.notifierConfirmation(reservation);
        }
        return reservation;
    }

    /**
     * Annule une réservation existante et notifie tous les observateurs (Atelier 6 - Observer Pattern)
     */
    @Override
    public Reservation annulerReservation(Reservation reservation) {
        reservation.setStatut(StatutReservation.ANNULEE);
        String email = reservation.getClient() != null ? reservation.getClient().getEmail() : "inconnu";
        log.info("Reservation #{} annulée pour le client {}",
                reservation.getId(), email);
        if (reservationSubject != null) {
            reservationSubject.notifierAnnulation(reservation);
        }
        return reservation;
    }

    private ReservationResponseDTO toResponse(Reservation reservation) {
        ReservationResponseDTO dto = reservationMapper.toDto(reservation);
        dto.setTarifEstime(calculerTarifEstime(reservation));
        return dto;
    }

    private BigDecimal calculerTarifEstime(Reservation reservation) {
        if (reservation.getDateDebut() == null
                || reservation.getDateFin() == null
                || reservation.getVehicule() == null
                || reservation.getVehicule().getTarifJournalier() == null) {
            return null;
        }
        if (tarificationStrategyFactory != null) {
            ITarificationStrategy strategy = tarificationStrategyFactory.resoudre(reservation);
            if (strategy != null) {
                return strategy.calculerTarif(reservation.getVehicule(), reservation);
            }
        }
        long jours = ChronoUnit.DAYS.between(
                reservation.getDateDebut(), reservation.getDateFin());
        if (jours <= 0) {
            jours = 1;
        }
        return reservation.getVehicule().getTarifJournalier()
                .multiply(BigDecimal.valueOf(jours));
    }

    private Reservation trouverReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private Client trouverClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private Vehicule trouverVehicule(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }
}
