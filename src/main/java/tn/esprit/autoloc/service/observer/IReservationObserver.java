package tn.esprit.autoloc.service.observer;

import tn.esprit.autoloc.domain.Reservation;

/**
 * Patron GoF : OBSERVER.
 * Interface commune à tous les observateurs notifies lors des evenements
 * du cycle de vie d'une reservation (confirmation, annulation...).
 */
public interface IReservationObserver {
    void onReservationConfirmee(Reservation reservation);
    void onReservationAnnulee(Reservation reservation);
}

