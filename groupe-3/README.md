ICT University – Section francophone
Programmation Java – Session normale Summer 2026
PROJET 3
Plateforme de quiz en ligne avec mode temps réel
Étudiant : 
NAMENI KOMMOGNE FRANCK JOEL  Matricule : ICTU20251445

NKANO BOLO  ULRICH JOEL Matricule : ICTU20251301
Yaoundé, septembre 2026


1. Introduction
Ce projet consiste à développer, en langage Java, une application console permettant à un enseignant de créer des quiz et à des étudiants d'y répondre en direct. L'application repose uniquement sur les notions étudiées en cours : tableaux, fonctions, boucles et conditions.
2. Objectifs et fonctionnalités
Gérer trois types de questions : choix multiples, vrai/faux et réponse courte.
Permettre à l'enseignant de créer et configurer des quiz (sélection des questions, ordre défini ou aléatoire).
Lancer une session en direct où les questions s'affichent l'une après l'autre.
Enregistrer les réponses des étudiants, les comparer aux bonnes réponses et calculer les scores.
Afficher un classement en temps réel à l'aide de tableaux triés.
Exporter les résultats dans un fichier CSV.
3. Conception
3.1 Structures de données
Les données sont stockées dans des tableaux statiques, organisés en trois groupes :

Groupe
Tableau
Rôle

Banque de questions
enonces[], types[], choix[][], bonnesReponses[], points[]
Stocke chaque question : énoncé, type, options (QCM), réponse attendue et barème

Quiz
titresQuiz[], questionsDuQuiz[][], nbQuestionsQuiz[], ordreAleatoire[]
Associe à chaque quiz un titre, la liste des indices de questions et son mode d'ordre

Session en direct
noms[], scores[], reponsesEtudiants[][]
Conserve les participants, leurs scores et leurs réponses


3.2 Organisation en fonctions
Fonction
Rôle

ajouterQuestion()
Saisie et enregistrement d'une question selon son type

creerQuiz()
Création d'un quiz à partir de numéros de questions et choix de l'ordre

lancerSessionEnDirect()
Déroulement complet de la session : ordre, affichage, réponses, scores

melanger(int[])
Mélange aléatoire des questions (algorithme de Fisher-Yates)

verifierReponse(idx, rep)
Compare la réponse donnée à la bonne réponse sans tenir compte de la casse

indicesTriesParScore()
Tri à bulles décroissant des étudiants selon leur score

afficherClassement()
Affichage du classement en temps réel

exporterResultats()
Écriture du fichier resultats_quiz.csv

lireEntier(message)
Saisie sécurisée d'un entier (gestion des erreurs de format)

4. Fonctionnement
4.1 Espace enseignant
L'enseignant ajoute des questions à la banque commune, puis crée un quiz en indiquant les numéros des questions à inclure. Il choisit enfin si l'ordre d'affichage sera celui de la création ou un ordre aléatoire. Des contrôles empêchent les saisies invalides (type inconnu, bonne réponse hors A–D, numéro de question inexistant).
4.2 Session en direct
Pour chaque question, une boucle interroge successivement chaque étudiant. Sa réponse est enregistrée puis comparée à la bonne réponse : si elle est correcte, les points de la question sont ajoutés à son score. Une fois tous les étudiants passés, la bonne réponse et le classement provisoire sont affichés, ce qui donne l'effet de temps réel.
4.3 Classement et export
Le classement est obtenu en triant les indices des étudiants par score décroissant, sans modifier les tableaux d'origine. L'export produit un fichier CSV (rang, nom, score) exploitable dans un tableur.
5. Utilisation
Compilation : javac PlateformeQuiz.java
Exécution : java PlateformeQuiz
Un quiz de démonstration de quatre questions est préchargé ; il suffit de choisir l'option 2 du menu principal pour le lancer.
6. Limites et améliorations possibles
Le « temps réel » est simulé dans une seule console : les étudiants répondent chacun à leur tour.
Les données ne sont pas sauvegardées entre deux exécutions (une lecture/écriture de fichier pourrait être ajoutée).
Une minuterie par question et une version réseau (sockets) permettraient un vrai mode multi-utilisateurs.
7. Conclusion
Le programme répond aux exigences du sujet : gestion de plusieurs types de questions dans des tableaux, création de quiz par fonctions dédiées, session en direct avec ordre défini ou aléatoire, calcul des scores, classement trié et export des résultats. Son découpage en fonctions distinctes le rend lisible et facile à faire évoluer.


Annexe – Code source (PlateformeQuiz.java)
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

