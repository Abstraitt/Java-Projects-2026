package com.ictu.banque;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Écrit chaque événement important dans un fichier journal texte
 * (par défaut {@code journal.log}), une ligne datée à la fois.
 *
 * Le mode "append" de {@link FileWriter} (paramètre {@code true}) conserve
 * l'historique existant à chaque exécution du programme : le journal n'est
 * jamais écrasé, ce qui est indispensable à la traçabilité.
 */
public class Journal {

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String cheminFichier;

    public Journal() {
        this("journal.log");
    }

    /** Constructeur permettant de choisir le fichier cible (utile pour les tests). */
    public Journal(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    /**
     * Ajoute une ligne datée au journal. Les erreurs d'écriture ne doivent
     * jamais interrompre une opération bancaire déjà effectuée : elles sont
     * donc signalées sur la sortie d'erreur plutôt que propagées.
     */
    public void ecrire(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(cheminFichier, true))) {
            pw.println("[" + LocalDateTime.now().format(FORMAT_DATE) + "] " + message);
        } catch (IOException e) {
            System.err.println("Avertissement : écriture du journal impossible (" + e.getMessage() + ")");
        }
    }
}
