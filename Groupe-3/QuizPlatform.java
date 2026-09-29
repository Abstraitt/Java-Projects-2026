import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

/**
 * Projet 3 - Plateforme de quiz en ligne avec mode temps réel (version console).
 * Tout est stocké dans des tableaux et organisé en fonctions distinctes.
 */
public class QuizPlatform {

    // ---------- Constantes ----------
    static final int MAX_QUESTIONS = 50;
    static final int MAX_ETUDIANTS = 30;
    static final int QCM = 1, VRAI_FAUX = 2, COURTE = 3;

    static Scanner sc = new Scanner(System.in);
    static Random rnd = new Random();

    // ---------- Configuration du quiz ----------
    static String titreQuiz = "Quiz sans titre";
    static boolean ordreAleatoire = false;
    static int tempsLimite = 0; // secondes par question (0 = illimité)

    // ---------- Tableaux des questions ----------
    static String[] enonces = new String[MAX_QUESTIONS];
    static int[] types = new int[MAX_QUESTIONS];
    static String[][] choix = new String[MAX_QUESTIONS][4];
    static String[] bonnesReponses = new String[MAX_QUESTIONS];
    static int[] points = new int[MAX_QUESTIONS];
    static int nbQuestions = 0;

    // ---------- Tableaux des étudiants ----------
    static String[] noms = new String[MAX_ETUDIANTS];
    static int[] scores = new int[MAX_ETUDIANTS];
    static String[][] reponses = new String[MAX_ETUDIANTS][MAX_QUESTIONS];
    static int nbEtudiants = 0;

    // =====================================================
    //                    UTILITAIRES
    // =====================================================

