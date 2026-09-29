package com.ictu.banque;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Cœur du système bancaire.
 *
 * Possède les deux tableaux à taille fixe imposés par le sujet (comptes et
 * transactions), réalise les opérations (création de compte,
 * authentification, dépôt, retrait, virement), analyse la fraude, gère
 * l'administration et l'export des relevés.
 *
 * Un tableau ayant une taille fixe, un compteur ({@code nbComptes},
 * {@code nbTransactions}) indique le nombre de cases réellement utilisées ;
 * les recherches se font par parcours séquentiel (boucle for), comme prévu
 * au rapport (section 3.3).
 */
public class Banque {

    /** Capacité maximale du tableau des comptes. */
    public static final int CAPACITE_COMPTES = 100;
    /** Capacité maximale du tableau des transactions. */
    public static final int CAPACITE_TRANSACTIONS = 1000;
    /** Longueur minimale exigée pour un mot de passe. */
    public static final int LONGUEUR_MIN_MOT_DE_PASSE = 4;

    /** Seuil de montant à partir duquel une opération est jugée suspecte (FCFA). */
    public static final double SEUIL_MONTANT = 500_000;
    /** Nombre d'opérations sortantes en 60 s à partir duquel c'est une fréquence anormale. */
    public static final int MAX_OPS_PAR_MINUTE = 3;
    private static final long FENETRE_FRAUDE_SECONDES = 60;

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Compte[] comptes = new Compte[CAPACITE_COMPTES];
    private int nbComptes = 0;

    private final Transaction[] transactions = new Transaction[CAPACITE_TRANSACTIONS];
    private int nbTransactions = 0;

    private final Journal journal;

    public Banque() {
        this(new Journal());
    }

    /** Constructeur permettant d'injecter un Journal (utile pour les tests). */
    public Banque(Journal journal) {
        this.journal = journal;
    }

    // ------------------------------------------------------------------
    // Création de compte
    // ------------------------------------------------------------------

