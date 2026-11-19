# Compte-Rendu des Travaux Pratiques — Atelier 6 & Atelier 7

**Module :** UP ASI — Architecture des Systèmes d'Information  
**Projet :** AutoLoc — Gestion de location de véhicules  

---

## Sommaire
1. [Atelier 6 : Patrons de Conception GoF et Principes SOLID](#1-atelier-6--patrons-de-conception-gof-et-principes-solid)
   - 1.1 Partie 1 : Analyse de AutoLoc v0 (Réponses aux Questions 1, 2 et 3)
   - 1.2 Partie 2 : Patron Strategy (Calcul dynamique des tarifs)
   - 1.3 Partie 3 : Patron Factory Method (Résolution de stratégie et canaux de notification)
   - 1.4 Partie 4 : Patron Observer (Notification découplée du cycle de vie)
   - 1.5 Partie 5 : Tests unitaires (Observer et Factory)
2. [Atelier 7 : Spring Data JPA, DTO, MapStruct, SonarLint & Validation](#2-atelier-7--spring-data-jpa-dto-mapstruct-sonarlint--validation)
   - 2.1 Partie 1 : Affectation et Désaffectation d'Entités (Véhicule, Employé, Agence)
   - 2.2 Partie 2 : Conception et Utilisation des DTOs avec MapStruct
   - 2.3 Partie 3 : Adaptation des Services et Contrôleurs REST
   - 2.4 Partie 4 : Qualité du code et Analyse SonarLint
   - 2.5 Partie 5 : Validation des Données (Bean Validation et Spring Validator)
3. [Bilan des Tests et Vérifications](#3-bilan-des-tests-et-vérifications)

---

## 1. Atelier 6 : Patrons de Conception GoF et Principes SOLID

### 1.1 Partie 1 : Analyse de AutoLoc v0

La classe initiale `AutoLocManager` regroupait dans une seule méthode monolithique plusieurs logiques métier disparates :
```java
public class AutoLocManager {
    public void louerVehicule(String typeReservation, int nombreJours, String clientEmail) { ... }
}
```

#### Question 1 : Combien de responsabilités possède la classe `AutoLocManager` ?
**Réponse :**  
La classe `AutoLocManager` concentre au minimum **4 responsabilités distinctes** :
1. **Calcul du tarif / Tarification :** Elle calcule le prix de la location selon des règles conditionnelles basées sur le type de réservation.
2. **Gestion du contrat :** Elle crée et persiste le contrat de location (`System.out.println("Contrat créé")`, `sauvegarderContrat()`).
3. **Gestion des paiements :** Elle déclenche l'enregistrement du paiement (`enregistrerPaiement()`).
4. **Gestion des notifications :** Elle gère les canaux de communication vers le client (`envoyerEmail()`, `envoyerSMS()`).

#### Question 2 : Quel principe SOLID n’est pas respecté ?
**Réponse :**  
Le principe **SRP (Single Responsibility Principle - Principe de Responsabilité Unique)** n'est pas respecté.  
*Justification :* Une classe ne devrait avoir qu'une seule raison de changer. Or, `AutoLocManager` changera si les règles tarifaires évoluent, si le format du contrat change, si la passerelle de paiement est modifiée ou si une nouvelle méthode de notification est introduite.

#### Question 3 : Que se passe-t-il si AutoLoc ajoute un nouveau type de réservation ? (Exemple : Pleinesaison)
**Réponse :**  
Pour supporter un nouveau type de réservation (comme `Pleinesaison`), le développeur est contraint de **modifier le code source existant** de la méthode `louerVehicule` en ajoutant une nouvelle branche conditionnelle `else if (typeReservation.equals("Pleinesaison")) { ... }`.  
Cela induit un risque élevé d'effets de bord et de régression sur les types existants (`LongDuree`, `Weekend`, `Standard`).

#### Donc, violation de quel principe ?
**Réponse :**  
Violation du principe **OCP (Open/Closed Principle - Principe Ouvert/Fermé)**.  
*Justification :* Les entités logicielles (classes, modules, fonctions) doivent être **ouvertes à l'extension**, mais **fermées à la modification**.

---

### 1.2 Partie 2 : Refactoring avec Strategy Pattern

Pour découpler l'algorithme de calcul du prix de la réservation, nous avons introduit l'interface `ITarificationStrategy` dans le package `tn.esprit.autoloc.service.strategy` :

```java
public interface ITarificationStrategy {
    BigDecimal calculerTarif(Vehicule vehicule, Reservation reservation);
    String getCode();
}
```

Trois implémentations concrètes (annotées `@Component`) ont été réalisées :
1. **`TarifStandardStrategy`** (`code: "STANDARD"`) : applique le plein tarif journalier sans remise (`jours * tarifJournalier`).
2. **`TarifWeekendStrategy`** (`code: "WEEKEND"`) : applique une remise de 10% (`REMISE_WEEKEND = 0.90`).
3. **`TarifLongueDureeStrategy`** (`code: "LONGUE_DUREE"`) : applique une remise de 20% pour les séjours de 7 jours ou plus (`REMISE_LONGUE_DUREE = 0.80`).

---

### 1.3 Partie 3 : Factory Pattern

Pour éviter que le code client n'instancie directement les stratégies ou observateurs par un `new`, deux fabriques ont été implémentées dans `tn.esprit.autoloc.service.factory` :

1. **`TarificationStrategyFactory`** :
   - Reçoit automatiquement par injection de dépendances Spring toutes les implémentations de `ITarificationStrategy` (`Map<String, ITarificationStrategy>`).
   - La méthode `resoudre(Reservation reservation)` sélectionne la bonne stratégie :
     - Si `jours >= 7` $\rightarrow$ `LONGUE_DUREE`
     - Si `jours <= 3` et début un vendredi/samedi/dimanche $\rightarrow$ `WEEKEND`
     - Sinon (cas par défaut) $\rightarrow$ `STANDARD`
2. **`NotificationFactory`** :
   - Encapsule la création/sélection des observateurs de notification (`email`, `sms`).
   - Méthode `creerObservateur(String canal)` qui retourne l'instance `IReservationObserver` appropriée.

---

### 1.4 Partie 4 : Observer Pattern

Pour notifier automatiquement les parties prenantes lors des événements du cycle de vie d'une réservation sans coupler le service métier aux canaux d'envoi, le patron **Observer** a été mis en place dans `tn.esprit.autoloc.service.observer` :

- **`IReservationObserver`** : interface définissant les hooks d'écoute :
  - `void onReservationConfirmee(Reservation reservation)`
  - `void onReservationAnnulee(Reservation reservation)`
- **`ReservationSubject`** : maintient la liste des observateurs injectés par Spring et diffuse les événements (`notifierConfirmation`, `notifierAnnulation`).
- **`EmailNotificationObserver`** et **`SmsNotificationObserver`** : observateurs concrets qui simulent l'envoi via les logs SLF4J (`log.info("[EMAIL] ...")` et `log.info("[SMS] ...")`).

---

### 1.5 Partie 5 : Intégration et Tests Unitaires

Dans `ReservationServiceImpl`, les méthodes requises par l'Atelier 6 ont été intégrées :
- `creerReservation(Client client)` : construit une réservation confirmée et invoque `reservationSubject.notifierConfirmation(reservation)`.
- `annulerReservation(Reservation reservation)` : passe le statut à `ANNULEE` et invoque `reservationSubject.notifierAnnulation(reservation)`.

#### Tests unitaires réalisés :
1. **`TarificationStrategyFactoryTest`** : valide l'ensemble des 4 branches d'équivalence :
   - Durée $\ge 7$ jours (01/01/2024 au 08/01/2024) $\rightarrow$ `LONGUE_DUREE`
   - Borne exacte à 7 jours (01/05/2024 au 08/05/2024) $\rightarrow$ `LONGUE_DUREE`
   - Court séjour débutant un vendredi (08/03/2024 au 10/03/2024, 2 jours) $\rightarrow$ `WEEKEND`
   - Durée moyenne (04/03/2024 au 08/03/2024, 4 jours du lundi au vendredi) $\rightarrow$ `STANDARD`
2. **`ReservationServiceTest`** : vérifie par mock/spy que les notifications de confirmation et d'annulation atteignent bien les observateurs SMS et Email avec les messages appropriés.

---

## 2. Atelier 7 : Spring Data JPA, DTO, MapStruct, SonarLint & Validation

### 2.1 Partie 1 : Affectation et Désaffectation avec Spring Data JPA

- **Compréhension du côté propriétaire :**
  - Dans l'association `@ManyToOne` entre `Vehicule` et `Agence`, l'entité `Vehicule` est le côté propriétaire portant la clé étrangère `agence_id`.
  - De même, dans l'association entre `Employe` et `Agence`, `Employe` est le côté propriétaire portant `agence_id`.
- **Opérations développées :**
  - `VehiculeService.affecterVehiculeAAgence(Long vehiculeId, Long agenceId)`
  - `VehiculeService.desaffecterVehiculeDeAgence(Long vehiculeId)`
  - `EmployeService.affecterEmployeAAgence(Long employeId, Long agenceId)`
  - `EmployeService.desaffecterEmployeDeAgence(Long employeId)`
- **Endpoints REST PUT exposés :**
  - `PUT /vehicules/{vehiculeId}/affecter/{agenceId}` $\rightarrow$ HTTP 204 No Content
  - `PUT /vehicules/{vehiculeId}/desaffecter` $\rightarrow$ HTTP 204 No Content
  - `PUT /employes/{employeId}/affecter/{agenceId}` $\rightarrow$ HTTP 204 No Content
  - `PUT /employes/{employeId}/desaffecter` $\rightarrow$ HTTP 204 No Content
- **Règles de robustesse :**
  - Vérification systématique de l'existence des entités (lève `ResourceNotFoundException` HTTP 404 si introuvable).
  - Vérification lors de la désaffectation : lève une `IllegalArgumentException` (HTTP 400) si l'entité n'est déjà rattachée à aucune agence.
  - Transactions `@Transactional` assurant la cohérence atomique des modifications en base.

---

### 2.2 Partie 2 : Conception et Utilisation des DTOs avec MapStruct

Les entités JPA ne sont plus jamais exposées directement via l'API REST. Les objets de transfert suivants ont été créés dans `tn.esprit.autoloc.web.dto` :
- `AgenceDTO` (id, nom, ville, adresse, telephone)
- `ClientDTO` (id, nom, prenom, email, telephone, numPermis, dateInscription)
- `ContratDTO` (id, dateSignature, montantTotal, valide, reservationId)
- `PaiementDTO` (id, montant, datePaiement, modePaiement, contratId)
- `ReservationRequestDTO` (clientId, vehiculeId, dateDebut, dateFin)
- `ReservationResponseDTO` (id, dateDebut, dateFin, statut, clientId, clientNomComplet, vehiculeId, vehiculeImmatriculation, tarifEstime)
- `VehiculeDTO` (id, immatriculation, marque, modele, categorie, tarifJournalier, statut, agenceId)
- `EmployeDTO`, `EquipementDTO`, `MaintenanceDTO`

Dans `tn.esprit.autoloc.web.mapper`, des interfaces de mapping annotées `@Mapper(componentModel = "spring")` assurent des conversions bidirectionnelles performantes et sûres à la compilation via **MapStruct 1.5.5.Final**.

---

### 2.3 Partie 3 : Adaptation des Services et Contrôleurs REST

Tous les contrôleurs REST et interfaces de services ont été refactorisés :
- Les méthodes des services reçoivent des DTOs et retournent des DTOs.
- Tous les contrôleurs REST renvoient des `ResponseEntity<DTO>` ou `ResponseEntity<List<DTO>>` avec les codes de statut HTTP appropriés (`200 OK`, `201 CREATED`, `204 NO_CONTENT`).
- **Garantie clé :** Aucune entité JPA (`Agence`, `Client`, `Vehicule`, `Contrat`, `Employe`, `Reservation`, `Paiement`) n'échappe à la couche REST.

---

### 2.4 Partie 4 : Qualité du code et Analyse SonarLint

Lors de la révision de qualité du code conforme aux règles SonarLint / Clean Code :
1. **Suppression des retours directs d'entités JPA dans l'API Web :**
   - *Règle :* Sécurité et découplage (prévention des boucles infinies de sérialisation JSON et de la fuite du schéma relationnel).
2. **Encapsulation des dépendances et constructeurs :**
   - Utilisation de `@RequiredArgsConstructor` de Lombok avec champs `private final` au lieu de l'injection par champ `@Autowired` (meilleure testabilité unitaire).
3. **Gestion des exceptions spécifiques :**
   - Éviter d'attraper ou lancer des exceptions génériques (`Exception` ou `RuntimeException`).
   - Utilisation de `ResourceNotFoundException` et `IllegalArgumentException` gérées centralement par `@RestControllerAdvice`.
4. **Immutabilité et lisibilité :**
   - Utilisation des flux `Stream.toList()` au lieu de `Collectors.toList()`.
   - Utilisation des méthodes factory immuables `List.of()` dans les tests et fabriques.
5. **Prévention des NullPointerException :**
   - Vérifications préventives de nullité sur les associations (`reservation.getClient() != null`) lors de la notification ou du calcul de tarif.

---

### 2.5 Partie 5 : Validation des Données (Bean Validation et Validateur Personnalisé)

1. **Bean Validation (JSR-380) sur les DTOs :**
   - `AgenceDTO` : `@NotBlank` sur le nom et la ville.
   - `ClientDTO` : `@NotBlank` sur nom et prénom, `@NotBlank` + `@Email` sur l'adresse email.
   - `ContratDTO` : `@DecimalMin(value = "0.0")` pour un montant positif ou nul.
   - `PaiementDTO` : `@NotNull` + `@DecimalMin(value = "0.0", inclusive = false)` pour un montant strictement positif, `@NotNull` sur le mode de paiement et contratId.
   - `ReservationRequestDTO` : `@NotNull` sur clientId, vehiculeId, et `@FutureOrPresent` sur les dates de début et fin.
   - `VehiculeDTO` : `@NotBlank` sur immatriculation, marque, modèle, `@NotNull` sur catégorie, agenceId et `@DecimalMin(inclusive = false)` sur le tarif journalier.
2. **Gestionnaire Global d'Exceptions (`GlobalExceptionHandler`) :**
   - Intercepte `MethodArgumentNotValidException` et renvoie un format d'erreur lisible `Map<String, String>` associant chaque champ à son message de validation explicite (HTTP 400).
3. **Validateur Personnalisé Spring (`ReservationRequestValidator`) :**
   - Implémente `org.springframework.validation.Validator`.
   - Contrôle la cohérence des dates :
     - La date de début doit être aujourd'hui ou dans le futur.
     - La date de fin doit être strictement postérieure à la date de début.
4. **Intégration dans `ReservationController` :**
   - Utilisation conjointe de `@Valid @RequestBody ReservationRequestDTO dto, BindingResult result`.
   - Appel explicite de `reservationRequestValidator.validate(dto, result)` avant toute transmission au service.

---

## 3. Bilan des Tests et Vérifications

Tous les tests unitaires et d'intégration ont été rédigés et validés avec succès :

| Classe de test | Rôle / Couverture | Nombre de tests | Résultat |
| :--- | :--- | :---: | :---: |
| **`TarificationStrategyFactoryTest`** | Atelier 6 : Test des 4 cas de tarification (7j, borne 7j, weekend vendredi, durée moyenne) | 4 | **SUCCÈS (100%)** |
| **`ReservationServiceTest`** | Atelier 6 : Test du patron Observer (confirmation et annulation avec SMS & Email) | 2 | **SUCCÈS (100%)** |
| **`AffectationServiceTest`** | Atelier 7 : Test complet d'affectation/désaffectation véhicule et employé à une agence | 10 | **SUCCÈS (100%)** |
| **`ValidationTest`** | Atelier 7 : Test des contraintes Bean Validation et du validateur de dates personnalisé | 6 | **SUCCÈS (100%)** |
| **TOTAL** | Ensemble des exigences fonctionnelles des deux ateliers | **22** | **TOUS LES TESTS PASSENT** |