    static int lireEntier(String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String saisie = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(saisie);
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException e) {
                // on redemande
            }
            System.out.println("  Valeur invalide (entre " + min + " et " + max + ").");
        }
    }

    static String lireTexte(String message) {
        String s;
        do {
            System.out.print(message);
            s = sc.nextLine().trim();
        } while (s.isEmpty());
        return s;
    }

    static int totalPoints() {
        int total = 0;
        for (int i = 0; i < nbQuestions; i++) total += points[i];
        return total;
    }

    // =====================================================
    //            CRÉATION ET CONFIGURATION DU QUIZ
    // =====================================================

    static void configurerQuiz() {
        titreQuiz = lireTexte("Titre du quiz : ");
        ordreAleatoire = lireEntier("Ordre des questions (1 = défini, 2 = aléatoire) : ", 1, 2) == 2;
        tempsLimite = lireEntier("Temps limite par question en secondes (0 = illimité) : ", 0, 600);
        System.out.println("Configuration enregistrée.");
    }

    static void ajouterQuestion() {
        if (nbQuestions >= MAX_QUESTIONS) {
            System.out.println("Nombre maximum de questions atteint.");
            return;
        }
        System.out.println("Type : 1) Choix multiples  2) Vrai/Faux  3) Réponse courte");
        int t = lireEntier("Votre choix : ", 1, 3);
        int q = nbQuestions;

        enonces[q] = lireTexte("Énoncé : ");
        types[q] = t;

        if (t == QCM) {
            for (int i = 0; i < 4; i++) {
                choix[q][i] = lireTexte("  Choix " + (char) ('A' + i) + " : ");
            }
            int b = lireEntier("Bonne réponse (1=A, 2=B, 3=C, 4=D) : ", 1, 4);
            bonnesReponses[q] = String.valueOf((char) ('A' + b - 1));
        } else if (t == VRAI_FAUX) {
            int b = lireEntier("Bonne réponse (1 = Vrai, 2 = Faux) : ", 1, 2);
            bonnesReponses[q] = (b == 1) ? "VRAI" : "FAUX";
        } else {
            bonnesReponses[q] = lireTexte("Réponse attendue : ");
        }
        points[q] = lireEntier("Points (1-10) : ", 1, 10);
        nbQuestions++;
        System.out.println("Question ajoutée.");
    }

    static void afficherQuestions() {
        if (nbQuestions == 0) {
            System.out.println("Aucune question.");
            return;
        }
        System.out.println("\n=== " + titreQuiz + " (" + nbQuestions + " questions, "
                + totalPoints() + " pts) ===");
        for (int i = 0; i < nbQuestions; i++) {
            System.out.println((i + 1) + ". [" + nomType(types[i]) + ", " + points[i] + " pts] "
                    + enonces[i] + "  -> " + bonnesReponses[i]);
        }
    }

    static String nomType(int t) {
        if (t == QCM) return "QCM";
        if (t == VRAI_FAUX) return "V/F";
        return "Courte";
    }

    static void supprimerQuestion() {
        if (nbQuestions == 0) {
            System.out.println("Aucune question à supprimer.");
            return;
        }
        afficherQuestions();
        int n = lireEntier("Numéro de la question à supprimer : ", 1, nbQuestions) - 1;
        for (int i = n; i < nbQuestions - 1; i++) {
            enonces[i] = enonces[i + 1];
            types[i] = types[i + 1];
            choix[i] = choix[i + 1];
            bonnesReponses[i] = bonnesReponses[i + 1];
            points[i] = points[i + 1];
        }
        nbQuestions--;
        choix[nbQuestions] = new String[4];
        System.out.println("Question supprimée.");
    }

    static void chargerExemples() {
        nbQuestions = 0;
        titreQuiz = "Quiz Java de base";

        enonces[0] = "Quel mot-clé permet de déclarer une constante ?";
        types[0] = QCM;
        choix[0] = new String[]{"const", "final", "static", "var"};
        bonnesReponses[0] = "B";
        points[0] = 2;

        enonces[1] = "Un tableau Java a une taille fixe.";
        types[1] = VRAI_FAUX;
        bonnesReponses[1] = "VRAI";
        points[1] = 1;

        enonces[2] = "Quelle méthode est le point d'entrée d'un programme Java ?";
        types[2] = COURTE;
        bonnesReponses[2] = "main";
        points[2] = 3;

        enonces[3] = "Quelle boucle s'exécute au moins une fois ?";
        types[3] = QCM;
        choix[3] = new String[]{"for", "while", "do...while", "foreach"};
        bonnesReponses[3] = "C";
        points[3] = 2;

        nbQuestions = 4;
        System.out.println("4 questions d'exemple chargées.");
    }

    // =====================================================
    //                  SESSION EN DIRECT
    // =====================================================

    /** Construit l'ordre d'affichage : défini (0,1,2...) ou mélangé (Fisher-Yates). */
    static int[] construireOrdre() {
        int[] ordre = new int[nbQuestions];
        for (int i = 0; i < nbQuestions; i++) ordre[i] = i;
        if (ordreAleatoire) {
            for (int i = nbQuestions - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int tmp = ordre[i];
                ordre[i] = ordre[j];
                ordre[j] = tmp;
            }
        }
        return ordre;
    }

    static void afficherQuestion(int q, int numero) {
        System.out.println("\nQuestion " + numero + "/" + nbQuestions
                + " (" + points[q] + " pts) : " + enonces[q]);
        if (types[q] == QCM) {
            for (int i = 0; i < 4; i++) {
                System.out.println("   " + (char) ('A' + i) + ") " + choix[q][i]);
            }
        } else if (types[q] == VRAI_FAUX) {
            System.out.println("   V) Vrai    F) Faux");
        }
        if (tempsLimite > 0) System.out.println("   (Temps limite : " + tempsLimite + " s)");
    }

    static String lireReponse(int q) {
        while (true) {
            System.out.print("Votre réponse : ");
            String s = sc.nextLine().trim();
            if (types[q] == QCM) {
                s = s.toUpperCase();
                if (s.equals("A") || s.equals("B") || s.equals("C") || s.equals("D")) return s;
                System.out.println("  Entrez A, B, C ou D.");
            } else if (types[q] == VRAI_FAUX) {
                s = s.toUpperCase();
                if (s.equals("V")) return "VRAI";
                if (s.equals("F")) return "FAUX";
                System.out.println("  Entrez V ou F.");
            } else {
                if (!s.isEmpty()) return s;
                System.out.println("  Réponse vide.");
            }
        }
    }

    static boolean verifierReponse(int q, String rep) {
        return bonnesReponses[q].trim().equalsIgnoreCase(rep.trim());
    }

    static void lancerSession() {
        if (nbQuestions == 0) {
            System.out.println("Ajoutez d'abord des questions.");
            return;
        }
        if (nbEtudiants >= MAX_ETUDIANTS) {
            System.out.println("Nombre maximum de participants atteint.");
            return;
        }

        System.out.println("\n=== SESSION : " + titreQuiz + " ===");
        String nom = lireTexte("Nom de l'étudiant : ");
        int[] ordre = construireOrdre();
        int score = 0;

        for (int k = 0; k < nbQuestions; k++) {
            int q = ordre[k];
            afficherQuestion(q, k + 1);

            long debut = System.currentTimeMillis();
            String rep = lireReponse(q);
            long duree = (System.currentTimeMillis() - debut) / 1000;

            reponses[nbEtudiants][q] = rep;

            if (tempsLimite > 0 && duree > tempsLimite) {
                System.out.println("Temps dépassé (" + duree + " s) : 0 point.");
            } else if (verifierReponse(q, rep)) {
                score += points[q];
                System.out.println("Correct ! +" + points[q] + " pts");
            } else {
                System.out.println("Faux. Bonne réponse : " + bonnesReponses[q]);
            }
        }

        noms[nbEtudiants] = nom;
        scores[nbEtudiants] = score;
        nbEtudiants++;

        System.out.println("\nScore final de " + nom + " : " + score + "/" + totalPoints());
        afficherClassement();
    }

    // =====================================================
    //                CLASSEMENT ET EXPORT
    // =====================================================

    /** Renvoie les indices des étudiants triés par score décroissant (tri à bulles). */
    static int[] classementTrie() {
        int[] idx = new int[nbEtudiants];
        for (int i = 0; i < nbEtudiants; i++) idx[i] = i;
        for (int i = 0; i < nbEtudiants - 1; i++) {
            for (int j = 0; j < nbEtudiants - 1 - i; j++) {
                if (scores[idx[j]] < scores[idx[j + 1]]) {
                    int tmp = idx[j];
                    idx[j] = idx[j + 1];
                    idx[j + 1] = tmp;
                }
            }
        }
        return idx;
    }

    static void afficherClassement() {
        if (nbEtudiants == 0) {
            System.out.println("Aucun résultat pour le moment.");
            return;
        }
        int[] idx = classementTrie();
        System.out.println("\n--- CLASSEMENT ---");
        for (int r = 0; r < nbEtudiants; r++) {
            System.out.println((r + 1) + ". " + noms[idx[r]] + " : "
                    + scores[idx[r]] + "/" + totalPoints());
        }
    }

    static void exporterResultats() {
        if (nbEtudiants == 0) {
            System.out.println("Rien à exporter.");
            return;
        }
        int[] idx = classementTrie();
        try (FileWriter fw = new FileWriter("resultats.csv")) {
            fw.write("Quiz;" + titreQuiz + "\n");
            fw.write("Rang;Nom;Score;Total\n");
            for (int r = 0; r < nbEtudiants; r++) {
                fw.write((r + 1) + ";" + noms[idx[r]] + ";" + scores[idx[r]] + ";" + totalPoints() + "\n");
            }
            System.out.println("Résultats exportés dans resultats.csv");
        } catch (IOException e) {
            System.out.println("Erreur d'export : " + e.getMessage());
        }
    }

    // =====================================================
    //                        MENU
    // =====================================================

    static void afficherMenu() {
        System.out.println("\n===== PLATEFORME DE QUIZ =====");
        System.out.println("Enseignant :");
        System.out.println("  1. Configurer le quiz");
        System.out.println("  2. Ajouter une question");
        System.out.println("  3. Lister les questions");
        System.out.println("  4. Supprimer une question");
        System.out.println("  5. Charger des questions d'exemple");
        System.out.println("Étudiant :");
        System.out.println("  6. Lancer une session (répondre au quiz)");
        System.out.println("Résultats :");
        System.out.println("  7. Afficher le classement");
        System.out.println("  8. Exporter les résultats (CSV)");
        System.out.println("  0. Quitter");
    }

    public static void main(String[] args) {
        int choixMenu;
        do {
            afficherMenu();
            choixMenu = lireEntier("Votre choix : ", 0, 8);
            switch (choixMenu) {
                case 1: configurerQuiz(); break;
                case 2: ajouterQuestion(); break;
                case 3: afficherQuestions(); break;
                case 4: supprimerQuestion(); break;
                case 5: chargerExemples(); break;
                case 6: lancerSession(); break;
                case 7: afficherClassement(); break;
                case 8: exporterResultats(); break;
                default: System.out.println("Au revoir !");
            }
        } while (choixMenu != 0);
    }
}