    /**
     * Crée un nouveau compte.
     *
     * @return le compte créé
     * @throws IllegalStateException    si le tableau des comptes est plein
     * @throws IllegalArgumentException si le dépôt initial est négatif ou le
     *                                  mot de passe trop court
     */
    public Compte creerCompte(String titulaire, String motDePasse, double depotInitial) {
        if (nbComptes >= CAPACITE_COMPTES) {
            throw new IllegalStateException("Capacité maximale de comptes atteinte (" + CAPACITE_COMPTES + ").");
        }
        if (titulaire == null || titulaire.isBlank()) {
            throw new IllegalArgumentException("Le nom du titulaire est obligatoire.");
        }
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN_MOT_DE_PASSE) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins " + LONGUEUR_MIN_MOT_DE_PASSE + " caractères.");
        }
        if (depotInitial < 0) {
            throw new IllegalArgumentException("Le dépôt initial ne peut pas être négatif.");
        }

        String numero = String.format("CM%05d", nbComptes + 1);
        Compte c = new Compte(numero, titulaire, Securite.hacher(motDePasse), depotInitial);
        comptes[nbComptes++] = c;

        journal.ecrire("Création du compte " + numero + " (" + titulaire + "), dépôt initial " + depotInitial);
        return c;
    }

    // ------------------------------------------------------------------
    // Recherche et authentification
    // ------------------------------------------------------------------

    /** Recherche un compte par son numéro (parcours séquentiel). Retourne null si introuvable. */
    public Compte rechercherCompte(String numero) {
        if (numero == null) {
            return null;
        }
        for (int i = 0; i < nbComptes; i++) {
            if (comptes[i].getNumero().equalsIgnoreCase(numero)) {
                return comptes[i];
            }
        }
        return null;
    }

    /**
     * Authentifie un compte par numéro + mot de passe.
     * Bloque le compte après {@link Compte#SEUIL_BLOCAGE} échecs consécutifs.
     *
     * @return le compte si l'authentification réussit, sinon null
     */
    public Compte authentifier(String numero, String motDePasse) {
        Compte c = rechercherCompte(numero);
        if (c == null) {
            journal.ecrire("Tentative de connexion sur un numéro inconnu : " + numero);
            return null;
        }
        if (c.isBloque()) {
            journal.ecrire("Connexion refusée : compte " + numero + " bloqué.");
            return null;
        }
        if (Securite.verifier(motDePasse, c.getMotDePasseHache())) {
            c.reinitialiserEchecs();
            journal.ecrire("Connexion réussie sur le compte " + numero);
            return c;
        }
        c.ajouterEchec();
        journal.ecrire("Échec de connexion sur le compte " + numero + " (" + c.getEchecs() + "/"
                + Compte.SEUIL_BLOCAGE + ")" + (c.isBloque() ? " -> COMPTE BLOQUÉ" : ""));
        return null;
    }

    // ------------------------------------------------------------------
    // Opérations : dépôt, retrait, virement
    // ------------------------------------------------------------------

    /** Dépôt sur un compte. @return true si l'opération a réussi. */
    public boolean deposer(String numero, double montant) {
        Compte c = rechercherCompte(numero);
        if (c == null || c.isBloque() || montant <= 0) {
            return false;
        }
        c.crediter(montant);
        Transaction t = new Transaction(Transaction.Type.DEPOT, null, numero, montant);
        analyserFraude(t);
        enregistrer(t);
        return true;
    }

    /** Retrait sur un compte. @return true si l'opération a réussi (solde suffisant, compte actif). */
    public boolean retirer(String numero, double montant) {
        Compte c = rechercherCompte(numero);
        if (c == null || c.isBloque() || montant <= 0 || c.getSolde() < montant) {
            return false;
        }
        c.debiter(montant);
        Transaction t = new Transaction(Transaction.Type.RETRAIT, numero, null, montant);
        analyserFraude(t);
        enregistrer(t);
        return true;
    }

    /**
     * Virement entre deux comptes distincts.
     * Refuse : destinataire inexistant, bloqué ou identique à la source ;
     * montant nul ou négatif ; solde source insuffisant ; compte source bloqué.
     *
     * @return true si le virement a été effectué
     */
    public boolean virer(String numeroSource, String numeroDestination, double montant) {
        Compte source = rechercherCompte(numeroSource);
        Compte dest = rechercherCompte(numeroDestination);

        if (source == null || source.isBloque()) {
            return false;
        }
        if (dest == null || dest == source || dest.isBloque()) {
            return false;
        }
        if (montant <= 0 || source.getSolde() < montant) {
            return false;
        }

        source.debiter(montant);
        dest.crediter(montant);

        Transaction t = new Transaction(Transaction.Type.VIREMENT, source.getNumero(), dest.getNumero(), montant);
        analyserFraude(t);
        enregistrer(t);
        return true;
    }

    // ------------------------------------------------------------------
    // Détection de fraude
    // ------------------------------------------------------------------

    /**
     * Applique deux règles à base de conditions à chaque opération :
     * montant élevé (≥ {@link #SEUIL_MONTANT}), ou fréquence anormale
     * (≥ {@link #MAX_OPS_PAR_MINUTE} opérations sortantes du même compte
     * dans les {@value #FENETRE_FRAUDE_SECONDES} dernières secondes).
     * Une transaction suspecte n'est jamais supprimée, seulement marquée.
     */
    private void analyserFraude(Transaction t) {
        if (t.getMontant() >= SEUIL_MONTANT) {
            t.marquerSuspecte("Montant eleve");
        } else if (t.getSource() != null && compterOperationsRecentes(t.getSource()) >= MAX_OPS_PAR_MINUTE) {
            // La transaction courante n'est pas encore enregistrée au moment du comptage :
            // on compte donc les opérations déjà enregistrées avant elle. Avec un seuil de 3,
            // la 4e opération sortante rapide (3 précédentes + celle-ci) est ainsi la première
            // marquée suspecte — comme illustré au rapport, section 5, cas de test n°5.
            t.marquerSuspecte("Frequence anormale");
        }
    }

    /** Compte les opérations sortantes (retrait/virement) du compte donné dans la dernière minute. */
    private int compterOperationsRecentes(String numeroCompteSource) {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(FENETRE_FRAUDE_SECONDES);
        int compteur = 0;
        for (int i = 0; i < nbTransactions; i++) {
            Transaction t = transactions[i];
            boolean sortante = t.getType() == Transaction.Type.RETRAIT || t.getType() == Transaction.Type.VIREMENT;
            if (sortante && numeroCompteSource.equals(t.getSource()) && t.getDate().isAfter(limite)) {
                compteur++;
            }
        }
        return compteur;
    }

    /** Enregistre une transaction dans le tableau, lui attribue un identifiant, journalise l'événement. */
    void enregistrer(Transaction t) {
        if (nbTransactions >= CAPACITE_TRANSACTIONS) {
            throw new IllegalStateException("Capacité maximale de transactions atteinte (" + CAPACITE_TRANSACTIONS + ").");
        }
        t.setId(nbTransactions + 1);
        transactions[nbTransactions++] = t;
        journal.ecrire("Transaction " + t);
    }

    // ------------------------------------------------------------------
    // Historique
    // ------------------------------------------------------------------

    /** Historique des transactions où le compte est source ou destination, dans l'ordre chronologique. */
    public List<Transaction> historique(String numeroCompte) {
        List<Transaction> resultat = new ArrayList<>();
        for (int i = 0; i < nbTransactions; i++) {
            if (transactions[i].concerne(numeroCompte)) {
                resultat.add(transactions[i]);
            }
        }
        return resultat;
    }

    /** Toutes les transactions marquées suspectes (pour l'administration). */
    public List<Transaction> transactionsSuspectes() {
        List<Transaction> resultat = new ArrayList<>();
        for (int i = 0; i < nbTransactions; i++) {
            if (transactions[i].isSuspecte()) {
                resultat.add(transactions[i]);
            }
        }
        return resultat;
    }

    // ------------------------------------------------------------------
    // Administration
    // ------------------------------------------------------------------

    /** Liste de tous les comptes (copie défensive, ordre de création). */
    public List<Compte> listerComptes() {
        List<Compte> resultat = new ArrayList<>();
        for (int i = 0; i < nbComptes; i++) {
            resultat.add(comptes[i]);
        }
        return resultat;
    }

    public boolean bloquerCompte(String numero) {
        Compte c = rechercherCompte(numero);
        if (c == null) {
            return false;
        }
        c.bloquer();
        journal.ecrire("Compte " + numero + " bloqué par l'administrateur.");
        return true;
    }

    public boolean debloquerCompte(String numero) {
        Compte c = rechercherCompte(numero);
        if (c == null) {
            return false;
        }
        c.debloquer();
        journal.ecrire("Compte " + numero + " débloqué par l'administrateur.");
        return true;
    }

    public int getNbComptes() {
        return nbComptes;
    }

    public int getNbTransactions() {
        return nbTransactions;
    }

    // ------------------------------------------------------------------
    // Export des relevés
    // ------------------------------------------------------------------

    /**
     * Génère le fichier {@code releve_NUMERO.txt} contenant le titulaire,
     * le solde actuel et l'historique complet du compte.
     *
     * @return le nom du fichier créé
     * @throws IllegalArgumentException si le compte est introuvable
     * @throws IOException              en cas d'erreur d'écriture
     */
    public String exporterReleve(String numero) throws IOException {
        Compte c = rechercherCompte(numero);
        if (c == null) {
            throw new IllegalArgumentException("Compte introuvable : " + numero);
        }
        String nomFichier = "releve_" + c.getNumero() + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(nomFichier, false))) {
            pw.println("Relevé de compte - " + LocalDateTime.now().format(FORMAT_DATE));
            pw.println("Numéro    : " + c.getNumero());
            pw.println("Titulaire : " + c.getTitulaire());
            pw.println(String.format("Solde     : %.2f FCFA", c.getSolde()));
            pw.println("----------------------------------------------------------------");
            pw.println("Historique des transactions :");
            List<Transaction> h = historique(numero);
            if (h.isEmpty()) {
                pw.println("(aucune transaction)");
            } else {
                for (Transaction t : h) {
                    pw.println(t);
                }
            }
        }
        journal.ecrire("Export du relevé " + nomFichier);
        return nomFichier;
    }
}