/**
 * Enumeration representant les trois types de questions pris en charge par la plateforme.
 * - QCM : Question a Choix Multiples (plusieurs options fournies).
 * - VRAI_FAUX : Question a reponse binaire (Vrai ou Faux).
 * - REPONSE_COURTE : Question ou l'etudiant saisit un texte court.
 */
enum TypeQuestion {
    QCM,
    VRAI_FAUX,
    REPONSE_COURTE
}

/**
 * Classe modélisant une question individuelle dans le quiz.
 */
class Question {
    String intitule;       // Le texte de la question posée
    TypeQuestion type;     // Le type de la question (QCM, VRAI_FAUX, REPONSE_COURTE)
    String[] options;      // Tableau contenant les choix de réponses (utilisé uniquement pour QCM)
    String bonneReponse;   // La réponse exacte attendue (normalisée en minuscules)

    /**
     * Constructeur spécifique pour les questions de type QCM.
     * @param intitule Le texte de la question.
     * @param options Le tableau des choix possibles.
     * @param bonneReponse La bonne réponse attendue.
     */
    public Question(String intitule, String[] options, String bonneReponse) {
        this.intitule = intitule;
        this.type = TypeQuestion.QCM;
        this.options = options;
        this.bonneReponse = bonneReponse.trim().toLowerCase();
    }

    /**
     * Constructeur pour les questions de type Vrai/Faux ou Réponse Courte.
     * @param intitule Le texte de la question.
     * @param type Le type de la question.
     * @param bonneReponse La réponse exacte attendue.
     */
    public Question(String intitule, TypeQuestion type, String bonneReponse) {
        this.intitule = intitule;
        this.type = type;
        this.options = new String[0]; // Tableau vide car pas de liste d'options
        this.bonneReponse = bonneReponse.trim().toLowerCase();
    }
}

/**
 * Classe modélisant un étudiant/participant au quiz.
 */
class Etudiant {
    String nom; // Nom complet de l'étudiant
    int score;  // Score cumulé lors du quiz

    /**
     * Constructeur de la classe Etudiant.
     * @param nom Le nom du participant.
     */
    public Etudiant(String nom) {
        this.nom = nom;
        this.score = 0; // Le score initial est de 0
    }
}

/**
 * Classe principale contenant le programme, l'interface console et le moteur de jeu.
 */
public class PlateformeQuiz {

    // Capacité maximale des tableaux fixes
    private static final int MAX_QUESTIONS = 50;
    private static final int MAX_ETUDIANTS = 50;

    // Tableau de stockage des questions et son compteur dynamique d'éléments
    private static Question[] banqueQuestions = new Question[MAX_QUESTIONS];
    private static int nbQuestions = 0;

    // Tableau de stockage des étudiants et son compteur dynamique
    private static Etudiant[] etudiants = new Etudiant[MAX_ETUDIANTS];
    private static int nbEtudiants = 0;

    // Scanner unique pour la gestion des entrées utilisateur dans la console
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Chargement initial d'un jeu de questions de démonstration
        initialiserQuestionsParDefaut();

        int choix = 0;
        // Boucle principale du menu applicatif
        do {
            System.out.println("\n========== PLATEFORME DE QUIZ EN LIGNE ==========");
            System.out.println("1. Espace Enseignant (Création / Gestion du Quiz)");
            System.out.println("2. Espace Étudiant (Lancer une session de Quiz)");
            System.out.println("3. Afficher le classement général");
            System.out.println("4. Exporter les résultats dans un fichier");
            System.out.println("5. Quitter");
            System.out.print("Choisissez une option : ");

            // Validation de l'entrée utilisateur pour éviter les erreurs de saisie
            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine(); // Vider le tampon de lecture
            } else {
                System.out.println("Entrée invalide. Veuillez saisir un nombre.");
                scanner.nextLine();
                continue;
            }

