package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.autoloc.domain.enums.CategorieVehicule;
import tn.esprit.autoloc.domain.enums.StatutVehicule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "vehicule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVehicule;
    private String immatriculation;
    private String marque;
    private String modele;
    private CategorieVehicule categorie;
    private BigDecimal tarifJournalier;
    private StatutVehicule statut;
    @ManyToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;

    // Un véhicule peut avoir plusieurs réservations
    @OneToMany(mappedBy = "vehicule")
    private List<Reservation> reservations = new ArrayList<>();

    // Un véhicule peut avoir plusieurs maintenances
    @OneToMany(mappedBy = "vehicule")
    private List<Maintenance> maintenances = new ArrayList<>();

    // Plusieurs véhicules peuvent avoir plusieurs équipements
    @ManyToMany
    @JoinTable(
            name = "vehicule_equipement",
            joinColumns = @JoinColumn(name = "vehicule_id"),
            inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    private List<Equipement> equipements = new ArrayList<>();
}
