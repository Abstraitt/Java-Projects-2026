import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

/**
 * Projet 3 - Plateforme de quiz en ligne avec mode temps réel
 * Données stockées dans des tableaux, logique découpée en fonctions.
 */
public class PlateformeQuiz {

    // ===== Constantes =====
    static final int MAX_QUESTIONS = 50;
    static final int MAX_QUIZ = 10;
    static final int MAX_ETUDIANTS = 30;
    static final int TYPE_QCM = 1;
    static final int TYPE_VRAI_FAUX = 2;
    static final int TYPE_COURTE = 3;

    // ===== Tableaux : questions (banque commune) =====
    static String[] enonces = new String[MAX_QUESTIONS];
    static int[] types = new int[MAX_QUESTIONS];
    static String[][] choix = new String[MAX_QUESTIONS][4];   // pour QCM
    static String[] bonnesReponses = new String[MAX_QUESTIONS]; // "B", "vrai", "yaoundé"...
    static int[] points = new int[MAX_QUESTIONS];
    static int nbQuestions = 0;

    // ===== Tableaux : quiz =====
    static String[] titresQuiz = new String[MAX_QUIZ];
    static int[][] questionsDuQuiz = new int[MAX_QUIZ][MAX_QUESTIONS]; // indices dans la banque
    static int[] nbQuestionsQuiz = new int[MAX_QUIZ];
    static boolean[] ordreAleatoire = new boolean[MAX_QUIZ];
    static int nbQuiz = 0;

    // ===== Tableaux : session en direct =====
    static String[] noms = new String[MAX_ETUDIANTS];
    static int[] scores = new int[MAX_ETUDIANTS];
    static int nbEtudiants = 0;
    // reponsesEtudiants[etudiant][position de la question dans le quiz]
    static String[][] reponsesEtudiants = new String[MAX_ETUDIANTS][MAX_QUESTIONS];

    static Scanner sc = new Scanner(System.in);
    static Random rnd = new Random();

    // =====================================================================
    // MAIN
    // =====================================================================
    public static void main(String[] args) {
        chargerDonneesDemo();
        int choixMenu;
        do {
            afficherMenuPrincipal();
            choixMenu = lireEntier("Votre choix : ");
            switch (choixMenu) {
                case 1: menuEnseignant(); break;
                case 2: lancerSessionEnDirect(); break;
                case 3: afficherClassement(); break;
                case 4: exporterResultats(); break;
                case 0: System.out.println("Au revoir !"); break;
                default: System.out.println("Choix invalide.");
            }
        } while (choixMenu != 0);
    }

    static void afficherMenuPrincipal() {
        System.out.println("\n===== PLATEFORME DE QUIZ =====");
        System.out.println("1. Espace enseignant (créer / configurer)");
        System.out.println("2. Lancer une session en direct (étudiants)");
        System.out.println("3. Afficher le classement");
        System.out.println("4. Exporter les résultats");
        System.out.println("0. Quitter");
    }

    // =====================================================================
    // ESPACE ENSEIGNANT : création et configuration
    // =====================================================================
    static void menuEnseignant() {
        int c;
        do {
            System.out.println("\n--- Espace enseignant ---");
            System.out.println("1. Ajouter une question à la banque");
            System.out.println("2. Lister les questions");
            System.out.println("3. Créer un quiz");
            System.out.println("4. Lister les quiz");
            System.out.println("0. Retour");
            c = lireEntier("Choix : ");
            switch (c) {
                case 1: ajouterQuestion(); break;
                case 2: listerQuestions(); break;
                case 3: creerQuiz(); break;
                case 4: listerQuiz(); break;
                case 0: break;
                default: System.out.println("Choix invalide.");
            }
        } while (c != 0);
    }

