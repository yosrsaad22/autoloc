package tn.esprit.autoloc.service.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.service.strategy.ITarificationStrategy;
import tn.esprit.autoloc.service.strategy.TarifLongueDureeStrategy;
import tn.esprit.autoloc.service.strategy.TarifStandardStrategy;
import tn.esprit.autoloc.service.strategy.TarifWeekendStrategy;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TarificationStrategyFactoryTest {

    private TarificationStrategyFactory factory;
    private TarifStandardStrategy standard;
    private TarifWeekendStrategy weekend;
    private TarifLongueDureeStrategy longueDuree;

    @BeforeEach
    void setUp() {
        standard = new TarifStandardStrategy();
        weekend = new TarifWeekendStrategy();
        longueDuree = new TarifLongueDureeStrategy();
        factory = new TarificationStrategyFactory(List.of(standard, weekend, longueDuree));
    }

    @Test
    void doitRetournerLongueDureePour7JoursOuPlus() {
        Reservation reservation = Reservation.builder()
                .dateDebut(LocalDate.parse("2024-01-01"))
                .dateFin(LocalDate.parse("2024-01-08"))
                .build();

        ITarificationStrategy resultat = factory.resoudre(reservation);

        assertThat(resultat).isInstanceOf(TarifLongueDureeStrategy.class);
        assertThat(resultat.getCode()).isEqualTo("LONGUE_DUREE");
    }

    @Test
    void doitRetournerLongueDureePourExactement7Jours() {
        Reservation reservation = Reservation.builder()
                .dateDebut(LocalDate.parse("2024-05-01"))
                .dateFin(LocalDate.parse("2024-05-08"))
                .build();

        ITarificationStrategy resultat = factory.resoudre(reservation);

        assertThat(resultat.getCode()).isEqualTo("LONGUE_DUREE");
    }

    @Test
    void doitRetournerWeekendPourCourtSejourVendredi() {
        Reservation reservation = Reservation.builder()
                .dateDebut(LocalDate.parse("2024-03-08"))
                .dateFin(LocalDate.parse("2024-03-10"))
                .build();

        ITarificationStrategy resultat = factory.resoudre(reservation);

        assertThat(resultat).isInstanceOf(TarifWeekendStrategy.class);
        assertThat(resultat.getCode()).isEqualTo("WEEKEND");
    }

    @Test
    void doitRetournerStandardPourMoyenneDuree() {
        Reservation reservation = Reservation.builder()
                .dateDebut(LocalDate.parse("2024-03-04"))
                .dateFin(LocalDate.parse("2024-03-08"))
                .build();

        ITarificationStrategy resultat = factory.resoudre(reservation);

        assertThat(resultat.getCode()).isEqualTo("STANDARD");
    }
}

