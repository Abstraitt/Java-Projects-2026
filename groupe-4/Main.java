package com.ictu.banque;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Point d'entrée console : affiche les menus et lit les saisies.
 *
 * Ne contient aucune logique bancaire — toute décision (montant valide,
 * solde suffisant, compte actif, etc.) est déléguée à {@link Banque}.
 * Les méthodes {@link #lireEntier} et {@link #lireMontant} utilisent
 * try/catch : une saisie invalide ne fait pas planter le programme et
 * retourne -1, une valeur toujours refusée par Banque.
 */
public class Main {

    /** Code d'accès à l'espace administrateur (modifiable ici). */
    private static final String CODE_ADMIN = "admin123";

    private static final Scanner SC = new Scanner(System.in);
    private static final Banque BANQUE = new Banque();

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" Simulation de transactions bancaires sécurisées — Groupe 4");
        System.out.println("==========================================================");

        boolean quitter = false;
        while (!quitter) {
            afficherMenuPrincipal();
            int choix = lireEntier("Votre choix : ");
            switch (choix) {
                case 1 -> creerCompte();
                case 2 -> connexionClient();
                case 3 -> connexionAdministrateur();
                case 0 -> quitter = true;
                default -> System.out.println("Choix invalide.");
            }
        }
        System.out.println("Fin du programme. Merci.");
    }

    // ------------------------------------------------------------------
    // Menu principal
    // ------------------------------------------------------------------

    private static void afficherMenuPrincipal() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Créer un compte");
        System.out.println("2. Se connecter (espace client)");
        System.out.println("3. Administration");
        System.out.println("0. Quitter");
    }

    private static void creerCompte() {
        System.out.println("\n-- Création de compte --");
        String titulaire = lireTexte("Nom du titulaire : ");
        String motDePasse = lireTexte("Mot de passe (min. " + Banque.LONGUEUR_MIN_MOT_DE_PASSE + " car.) : ");
        double depot = lireMontant("Dépôt initial (FCFA) : ");
        try {
            Compte c = BANQUE.creerCompte(titulaire, motDePasse, depot);
            System.out.println("Compte créé avec succès. Numéro : " + c.getNumero());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Échec de la création : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Espace client
    // ------------------------------------------------------------------

    private static void connexionClient() {
        System.out.println("\n-- Connexion --");
        String numero = lireTexte("Numéro de compte (ex. CM00001) : ");
        String motDePasse = lireTexte("Mot de passe : ");
        Compte c = BANQUE.authentifier(numero, motDePasse);
        if (c == null) {
            Compte existant = BANQUE.rechercherCompte(numero);
            if (existant != null && existant.isBloque()) {
                System.out.println("Compte bloqué. Contactez un administrateur.");
            } else {
                System.out.println("Identifiants invalides.");
            }
            return;
        }
        menuClient(c);
    }

    private static void menuClient(Compte c) {
        boolean retour = false;
        while (!retour) {
            System.out.println("\n--- ESPACE CLIENT (" + c.getNumero() + ") ---");
            System.out.println("1. Consulter le solde");
            System.out.println("2. Déposer");
            System.out.println("3. Retirer");
            System.out.println("4. Virement");
            System.out.println("5. Historique des transactions");
            System.out.println("6. Exporter mon relevé");
            System.out.println("0. Déconnexion");
            int choix = lireEntier("Votre choix : ");
            switch (choix) {
                case 1 -> System.out.printf("Solde actuel : %.2f FCFA%n", c.getSolde());
                case 2 -> {
                    double m = lireMontant("Montant à déposer : ");
                    System.out.println(BANQUE.deposer(c.getNumero(), m) ? "Dépôt effectué." : "Dépôt refusé.");
                }
                case 3 -> {
                    double m = lireMontant("Montant à retirer : ");
                    System.out.println(BANQUE.retirer(c.getNumero(), m)
                            ? "Retrait effectué." : "Retrait refusé (solde insuffisant ou montant invalide).");
                }
                case 4 -> {
                    String dest = lireTexte("Numéro du compte destinataire : ");
                    double m = lireMontant("Montant à virer : ");
                    System.out.println(BANQUE.virer(c.getNumero(), dest, m)
                            ? "Virement effectué." : "Virement refusé.");
                }
                case 5 -> afficherHistorique(c.getNumero());
                case 6 -> exporter(c.getNumero());
                case 0 -> retour = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private static void afficherHistorique(String numero) {
        List<Transaction> h = BANQUE.historique(numero);
        if (h.isEmpty()) {
            System.out.println("Aucune transaction.");
            return;
        }
        h.forEach(t -> System.out.println("  " + t));
    }

    private static void exporter(String numero) {
        try {
            String fichier = BANQUE.exporterReleve(numero);
            System.out.println("Relevé exporté : " + fichier);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'export : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Espace administrateur
    // ------------------------------------------------------------------

    private static void connexionAdministrateur() {
        String code = lireTexte("\nCode administrateur : ");
        if (!code.equals(CODE_ADMIN)) {
            System.out.println("Code incorrect.");
            return;
        }
        menuAdministrateur();
    }

    private static void menuAdministrateur() {
        boolean retour = false;
        while (!retour) {
            System.out.println("\n--- ESPACE ADMINISTRATEUR ---");
            System.out.println("1. Lister les comptes");
            System.out.println("2. Transactions suspectes");
            System.out.println("3. Bloquer un compte");
            System.out.println("4. Débloquer un compte");
            System.out.println("0. Retour");
            int choix = lireEntier("Votre choix : ");
            switch (choix) {
                case 1 -> BANQUE.listerComptes().forEach(c -> System.out.println("  " + c));
                case 2 -> {
                    List<Transaction> s = BANQUE.transactionsSuspectes();
                    if (s.isEmpty()) {
                        System.out.println("Aucune transaction suspecte.");
                    } else {
                        s.forEach(t -> System.out.println("  " + t));
                    }
                }
                case 3 -> {
                    String n = lireTexte("Numéro du compte à bloquer : ");
                    System.out.println(BANQUE.bloquerCompte(n) ? "Compte bloqué." : "Compte introuvable.");
                }
                case 4 -> {
                    String n = lireTexte("Numéro du compte à débloquer : ");
                    System.out.println(BANQUE.debloquerCompte(n) ? "Compte débloqué." : "Compte introuvable.");
                }
                case 0 -> retour = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    // ------------------------------------------------------------------
    // Lecture des saisies (robuste)
    // ------------------------------------------------------------------

    private static String lireTexte(String invite) {
        System.out.print(invite);
        return SC.nextLine().trim();
    }

    /** Lit un entier ; une saisie invalide retourne -1 (toujours refusé par Banque). */
    private static int lireEntier(String invite) {
        System.out.print(invite);
        try {
            return Integer.parseInt(SC.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Lit un montant décimal ; une saisie invalide retourne -1 (toujours refusé par Banque). */
    private static double lireMontant(String invite) {
        System.out.print(invite);
        try {
            return Double.parseDouble(SC.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
