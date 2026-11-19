package tn.esprit.autoloc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReservation;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    @OneToOne(mappedBy = "reservation")
    @JsonIgnore
    private Contrat contrat;

    public Long getId() {
        return this.idReservation;
    }

    public void setId(Long id) {
        this.idReservation = id;
    }

    public void setStatut(String statutStr) {
        if (statutStr != null) {
            this.statut = StatutReservation.valueOf(statutStr);
        } else {
            this.statut = null;
        }
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }
}
