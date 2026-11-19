package tn.esprit.autoloc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.factory.TarificationStrategyFactory;
import tn.esprit.autoloc.service.impl.ReservationServiceImpl;
import tn.esprit.autoloc.service.observer.EmailNotificationObserver;
import tn.esprit.autoloc.service.observer.ReservationSubject;
import tn.esprit.autoloc.service.observer.SmsNotificationObserver;
import tn.esprit.autoloc.web.mapper.ReservationMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;
    @Mock
    private IClientRepository clientRepository;
    @Mock
    private IVehiculeRepository vehiculeRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private TarificationStrategyFactory tarificationStrategyFactory;

    private EmailNotificationObserver emailObserver;
    private SmsNotificationObserver smsObserver;
    private ReservationSubject reservationSubject;
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        emailObserver = spy(new EmailNotificationObserver());
        smsObserver = spy(new SmsNotificationObserver());
        reservationSubject = new ReservationSubject(List.of(emailObserver, smsObserver));

        reservationService = new ReservationServiceImpl(
                reservationRepository,
                clientRepository,
                vehiculeRepository,
                reservationMapper,
                reservationSubject,
                tarificationStrategyFactory
        );
    }

    @Test
    void doitNotifierObservateursLorsDeLaCreationDeReservation() {
        Client client = Client.builder()
                .idClient(1L)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .telephone("+21612345678")
                .build();

        Reservation reservation = reservationService.creerReservation(client);

        assertThat(reservation).isNotNull();
        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
        assertThat(reservation.getClient()).isEqualTo(client);

        verify(emailObserver).onReservationConfirmee(any(Reservation.class));
        verify(smsObserver).onReservationConfirmee(any(Reservation.class));
    }

    @Test
    void doitNotifierObservateursLorsDeLAnnulationDeReservation() {
        Client client = Client.builder()
                .idClient(1L)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .telephone("+21612345678")
                .build();

        Reservation reservation = Reservation.builder()
                .idReservation(100L)
                .client(client)
                .dateDebut(LocalDate.now())
                .statut(StatutReservation.CONFIRMEE)
                .build();

        Reservation annulee = reservationService.annulerReservation(reservation);

        assertThat(annulee).isNotNull();
        assertThat(annulee.getStatut()).isEqualTo(StatutReservation.ANNULEE);

        verify(emailObserver).onReservationAnnulee(any(Reservation.class));
        verify(smsObserver).onReservationAnnulee(any(Reservation.class));
    }
}