    static void ajouterQuestion() {
        if (nbQuestions >= MAX_QUESTIONS) {
            System.out.println("Banque de questions pleine.");
            return;
        }
        System.out.println("Type : 1=Choix multiples  2=Vrai/Faux  3=Réponse courte");
        int t = lireEntier("Type : ");
        if (t < 1 || t > 3) {
            System.out.println("Type invalide.");
            return;
        }
        System.out.print("Énoncé : ");
        String enonce = sc.nextLine();
        String[] opts = new String[4];
        String bonne;

        if (t == TYPE_QCM) {
            for (int i = 0; i < 4; i++) {
                System.out.print("Choix " + (char) ('A' + i) + " : ");
                opts[i] = sc.nextLine();
            }
            do {
                System.out.print("Bonne réponse (A/B/C/D) : ");
                bonne = sc.nextLine().trim().toUpperCase();
            } while (!(bonne.equals("A") || bonne.equals("B") || bonne.equals("C") || bonne.equals("D")));
        } else if (t == TYPE_VRAI_FAUX) {
            do {
                System.out.print("Bonne réponse (vrai/faux) : ");
                bonne = sc.nextLine().trim().toLowerCase();
            } while (!(bonne.equals("vrai") || bonne.equals("faux")));
        } else {
            System.out.print("Bonne réponse attendue : ");
            bonne = sc.nextLine().trim().toLowerCase();
        }
        int pts = lireEntier("Points : ");

        enonces[nbQuestions] = enonce;
        types[nbQuestions] = t;
        choix[nbQuestions] = opts;
        bonnesReponses[nbQuestions] = bonne;
        points[nbQuestions] = pts;
        nbQuestions++;
        System.out.println("Question ajoutée (n°" + nbQuestions + ").");
    }

    static void listerQuestions() {
        if (nbQuestions == 0) {
            System.out.println("Aucune question.");
            return;
        }
        for (int i = 0; i < nbQuestions; i++) {
            System.out.println((i + 1) + ". [" + libelleType(types[i]) + "] " + enonces[i]
                    + " (" + points[i] + " pt)");
        }
    }

    static void creerQuiz() {
        if (nbQuiz >= MAX_QUIZ) {
            System.out.println("Nombre maximal de quiz atteint.");
            return;
        }
        if (nbQuestions == 0) {
            System.out.println("Ajoutez d'abord des questions.");
            return;
        }
        System.out.print("Titre du quiz : ");
        titresQuiz[nbQuiz] = sc.nextLine();
        listerQuestions();
        System.out.println("Entrez les numéros des questions (0 pour terminer) :");
        int compteur = 0;
        while (true) {
            int n = lireEntier("N° : ");
            if (n == 0) break;
            if (n < 1 || n > nbQuestions) {
                System.out.println("Numéro invalide.");
                continue;
            }
            questionsDuQuiz[nbQuiz][compteur++] = n - 1;
        }
        if (compteur == 0) {
            System.out.println("Quiz vide, annulé.");
            return;
        }
        nbQuestionsQuiz[nbQuiz] = compteur;
        System.out.print("Ordre aléatoire ? (o/n) : ");
        ordreAleatoire[nbQuiz] = sc.nextLine().trim().equalsIgnoreCase("o");
        nbQuiz++;
        System.out.println("Quiz créé.");
    }

    static void listerQuiz() {
        if (nbQuiz == 0) {
            System.out.println("Aucun quiz.");
            return;
        }
        for (int i = 0; i < nbQuiz; i++) {
            System.out.println((i + 1) + ". " + titresQuiz[i] + " - " + nbQuestionsQuiz[i]
                    + " question(s) - ordre " + (ordreAleatoire[i] ? "aléatoire" : "défini"));
        }
    }

    // =====================================================================
    // SESSION EN DIRECT
    // =====================================================================
    static void lancerSessionEnDirect() {
        if (nbQuiz == 0) {
            System.out.println("Aucun quiz disponible.");
            return;
        }
        listerQuiz();
        int q = lireEntier("Quiz à lancer : ") - 1;
        if (q < 0 || q >= nbQuiz) {
            System.out.println("Quiz invalide.");
            return;
        }
        int nb = lireEntier("Nombre d'étudiants participants (max " + MAX_ETUDIANTS + ") : ");
        if (nb < 1 || nb > MAX_ETUDIANTS) {
            System.out.println("Nombre invalide.");
            return;
        }
        reinitialiserSession();
        nbEtudiants = nb;
        for (int e = 0; e < nb; e++) {
            System.out.print("Nom de l'étudiant " + (e + 1) + " : ");
            noms[e] = sc.nextLine();
        }

        // Ordre des questions : défini ou aléatoire
        int total = nbQuestionsQuiz[q];
        int[] ordre = new int[total];
        for (int i = 0; i < total; i++) ordre[i] = questionsDuQuiz[q][i];
        if (ordreAleatoire[q]) melanger(ordre);

        System.out.println("\n>>> SESSION EN DIRECT : " + titresQuiz[q] + " <<<");
        for (int pos = 0; pos < total; pos++) {
            int idx = ordre[pos];
            System.out.println("\n=== Question " + (pos + 1) + "/" + total + " ("
                    + points[idx] + " pt) ===");
            afficherQuestion(idx);
            for (int e = 0; e < nbEtudiants; e++) {
                System.out.print("[" + noms[e] + "] réponse : ");
                String rep = sc.nextLine().trim();
                reponsesEtudiants[e][pos] = rep;
                if (verifierReponse(idx, rep)) {
                    scores[e] += points[idx];
                }
            }
            System.out.println("Bonne réponse : " + bonnesReponses[idx]);
            afficherClassement(); // classement en temps réel après chaque question
        }
        System.out.println("\n>>> FIN DE LA SESSION <<<");
    }

