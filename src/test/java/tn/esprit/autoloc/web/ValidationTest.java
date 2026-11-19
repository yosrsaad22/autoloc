package tn.esprit.autoloc.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.enums.CategorieVehicule;
import tn.esprit.autoloc.web.dto.ClientDTO;
import tn.esprit.autoloc.web.dto.PaiementDTO;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.VehiculeDTO;
import tn.esprit.autoloc.web.validator.ReservationRequestValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    private static Validator validator;
    private final ReservationRequestValidator customValidator = new ReservationRequestValidator();

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void doitRejeterClientAvecEmailInvalide() {
        ClientDTO client = ClientDTO.builder()
                .nom("Ben Salah")
                .prenom("Ahmed")
                .email("email-invalide")
                .build();

        Set<ConstraintViolation<ClientDTO>> violations = validator.validate(client);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void doitRejeterVehiculeSansImmatriculation() {
        VehiculeDTO vehicule = VehiculeDTO.builder()
                .immatriculation("")
                .marque("Renault")
                .modele("Clio")
                .categorie(CategorieVehicule.CITADINE)
                .tarifJournalier(new BigDecimal("90.00"))
                .agenceId(1L)
                .build();

        Set<ConstraintViolation<VehiculeDTO>> violations = validator.validate(vehicule);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("immatriculation"));
    }

    @Test
    void doitRejeterPaiementAvecMontantNulOuNegatif() {
        PaiementDTO paiementZero = PaiementDTO.builder()
                .montant(BigDecimal.ZERO)
                .modePaiement(ModePaiement.CARTE)
                .contratId(1L)
                .build();

        Set<ConstraintViolation<PaiementDTO>> violationsZero = validator.validate(paiementZero);
        assertThat(violationsZero).anyMatch(v -> v.getPropertyPath().toString().equals("montant"));

        PaiementDTO paiementNegatif = PaiementDTO.builder()
                .montant(new BigDecimal("-50.00"))
                .modePaiement(ModePaiement.CARTE)
                .contratId(1L)
                .build();

        Set<ConstraintViolation<PaiementDTO>> violationsNegatif = validator.validate(paiementNegatif);
        assertThat(violationsNegatif).anyMatch(v -> v.getPropertyPath().toString().equals("montant"));
    }

    @Test
    void doitRejeterReservationSansDateDebutOuFin() {
        ReservationRequestDTO dto = ReservationRequestDTO.builder()
                .clientId(1L)
                .vehiculeId(2L)
                .dateDebut(null)
                .dateFin(null)
                .build();

        Set<ConstraintViolation<ReservationRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dateDebut"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dateFin"));
    }

    @Test
    void doitRejeterReservationSiDateFinAnterieureOuEgaleDateDebut() {
        LocalDate debut = LocalDate.now().plusDays(2);
        LocalDate finEgale = debut;
        ReservationRequestDTO dto = ReservationRequestDTO.builder()
                .clientId(1L)
                .vehiculeId(2L)
                .dateDebut(debut)
                .dateFin(finEgale)
                .build();

        Errors errors = new BeanPropertyBindingResult(dto, "dto");
        customValidator.validate(dto, errors);

        assertThat(errors.hasFieldErrors("dateFin")).isTrue();
        assertThat(errors.getFieldError("dateFin").getDefaultMessage())
                .isEqualTo("La date de fin doit être postérieure à la date de début");
    }

    @Test
    void doitValiderReservationAvecDatesCoherentes() {
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = LocalDate.now().plusDays(5);
        ReservationRequestDTO dto = ReservationRequestDTO.builder()
                .clientId(1L)
                .vehiculeId(2L)
                .dateDebut(debut)
                .dateFin(fin)
                .build();

        Set<ConstraintViolation<ReservationRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();

        Errors errors = new BeanPropertyBindingResult(dto, "dto");
        customValidator.validate(dto, errors);
        assertThat(errors.hasErrors()).isFalse();
    }
}