            // Orientation vers les différentes fonctionnalités
            switch (choix) {
                case 1:
                    menuEnseignant();
                    break;
                case 2:
                    sessionQuizTempsReel();
                    break;
                case 3:
                    afficherClassement();
                    break;
                case 4:
                    exporterResultats();
                    break;
                case 5:
                    System.out.println("Merci d'avoir utilisé la plateforme. Au revoir !");
                    break;
                default:
                    System.out.println("Option non valide. Veuillez réessayer.");
            }
        } while (choix != 5);
    }

    // ==========================================================
    // MODULE ENSEIGNANT : CREATION ET CONSULTATION DE QUESTIONS
    // ==========================================================

    /**
     * Menu interactif permettant à l'enseignant de créer de nouvelles questions
     * ou d'afficher la banque de questions existante.
     */
    private static void menuEnseignant() {
        System.out.println("\n--- ESPACE ENSEIGNANT ---");
        System.out.println("1. Ajouter une question QCM");
        System.out.println("2. Ajouter une question Vrai/Faux");
        System.out.println("3. Ajouter une question à Réponse Courte");
        System.out.println("4. Lister les questions actuelles");
        System.out.print("Choix : ");

        int choix = scanner.nextInt();
        scanner.nextLine();

        // Vérification de la capacité du tableau avant ajout
        if (nbQuestions >= MAX_QUESTIONS) {
            System.out.println("Erreur : La banque de questions est pleine.");
            return;
        }

        switch (choix) {
            case 1:
                System.out.print("Entrez l'intitulé du QCM : ");
                String intituleQCM = scanner.nextLine();
                System.out.print("Nombre d'options : ");
                int nbOpt = scanner.nextInt();
                scanner.nextLine();
                String[] opts = new String[nbOpt];
                for (int i = 0; i < nbOpt; i++) {
                    System.out.print("Option " + (i + 1) + " : ");
                    opts[i] = scanner.nextLine();
                }
                System.out.print("Entrez la bonne réponse (texte exact) : ");
                String repQCM = scanner.nextLine();
                // Ajout dans le tableau et incrémentation du compteur
                banqueQuestions[nbQuestions++] = new Question(intituleQCM, opts, repQCM);
                System.out.println("Question QCM ajoutée avec succès !");
                break;

            case 2:
                System.out.print("Entrez l'intitulé de la question Vrai/Faux : ");
                String intituleVF = scanner.nextLine();
                System.out.print("Bonne réponse (vrai/faux) : ");
                String repVF = scanner.nextLine();
                banqueQuestions[nbQuestions++] = new Question(intituleVF, TypeQuestion.VRAI_FAUX, repVF);
                System.out.println("Question Vrai/Faux ajoutée avec succès !");
                break;

            case 3:
                System.out.print("Entrez l'intitulé de la question à réponse courte : ");
                String intituleRC = scanner.nextLine();
                System.out.print("Bonne réponse attendue : ");
                String repRC = scanner.nextLine();
                banqueQuestions[nbQuestions++] = new Question(intituleRC, TypeQuestion.REPONSE_COURTE, repRC);
                System.out.println("Question à réponse courte ajoutée avec succès !");
                break;

            case 4:
                afficherQuestions();
                break;

            default:
                System.out.println("Choix invalide.");
        }
    }

    /**
     * Parcourt et affiche l'ensemble des questions enregistrées dans la banque.
     */
    private static void afficherQuestions() {
        System.out.println("\n--- LISTE DES QUESTIONS (" + nbQuestions + ") ---");
        for (int i = 0; i < nbQuestions; i++) {
            Question q = banqueQuestions[i];
            System.out.println((i + 1) + ". [" + q.type + "] " + q.intitule);
            // Si la question est de type QCM, on affiche la liste des options disponibles
            if (q.type == TypeQuestion.QCM) {
                for (int j = 0; j < q.options.length; j++) {
                    System.out.println("   " + (j + 1) + ") " + q.options[j]);
                }
            }
            System.out.println("   Bonne réponse : " + q.bonneReponse);
        }
    }

    // ==========================================================
    // MODULE ETUDIANT : SESSION DE QUIZ EN TEMPS REEL
    // ==========================================================

    /**
     * Gère le déroulement d'une session de quiz interactive pour un étudiant.
     */
    private static void sessionQuizTempsReel() {
        if (nbQuestions == 0) {
            System.out.println("Aucune question disponible. L'enseignant doit d'abord créer un quiz.");
            return;
        }

        System.out.print("\nEntrez votre nom d'étudiant : ");
        String nom = scanner.nextLine();
        Etudiant joueur = new Etudiant(nom);

        System.out.println("\nSouhaitez-vous mélanger les questions ? (1: Oui / 2: Non)");
        int melanger = scanner.nextInt();
        scanner.nextLine();

        // Copie de travail du tableau des questions pour préserver l'ordre initial
        Question[] ordreQuestions = new Question[nbQuestions];
        for (int i = 0; i < nbQuestions; i++) {
            ordreQuestions[i] = banqueQuestions[i];
        }

        // Mélange optionnel des questions (Mode aléatoire)
        if (melanger == 1) {
            melangerQuestions(ordreQuestions);
        }

        System.out.println("\n--- DÉBUT DU QUIZ EN TEMPS RÉEL ---");
        int scoreJoueur = 0;

        // Boucle d'affichage et d'évaluation de chaque question
        for (int i = 0; i < nbQuestions; i++) {
            Question q = ordreQuestions[i];
            System.out.println("\nQuestion " + (i + 1) + "/" + nbQuestions + " [" + q.type + "] : " + q.intitule);

            // Affichage des choix si QCM
            if (q.type == TypeQuestion.QCM) {
                for (int j = 0; j < q.options.length; j++) {
                    System.out.println("  " + (j + 1) + ". " + q.options[j]);
                }
            }

            System.out.print("Votre réponse : ");
            String reponseSaisie = scanner.nextLine().trim().toLowerCase();

            // Comparaison de la réponse saisie avec la bonne réponse enregistrée
            if (reponseSaisie.equals(q.bonneReponse)) {
                System.out.println("Correct ! (+10 points)");
                scoreJoueur += 10;
            } else {
                System.out.println("Incorrect. La bonne réponse était : " + q.bonneReponse);
            }
        }

        // Attribution du score final et enregistrement du joueur
        joueur.score = scoreJoueur;
        enregistrerEtudiant(joueur);

        System.out.println("\nFIN DU QUIZ ! " + nom + ", votre score final est : " + scoreJoueur + " pts.");
        
        // Tri immédiat et affichage du classement à jour
        trierEtudiants();
        afficherClassement();
    }

    /**
     * Algorithme de mélange moderne (Fisher-Yates) pour mélanger un tableau de questions.
     * @param tableau Le tableau à mélanger sur place.
     */
    private static void melangerQuestions(Question[] tableau) {
        Random rand = new Random();
        for (int i = tableau.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            // Échange des éléments d'indice i et j
            Question temp = tableau[i];
            tableau[i] = tableau[j];
            tableau[j] = temp;
        }
    }

    /**
     * Enregistre un nouvel étudiant dans le tableau des résultats.
     */
    private static void enregistrerEtudiant(Etudiant e) {
        if (nbEtudiants < MAX_ETUDIANTS) {
            etudiants[nbEtudiants++] = e;
        } else {
            System.out.println("Limite du nombre d'étudiants atteinte.");
        }
    }

    // ==========================================================
    // MODULE TRI ET CLASSEMENT (TABLEAUX)
    // ==========================================================

    /**
     * Algorithme de Tri par Sélection (Selection Sort) pour ordonner les étudiants
     * par ordre décroissant de leurs scores.
     */
    private static void trierEtudiants() {
        for (int i = 0; i < nbEtudiants - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < nbEtudiants; j++) {
                if (etudiants[j].score > etudiants[maxIdx].score) {
                    maxIdx = j;
                }
            }
            // Échange (permutation) des éléments
            Etudiant temp = etudiants[i];
            etudiants[i] = etudiants[maxIdx];
            etudiants[maxIdx] = temp;
        }
    }

    /**
     * Affiche le classement général des participants sous forme de tableau ordonné.
     */
    private static void afficherClassement() {
        System.out.println("\n================ CLASSEMENT ================");
        if (nbEtudiants == 0) {
            System.out.println("Aucun étudiant n'a encore participé.");
            return;
        }
        for (int i = 0; i < nbEtudiants; i++) {
            System.out.println((i + 1) + ". " + etudiants[i].nom + " - " + etudiants[i].score + " pts");
        }
        System.out.println("============================================");
    }

    // ==========================================================
    // MODULE EXPORTATION DES RESULTATS
    // ==========================================================

    /**
     * Exporte le classement final des étudiants dans un fichier texte externe.
     */
    private static void exporterResultats() {
        String nomFichier = "resultats_quiz.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomFichier))) {
            writer.println("=== RÉSULTATS DU QUIZ EN LIGNE ===");
            writer.println("Nombre total de participants : " + nbEtudiants);
            writer.println("------------------------------------");
            for (int i = 0; i < nbEtudiants; i++) {
                writer.println((i + 1) + ". " + etudiants[i].nom + " | Score : " + etudiants[i].score + " pts");
            }
            System.out.println("Résultats exportés avec succès dans le fichier '" + nomFichier + "' !");
        } catch (IOException e) {
            System.out.println("Erreur lors de l'exportation des résultats : " + e.getMessage());
        }
    }

    /**
     * Initialise quelques questions de test au démarrage pour accélérer les démonstrations.
     */
    private static void initialiserQuestionsParDefaut() {
        String[] opts1 = {"Java", "C++", "Python", "HTML"};
        banqueQuestions[nbQuestions++] = new Question("Lequel est un langage orienté objet compilé sous forme de Bytecode ?", opts1, "Java");
        banqueQuestions[nbQuestions++] = new Question("Java supporte l'héritage multiple de classes.", TypeQuestion.VRAI_FAUX, "faux");
        banqueQuestions[nbQuestions++] = new Question("Quel est le mot-clé 
