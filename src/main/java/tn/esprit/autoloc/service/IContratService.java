package tn.esprit.autoloc.service;

import tn.esprit.autoloc.web.dto.ContratDTO;
import tn.esprit.autoloc.web.dto.PaiementDTO;

import java.util.List;

public interface IContratService {

    ContratDTO create(ContratDTO dto);

    ContratDTO findById(Long id);

    List<ContratDTO> findAll();

    ContratDTO update(Long id, ContratDTO dto);

    void deleteById(Long id);

    PaiementDTO ajouterPaiement(Long idContrat, PaiementDTO paiementDto);
}