    static void reinitialiserSession() {
        for (int i = 0; i < MAX_ETUDIANTS; i++) {
            noms[i] = null;
            scores[i] = 0;
            for (int j = 0; j < MAX_QUESTIONS; j++) reponsesEtudiants[i][j] = null;
        }
        nbEtudiants = 0;
    }

    static void afficherQuestion(int idx) {
        System.out.println(enonces[idx]);
        if (types[idx] == TYPE_QCM) {
            for (int i = 0; i < 4; i++) {
                System.out.println("  " + (char) ('A' + i) + ") " + choix[idx][i]);
            }
        } else if (types[idx] == TYPE_VRAI_FAUX) {
            System.out.println("  (répondre : vrai ou faux)");
        }
    }

    static boolean verifierReponse(int idx, String reponse) {
        return reponse.trim().equalsIgnoreCase(bonnesReponses[idx]);
    }

    // Mélange de Fisher-Yates
    static void melanger(int[] t) {
        for (int i = t.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = t[i];
            t[i] = t[j];
            t[j] = tmp;
        }
    }

    // =====================================================================
    // CLASSEMENT (tableaux triés)
    // =====================================================================
    static int[] indicesTriesParScore() {
        int[] idx = new int[nbEtudiants];
        for (int i = 0; i < nbEtudiants; i++) idx[i] = i;
        // tri à bulles décroissant sur les scores
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
        int[] idx = indicesTriesParScore();
        System.out.println("\n--- CLASSEMENT ---");
        for (int r = 0; r < idx.length; r++) {
            System.out.println((r + 1) + ". " + noms[idx[r]] + " : " + scores[idx[r]] + " pt");
        }
    }

    // =====================================================================
    // EXPORT DES RÉSULTATS
    // =====================================================================
    static void exporterResultats() {
        if (nbEtudiants == 0) {
            System.out.println("Aucun résultat à exporter.");
            return;
        }
        int[] idx = indicesTriesParScore();
        try (FileWriter fw = new FileWriter("resultats_quiz.csv")) {
            fw.write("Rang;Nom;Score\n");
            for (int r = 0; r < idx.length; r++) {
                fw.write((r + 1) + ";" + noms[idx[r]] + ";" + scores[idx[r]] + "\n");
            }
            System.out.println("Résultats exportés dans resultats_quiz.csv");
        } catch (IOException ex) {
            System.out.println("Erreur d'export : " + ex.getMessage());
        }
    }

    // =====================================================================
    // OUTILS
    // =====================================================================
    static String libelleType(int t) {
        if (t == TYPE_QCM) return "QCM";
        if (t == TYPE_VRAI_FAUX) return "Vrai/Faux";
        return "Réponse courte";
    }

    static int lireEntier(String message) {
        while (true) {
            System.out.print(message);
            String ligne = sc.nextLine().trim();
            try {
                return Integer.parseInt(ligne);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre entier.");
            }
        }
    }

    // Données de démonstration
    static void chargerDonneesDemo() {
        ajouterDemo("Quel mot-clé Java déclare une constante ?", TYPE_QCM,
                new String[]{"const", "final", "static", "var"}, "B", 2);
        ajouterDemo("Java est un langage compilé en bytecode.", TYPE_VRAI_FAUX,
                new String[4], "vrai", 1);
        ajouterDemo("Quelle est la capitale du Cameroun ?", TYPE_COURTE,
                new String[4], "yaoundé", 2);
        ajouterDemo("Quel indice a le premier élément d'un tableau ?", TYPE_COURTE,
                new String[4], "0", 1);

        titresQuiz[0] = "Quiz de démonstration";
        for (int i = 0; i < 4; i++) questionsDuQuiz[0][i] = i;
        nbQuestionsQuiz[0] = 4;
        ordreAleatoire[0] = false;
        nbQuiz = 1;
    }

    static void ajouterDemo(String e, int t, String[] o, String b, int p) {
        enonces[nbQuestions] = e;
        types[nbQuestions] = t;
        choix[nbQuestions] = o;
        bonnesReponses[nbQuestions] = b;
        points[nbQuestions] = p;
        nbQuestions++;
    }
}
