package tn.esprit.autoloc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IPaiementRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.service.IContratService;
import tn.esprit.autoloc.web.dto.ContratDTO;
import tn.esprit.autoloc.web.dto.PaiementDTO;
import tn.esprit.autoloc.web.mapper.ContratMapper;
import tn.esprit.autoloc.web.mapper.PaiementMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContratServiceImpl implements IContratService {

    private final IContratRepository contratRepository;
    private final IPaiementRepository paiementRepository;
    private final IReservationRepository reservationRepository;
    private final ContratMapper contratMapper;
    private final PaiementMapper paiementMapper;

    @Override
    public ContratDTO create(ContratDTO dto) {
        if (dto.getId() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        verifierMontant(dto.getMontantTotal());

        Contrat contrat = contratMapper.toEntity(dto);
        if (dto.getReservationId() != null) {
            contrat.setReservation(trouverReservation(dto.getReservationId()));
        }
        return contratMapper.toDto(contratRepository.save(contrat));
    }

    @Override
    @Transactional(readOnly = true)
    public ContratDTO findById(Long id) {
        return contratMapper.toDto(trouverContrat(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratDTO> findAll() {
        return contratRepository.findAll().stream()
                .map(contratMapper::toDto)
                .toList();
    }

    @Override
    public ContratDTO update(Long id, ContratDTO dto) {
        verifierMontant(dto.getMontantTotal());

        Contrat existant = trouverContrat(id);
        existant.setMontantTotal(dto.getMontantTotal());
        existant.setDateSignature(dto.getDateSignature());
        existant.setValide(dto.isValide());
        if (dto.getReservationId() != null) {
            existant.setReservation(trouverReservation(dto.getReservationId()));
        }
        return contratMapper.toDto(contratRepository.save(existant));
    }

    @Override
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        contratRepository.deleteById(id);
    }

    @Override
    public PaiementDTO ajouterPaiement(Long idContrat, PaiementDTO paiementDto) {
        Contrat contrat = trouverContrat(idContrat);
        if (contrat.getPaiement() != null) {
            throw new IllegalArgumentException(
                    "Ce contrat possède déjà un paiement");
        }

        Paiement paiement = paiementMapper.toEntity(paiementDto);
        paiement.setIdPaiement(null);
        paiement.setContrat(contrat);
        Paiement sauvegarde = paiementRepository.save(paiement);
        contrat.setPaiement(sauvegarde);
        return paiementMapper.toDto(sauvegarde);
    }

    private Contrat trouverContrat(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    private Reservation trouverReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private void verifierMontant(BigDecimal montant) {
        if (montant != null && montant.signum() < 0) {
            throw new IllegalArgumentException(
                    "Le montant total ne peut pas être négatif");
        }
    }
}
