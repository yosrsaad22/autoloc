package tn.esprit.autoloc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IEmployeRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.impl.EmployeServiceImpl;
import tn.esprit.autoloc.service.impl.VehiculeServiceImpl;
import tn.esprit.autoloc.web.mapper.EmployeMapper;
import tn.esprit.autoloc.web.mapper.VehiculeMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AffectationServiceTest {

    @Mock
    private IVehiculeRepository vehiculeRepository;
    @Mock
    private IAgenceRepository agenceRepository;
    @Mock
    private VehiculeMapper vehiculeMapper;

    @Mock
    private IEmployeRepository employeRepository;
    @Mock
    private EmployeMapper employeMapper;

    private VehiculeServiceImpl vehiculeService;
    private EmployeServiceImpl employeService;

    private Agence agence1;
    private Agence agence2;
    private Vehicule vehicule;
    private Employe employe;

    @BeforeEach
    void setUp() {
        vehiculeService = new VehiculeServiceImpl(vehiculeRepository, agenceRepository, vehiculeMapper);
        employeService = new EmployeServiceImpl(employeRepository, agenceRepository, employeMapper);

        agence1 = new Agence();
        agence1.setIdAgence(1L);
        agence1.setNom("Agence Tunis Centre");

        agence2 = new Agence();
        agence2.setIdAgence(2L);
        agence2.setNom("Agence Sousse");

        vehicule = new Vehicule();
        vehicule.setIdVehicule(10L);
        vehicule.setImmatriculation("123-TN-456");

        employe = new Employe();
        employe.setIdEmploye(20L);
        employe.setNom("Ben Ali");
        employe.setPrenom("Sami");
    }

    // --- Tests Véhicule ---

    @Test
    void doitAffecterVehiculeAAgence() {
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));
        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence1));

        vehiculeService.affecterVehiculeAAgence(10L, 1L);

        assertThat(vehicule.getAgence()).isEqualTo(agence1);
        verify(vehiculeRepository).save(vehicule);
    }

    @Test
    void doitLeverExceptionSiVehiculeInexistantLorsDeAffectation() {
        when(vehiculeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.affecterVehiculeAAgence(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doitLeverExceptionSiAgenceInexistanteLorsDeAffectationVehicule() {
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));
        when(agenceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.affecterVehiculeAAgence(10L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doitTransfererVehiculeDuneAgenceAUneAutre() {
        vehicule.setAgence(agence1);
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));
        when(agenceRepository.findById(2L)).thenReturn(Optional.of(agence2));

        vehiculeService.affecterVehiculeAAgence(10L, 2L);

        assertThat(vehicule.getAgence()).isEqualTo(agence2);
        verify(vehiculeRepository).save(vehicule);
    }

    @Test
    void doitDesaffecterVehiculeDeAgence() {
        vehicule.setAgence(agence1);
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));

        vehiculeService.desaffecterVehiculeDeAgence(10L);

        assertThat(vehicule.getAgence()).isNull();
        verify(vehiculeRepository).save(vehicule);
    }

    @Test
    void doitLeverExceptionSiDesaffectationVehiculeSansAgence() {
        vehicule.setAgence(null);
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> vehiculeService.desaffecterVehiculeDeAgence(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le véhicule n'est rattaché à aucune agence");
    }

    // --- Tests Employé ---

    @Test
    void doitAffecterEmployeAAgence() {
        when(employeRepository.findById(20L)).thenReturn(Optional.of(employe));
        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence1));

        employeService.affecterEmployeAAgence(20L, 1L);

        assertThat(employe.getAgence()).isEqualTo(agence1);
        verify(employeRepository).save(employe);
    }

    @Test
    void doitLeverExceptionSiEmployeInexistantLorsDeAffectation() {
        when(employeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeService.affecterEmployeAAgence(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doitLeverExceptionSiAgenceInexistanteLorsDeAffectationEmploye() {
        when(employeRepository.findById(20L)).thenReturn(Optional.of(employe));
        when(agenceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeService.affecterEmployeAAgence(20L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doitDesaffecterEmployeDeAgence() {
        employe.setAgence(agence1);
        when(employeRepository.findById(20L)).thenReturn(Optional.of(employe));

        employeService.desaffecterEmployeDeAgence(20L);

        assertThat(employe.getAgence()).isNull();
        verify(employeRepository).save(employe);
    }

    @Test
    void doitLeverExceptionSiDesaffectationEmployeSansAgence() {
        employe.setAgence(null);
        when(employeRepository.findById(20L)).thenReturn(Optional.of(employe));

        assertThatThrownBy(() -> employeService.desaffecterEmployeDeAgence(20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("L'employé n'est rattaché à aucune agence");
    }
}

