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
}

class Transaction {
    int idEmprunt;
    int idAdherent;
    int idDocument;
    LocalDate dateEmprunt;
    LocalDate dateRetourPrevue;
    LocalDate dateRetourReelle;
    boolean estReserve;

    public Transaction(int idEmprunt, int idAdherent, int idDocument, LocalDate dateEmprunt, boolean estReserve) {
        this.idEmprunt = idEmprunt;
        this.idAdherent = idAdherent;
        this.idDocument = idDocument;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateEmprunt.plusDays(14);
        this.dateRetourReelle = null;
        this.estReserve = estReserve;
    }
}

// --- CLASSE PRINCIPALE ---
public class Main {
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
            System.out.print("Votre choix : ");

            if (!sc.hasNextInt()) break;
            int choixRole = sc.nextInt();
            sc.nextLine();

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

    // --- MENU ADMIN (CRUD) ---
    private static void menuAdmin(Scanner sc) {
        System.out.println("\n--- MENU ADMINISTRATEUR ---");
        System.out.println("1. Ajouter un document (Create)");
        System.out.println("2. Modifier un document (Update)");
        System.out.println("3. Supprimer un document (Delete)");
        System.out.println("4. Consulter le catalogue (Read)");
        System.out.println("5. Enregistrer un retour (Calcul des penalites)");
        System.out.println("6. Generer le rapport des emprunts actifs");
        System.out.print("Votre choix : ");
        if (!sc.hasNextInt()) return;
        int choix = sc.nextInt();
        sc.nextLine();

        switch (choix) {
            case 1 -> {
                System.out.print("ID unique : "); int id = sc.nextInt(); sc.nextLine();
                System.out.print("Titre : "); String titre = sc.nextLine();
                System.out.print("Auteur : "); String auteur = sc.nextLine();
                System.out.print("Type (0:Livre, 1:Revue, 2:Memoire, 3:These) : "); int t = sc.nextInt();
                System.out.print("Nombre d'exemplaires : "); int ex = sc.nextInt();
                catalogue.add(new Document(id, titre, auteur, TypeDoc.values()[t], ex));
                System.out.println("Document ajoute avec succes !");
            }
            case 2 -> {
                System.out.print("ID du document a modifier : "); int id = sc.nextInt(); sc.nextLine();
                Document doc = trouverDocument(id);
                if (doc != null) {
                    System.out.print("Nouveau titre : "); doc.titre = sc.nextLine();
                    System.out.print("Nouvel auteur : "); doc.auteur = sc.nextLine();
                    System.out.print("Nouveau stock d'exemplaires : "); doc.exemplaireDispo = sc.nextInt();
                    System.out.println("Document mis a jour !");
                } else System.out.println("Document introuvable.");
            }
            case 3 -> {
                System.out.print("ID du document a supprimer : "); int id = sc.nextInt();
                if (catalogue.removeIf(d -> d.id == id)) System.out.println("Document supprime.");
                else System.out.println("Document introuvable.");
            }
            case 4 -> afficherCatalogue();
            case 5 -> {
                System.out.print("Entrez l'ID de l'emprunt a cloturer : "); int idEmp = sc.nextInt();
                enregistrerRetour(idEmp);
            }
            case 6 -> genererRapports();
        }
    }

    // --- MENU ADHERENT ---
    private static void menuAdherent(Scanner sc) {
        System.out.print("\nEntrez votre ID Adherent (Ex: 1 ou 2) : ");
        int idAd = sc.nextInt(); sc.nextLine();

        System.out.println("\n--- ESPACE ADHERENT (ID: " + idAd + ") ---");
        System.out.println("1. Consulter le catalogue complet");
        System.out.println("2. Rechercher un ouvrage (Multi-critères)");
        System.out.println("3. Emprunter un ouvrage");
        System.out.println("4. Reserver un ouvrage");
        System.out.println("5. Consulter mon historique de compte");
        System.out.print("Votre choix : ");
        if (!sc.hasNextInt()) return;
        int choix = sc.nextInt(); sc.nextLine();

        switch (choix) {
            case 1 -> afficherCatalogue();
            case 2 -> {
                System.out.print("Entrez un mot-cle (titre, auteur ou categorie) : ");
                String recherche = sc.nextLine();
                rechercherDocument(recherche);
            }
            case 3 -> {
                System.out.print("ID du document a emprunter : "); int idDoc = sc.nextInt();
                effectuerTransaction(idAd, idDoc, false);
            }
            case 4 -> {
                System.out.print("ID du document a reserver : "); int idDoc = sc.nextInt();
                effectuerTransaction(idAd, idDoc, true);
            }
            case 5 -> consulterHistorique(idAd);
        }
    }

    // --- LOGIQUE METIER & ALGORITHMES ---
    private static void afficherCatalogue() {
        System.out.println("\n--- CATALOGUE DES DOCUMENTS EN LIGNE ---");
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

    private static void efectuarTransaction() {}

    private static void effectuerTransaction(int idAdherent, int idDoc, boolean estReservation) {
        Document doc = trouverDocument(idDoc);
        if (doc == null) { System.out.println("Erreur : Document introuvable."); return; }

        if (!estReservation && doc.exemplaireDispo <= 0) {
            System.out.println("Plus d'exemplaires disponibles. Utilisez la reservation.");
            return;
        }

        Transaction t = new Transaction(prochainIdEmprunt++, idAdherent, idDoc, LocalDate.now(), estReservation);
        if (!estReservation) doc.exemplaireDispo--;

        transactions.add(t);
        System.out.println((estReservation ? "Reservation" : "Emprunt") + " enregistre ! ID Transaction : " + t.idEmprunt);
    }

    private static void enregistrerRetour(int idEmprunt) {
        for (Transaction t : transactions) {
            if (t.idEmprunt == idEmprunt && t.dateRetourReelle == null) {
                t.dateRetourReelle = LocalDate.now().plusDays(24); // Simulation retard

                Document doc = trouverDocument(t.idDocument);
                if (doc != null) doc.exemplaireDispo++;

                System.out.println("Retour enregistre.");

                if (t.dateRetourReelle.isAfter(t.dateRetourPrevue)) {
                    long joursRetard = ChronoUnit.DAYS.between(t.dateRetourPrevue, t.dateRetourReelle);
                    long amende = joursRetard * 500;
                    System.out.printf("⚠️ RETARD : %d jours constates. Penalite : %d FCFA\n", joursRetard, amende);
                } else {
                    System.out.println("Document rendu a temps.");
                }
                return;
            }
        }
