import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// --- ENUMERATIONS ---
enum TypeDoc { LIVRE, REVUE, MEMOIRE, THESE }
enum TypeAdherent { ETUDIANT, ENSEIGNANT }

// --- CLASSES DE MODELISATION ---
class Document {
    int id;
    String titre;
    String auteur;
    TypeDoc type;
    int exemplaireDispo;

    public Document(int id, String titre, String auteur, TypeDoc type, int exemplaireDispo) {
        this.id = id;
        this.titre = titre;
        this.auteur = auteur;
        this.type = type;
        this.exemplaireDispo = exemplaireDispo;
    }
}

class Adherent {
    int id;
    String nom;
    String prenom;
    TypeAdherent type;

    public Adherent(int id, String nom, String prenom, TypeAdherent type) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.type = type;
    }

    // Duree de pret selon le profil : 14 jours (etudiant), 30 jours (enseignant)
    public int dureePret() {
        return type == TypeAdherent.ENSEIGNANT ? 30 : 14;
    }
}

class Transaction {
    int idEmprunt;
    int idAdherent;
    int idDocument;
    LocalDate dateEmprunt;
    LocalDate dateRetourPrevue;
    LocalDate dateRetourReelle;
    boolean estReserve;

    public Transaction(int idEmprunt, int idAdherent, int idDocument,
                       LocalDate dateEmprunt, int dureeJours, boolean estReserve) {
        this.idEmprunt = idEmprunt;
        this.idAdherent = idAdherent;
        this.idDocument = idDocument;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateEmprunt.plusDays(dureeJours);
        this.dateRetourReelle = null;
        this.estReserve = estReserve;
    }
}

// --- CLASSE PRINCIPALE ---
public class Main {
    private static final int PENALITE_PAR_JOUR = 500; // FCFA
    private static final List<Document> catalogue = new ArrayList<>();
    private static final List<Adherent> adherents = new ArrayList<>();
    private static final List<Transaction> transactions = new ArrayList<>();
    private static int prochainIdEmprunt = 1;

