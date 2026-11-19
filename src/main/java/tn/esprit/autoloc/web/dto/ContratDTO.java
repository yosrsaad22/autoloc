package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratDTO {

    private Long id;
    private LocalDate dateSignature;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant total ne peut pas être négatif")
    private BigDecimal montantTotal;

    private boolean valide;
    private Long reservationId;
}
