package tn.esprit.autoloc.legacy;

/**
 * Atelier 6 - Partie 1 : Classe problématique (AutoLoc v0)
 *
 * Cette classe représente la première version de l'application AutoLoc.
 * Elle sert de point de départ pour l'analyse des violations des principes SOLID (SRP, OCP)
 * et pour le refactoring avec les patrons de conception GoF (Strategy, Factory, Observer).
 */
public class AutoLocManager {

    public void louerVehicule(String typeReservation, int nombreJours, String clientEmail) {
        double prix = 0;
        if ("LongDuree".equals(typeReservation)) {
            prix = nombreJours * 120;
        } else if ("Weekend".equals(typeReservation)) {
            prix = nombreJours * 180;
        } else if ("Standard".equals(typeReservation)) {
            prix = nombreJours * 150;
        }

        System.out.println("Contrat créé");
        System.out.println("Prix total : " + prix);

        envoyerEmail(clientEmail);
        enregistrerPaiement();
        sauvegarderContrat();
    }

    private void envoyerEmail(String email) {
        System.out.println("Email envoyé à " + email);
    }

    private void envoyerSMS(String sms) {
        System.out.println("SMS envoyé à " + sms);
    }

    private void enregistrerPaiement() {
        System.out.println("Paiement enregistré");
    }

    private void sauvegarderContrat() {
        System.out.println("Contrat sauvegardé");
    }
}