    public static void main(String[] args) {
        initialiserDonnees();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=========================================");
            System.out.println("    GESTION DE BIBLIOTHEQUE UNIVERSITAIRE");
            System.out.println("=========================================");
            System.out.println("1. Espace Administrateur");
            System.out.println("2. Espace Adherent (Etudiants / Enseignants)");
            System.out.println("3. Quitter le programme");
            int choixRole = lireInt(sc, "Votre choix : ");

            if (choixRole == 3) break;

            switch (choixRole) {
                case 1 -> menuAdmin(sc);
                case 2 -> menuAdherent(sc);
                default -> System.out.println("Choix non valide.");
            }
        }
        System.out.println("\nMerci d'avoir utilise le systeme. Au revoir !");
        sc.close();
    }

    // --- SAISIE SECURISEE ---
    private static int lireInt(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String ligne = sc.nextLine().trim();
            try {
                return Integer.parseInt(ligne);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre entier valide.");
            }
        }
    }

    // =========================================================
    //  FONCTION DE CALCUL DES PÉNALITÉS
    // =========================================================
    private static long calculerPenalite(LocalDate dateRetourPrevue, LocalDate dateReference) {
        if (dateReference == null || !dateReference.isAfter(dateRetourPrevue)) {
            return 0;
        }
        long joursRetard = ChronoUnit.DAYS.between(dateRetourPrevue, dateReference);
        return joursRetard * PENALITE_PAR_JOUR;
    }

    // --- MENU ADMIN ---
    private static void menuAdmin(Scanner sc) {
        System.out.println("\n--- MENU ADMINISTRATEUR ---");
        System.out.println("1. Ajouter un document (Create)");
        System.out.println("2. Modifier un document (Update)");
        System.out.println("3. Supprimer un document (Delete)");
        System.out.println("4. Consulter le catalogue (Read)");
        System.out.println("5. Enregistrer un retour (Calcul des penalites)");
        System.out.println("6. Generer le rapport des emprunts actifs");
        System.out.println("7. Generer le rapport des retards");
        int choix = lireInt(sc, "Votre choix : ");

        switch (choix) {
            case 1 -> {
                int id = lireInt(sc, "ID unique : ");
                if (trouverDocument(id) != null) {
                    System.out.println("Erreur : cet ID existe deja.");
                    return;
                }
                System.out.print("Titre : "); String titre = sc.nextLine();
                System.out.print("Auteur : "); String auteur = sc.nextLine();
                int t = lireInt(sc, "Type (0:Livre, 1:Revue, 2:Memoire, 3:These) : ");
                if (t < 0 || t >= TypeDoc.values().length) {
                    System.out.println("Type invalide.");
                    return;
                }
                int ex = lireInt(sc, "Nombre d'exemplaires : ");
                if (ex < 0) { System.out.println("Nombre invalide."); return; }
                catalogue.add(new Document(id, titre, auteur, TypeDoc.values()[t], ex));
                System.out.println("Document ajoute avec succes !");
            }
            case 2 -> {
                int id = lireInt(sc, "ID du document a modifier : ");
                Document doc = trouverDocument(id);
                if (doc != null) {
                    System.out.print("Nouveau titre : "); doc.titre = sc.nextLine();
                    System.out.print("Nouvel auteur : "); doc.auteur = sc.nextLine();
                    doc.exemplaireDispo = lireInt(sc, "Nouveau stock d'exemplaires : ");
                    System.out.println("Document mis a jour !");
                } else System.out.println("Document introuvable.");
            }
            case 3 -> {
                int id = lireInt(sc, "ID du document a supprimer : ");
                if (catalogue.removeIf(d -> d.id == id)) System.out.println("Document supprime.");
                else System.out.println("Document introuvable.");
            }
            case 4 -> afficherCatalogue();
            case 5 -> {
                int idEmp = lireInt(sc, "Entrez l'ID de l'emprunt a cloturer : ");
                int jours = lireInt(sc, "Nombre de jours ecoules depuis l'emprunt (simulation) : ");
                enregistrerRetour(idEmp, jours);
            }
            case 6 -> rapportEmpruntsActifs();
            case 7 -> rapportRetards();
            default -> System.out.println("Choix non valide.");
        }
    }

    // --- MENU ADHERENT (avec boucle pour rester dans l'espace) ---
    private static void menuAdherent(Scanner sc) {
        int idAd = lireInt(sc, "\nEntrez votre ID Adherent (Ex: 1 ou 2) : ");
        Adherent ad = trouverAdherent(idAd);
        if (ad == null) {
            System.out.println("Adherent introuvable.");
            return;
        }

        // Boucle pour rester dans l'espace adhérent
        while (true) {
            System.out.println("\n--- ESPACE ADHERENT : " + ad.type + " ---");
            System.out.println("1. Consulter le catalogue complet");
            System.out.println("2. Rechercher un ouvrage (Multi-criteres)");
            System.out.println("3. Emprunter un ouvrage");
            System.out.println("4. Reserver un ouvrage");
            System.out.println("5. Consulter mon historique de compte");
            System.out.println("6. Retour au menu principal");
            int choix = lireInt(sc, "Votre choix : ");

            switch (choix) {
                case 1 -> afficherCatalogue();
                case 2 -> {
                    System.out.print("Entrez un mot-cle (titre, auteur ou categorie) : ");
                    String recherche = sc.nextLine();
                    rechercherDocument(recherche);
                }
                case 3 -> {
                    int idDoc = lireInt(sc, "ID du document a emprunter : ");
                    effectuerTransaction(ad, idDoc, false);
                }
                case 4 -> {
                    int idDoc = lireInt(sc, "ID du document a reserver : ");
                    effectuerTransaction(ad, idDoc, true);
                }
                case 5 -> consulterHistorique(ad.id);
                case 6 -> {
                    return; // Sort de l'espace adhérent
                }
                default -> System.out.println("Choix non valide.");
            }
        }
    }

    // --- LOGIQUE METIER ---
    private static Document trouverDocument(int id) {
        for (Document d : catalogue) {
            if (d.id == id) return d;
        }
        return null;
    }

    private static Adherent trouverAdherent(int id) {
        for (Adherent a : adherents) {
            if (a.id == id) return a;
        }
        return null;
    }

    private static void afficherCatalogue() {
        System.out.println("\n--- CATALOGUE DES DOCUMENTS EN LIGNE ---");
        if (catalogue.isEmpty()) {
            System.out.println("Le catalogue est vide.");
            return;
        }
        for (Document doc : catalogue) {
            System.out.printf("ID: %d | [%s] \"%s\" par %s | Stock dispo: %d\n",
                    doc.id, doc.type, doc.titre, doc.auteur, doc.exemplaireDispo);
        }
    }

    private static void rechercherDocument(String critere) {
        System.out.println("\n--- Resultats de la recherche pour : '" + critere + "' ---");
        boolean trouve = false;
        String requeteLower = critere.toLowerCase();

        for (Document doc : catalogue) {
            if (doc.titre.toLowerCase().contains(requeteLower) ||
                    doc.auteur.toLowerCase().contains(requeteLower) ||
                    doc.type.toString().toLowerCase().contains(requeteLower)) {
                System.out.printf("ID: %d | [%s] \"%s\" par %s (Dispo: %d)\n",
                        doc.id, doc.type, doc.titre, doc.auteur, doc.exemplaireDispo);
                trouve = true;
            }
        }
        if (!trouve) System.out.println("Aucun document correspondant trouve.");
    }

    private static void effectuerTransaction(Adherent ad, int idDoc, boolean estReservation) {
        Document doc = trouverDocument(idDoc);
        if (doc == null) { System.out.println("Erreur : Document introuvable."); return; }

        if (!estReservation && doc.exemplaireDispo <= 0) {
            System.out.println("Plus d'exemplaires disponibles. Utilisez la reservation.");
            return;
        }
        if (estReservation && doc.exemplaireDispo > 0) {
            System.out.println("Des exemplaires sont disponibles : empruntez-le directement.");
            return;
        }

        Transaction t = new Transaction(prochainIdEmprunt++, ad.id, idDoc,
                LocalDate.now(), ad.dureePret(), estReservation);
        if (!estReservation) doc.exemplaireDispo--;

        transactions.add(t);
        System.out.println((estReservation ? "Reservation" : "Emprunt") + " enregistre ! ID Transaction : " + t.idEmprunt);
        if (!estReservation) System.out.println("Retour prevu le : " + t.dateRetourPrevue);
    }

    private static void enregistrerRetour(int idEmprunt, int joursEcoules) {
        for (Transaction t : transactions) {
            if (t.idEmprunt == idEmprunt && !t.estReserve && t.dateRetourReelle == null) {
                t.dateRetourReelle = t.dateEmprunt.plusDays(joursEcoules);

                Document doc = trouverDocument(t.idDocument);
                if (doc != null) doc.exemplaireDispo++;

                System.out.println("Retour enregistre.");

                long amende = calculerPenalite(t.dateRetourPrevue, t.dateRetourReelle);

                if (amende > 0) {
                    long joursRetard = ChronoUnit.DAYS.between(t.dateRetourPrevue, t.dateRetourReelle);
                    System.out.printf("RETARD : %d jours constates. Penalite : %d FCFA\n", joursRetard, amende);
                } else {
                    System.out.println("Document rendu a temps.");
                }
                return;
            }
        }
        System.out.println("Aucun emprunt actif trouve avec cet ID.");
    }

    // --- RAPPORTS ---
    private static void rapportEmpruntsActifs() {
        System.out.println("\n--- EMPRUNTS EN COURS ---");
        boolean trouve = false;
        for (Transaction t : transactions) {
            if (!t.estReserve && t.dateRetourReelle == null) {
                Document d = trouverDocument(t.idDocument);
                Adherent a = trouverAdherent(t.idAdherent);
                System.out.printf("Emprunt #%d | %s | \"%s\" | Emprunte le %s | Retour prevu le %s\n",
                        t.idEmprunt,
                        a != null ? a.type : "?",
                        d != null ? d.titre : "(supprime)",
                        t.dateEmprunt, t.dateRetourPrevue);
                trouve = true;
            }
        }
        if (!trouve) System.out.println("Aucun emprunt en cours.");
    }

    private static void rapportRetards() {
        System.out.println("\n--- EMPRUNTS EN RETARD ---");
        boolean trouve = false;
        LocalDate aujourdHui = LocalDate.now();
        for (Transaction t : transactions) {
            if (!t.estReserve && t.dateRetourReelle == null && aujourdHui.isAfter(t.dateRetourPrevue)) {
                long amende = calculerPenalite(t.dateRetourPrevue, aujourdHui);
                long jours = ChronoUnit.DAYS.between(t.dateRetourPrevue, aujourdHui);
                Document d = trouverDocument(t.idDocument);
                System.out.printf("Emprunt #%d | Adherent %d | \"%s\" | %d jours de retard | Penalite : %d FCFA\n",
                        t.idEmprunt, t.idAdherent,
                        d != null ? d.titre : "(supprime)",
                        jours, amende);
                trouve = true;
            }
        }
        if (!trouve) System.out.println("Aucun retard constate.");
    }

    private static void consulterHistorique(int idAdherent) {
        System.out.println("\n--- HISTORIQUE DE L'ADHERENT " + idAdherent + " ---");
        boolean trouve = false;
        for (Transaction t : transactions) {
            if (t.idAdherent == idAdherent) {
                Document d = trouverDocument(t.idDocument);
                String statut;
                if (t.estReserve) statut = "Reservation";
                else if (t.dateRetourReelle == null) statut = "En cours (retour prevu le " + t.dateRetourPrevue + ")";
                else statut = "Rendu le " + t.dateRetourReelle;
                System.out.printf("#%d | \"%s\" | %s | %s\n",
                        t.idEmprunt, d != null ? d.titre : "(supprime)", t.dateEmprunt, statut);
                trouve = true;
            }
        }
        if (!trouve) System.out.println("Aucune operation enregistree.");
    }

    // --- DONNEES DE DEMARRAGE ---
    private static void initialiserDonnees() {
        catalogue.add(new Document(101, "Algorithmique et Java", "J. Dupont", TypeDoc.LIVRE, 3));
        catalogue.add(new Document(102, "Revue Cybersecurite Afrique", "Collectif", TypeDoc.REVUE, 2));
        catalogue.add(new Document(103, "Detection d'intrusions par IA", "T. Essomba", TypeDoc.MEMOIRE, 1));
        catalogue.add(new Document(104, "Cryptographie moderne", "A. Nkou", TypeDoc.THESE, 0));
        catalogue.add(new Document(105, "Introduction a l'algorithmique", "T. Cormen", TypeDoc.LIVRE, 4));
        catalogue.add(new Document(106, "Reseaux informatiques", "A. Tanenbaum", TypeDoc.LIVRE, 3));
        catalogue.add(new Document(107, "Systemes d'exploitation", "A. Silberschatz", TypeDoc.LIVRE, 2));
        catalogue.add(new Document(108, "Bases de donnees relationnelles", "C. Date", TypeDoc.LIVRE, 3));
        catalogue.add(new Document(109, "Securite des systemes d'information", "S. Mbarga", TypeDoc.LIVRE, 2));
        catalogue.add(new Document(110, "Revue Africaine d'Informatique", "Collectif", TypeDoc.REVUE, 2));
        catalogue.add(new Document(111, "Securite des reseaux sans fil", "B. Fouda", TypeDoc.MEMOIRE, 1));
        catalogue.add(new Document(112, "Intelligence artificielle et societe", "S. Owona", TypeDoc.THESE, 1));

        // Noms simplifiés : on garde uniquement le statut
        adherents.add(new Adherent(1, "", "", TypeAdherent.ETUDIANT));
        adherents.add(new Adherent(2, "", "", TypeAdherent.ENSEIGNANT));
    }
}