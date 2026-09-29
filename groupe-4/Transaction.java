package com.ictu.banque;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente une opération bancaire (dépôt, retrait ou virement).
 *
 * L'identifiant est attribué automatiquement par la banque au moment de
 * l'enregistrement (voir {@link Banque#enregistrer(Transaction)}).
 * Une transaction jugée suspecte n'est jamais supprimée : elle reste
 * visible dans l'historique, marquée avec son motif, pour que
 * l'administrateur puisse l'examiner (voir rapport, section 4.4).
 */
public class Transaction {

    public enum Type { DEPOT, RETRAIT, VIREMENT }

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id;                 // attribué par Banque.enregistrer()
    private final Type type;
    private final String source;      // numéro de compte source ; null pour un dépôt "externe"
    private final String destination; // numéro de compte destination ; null pour un retrait
    private final double montant;
    private final LocalDateTime date;
    private boolean suspecte;
    private String motif;

    public Transaction(Type type, String source, String destination, double montant) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de transaction est obligatoire.");
        }
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant de la transaction doit être positif.");
        }
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.montant = montant;
        this.date = LocalDateTime.now();
        this.suspecte = false;
        this.motif = "";
    }

    public int getId() {
        return id;
    }

    /** Package-private : seule Banque attribue l'identifiant, à l'enregistrement. */
    void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public double getMontant() {
        return montant;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isSuspecte() {
        return suspecte;
    }

    public String getMotif() {
        return motif;
    }

    /** Marque la transaction comme suspecte, avec le motif de la détection de fraude. */
    public void marquerSuspecte(String motif) {
        this.suspecte = true;
        this.motif = motif;
    }

    /** true si ce compte est impliqué comme source OU comme destination. */
    public boolean concerne(String numeroCompte) {
        return numeroCompte != null && (numeroCompte.equals(source) || numeroCompte.equals(destination));
    }

    @Override
    public String toString() {
        String base = String.format("#%04d [%s] %-8s %10.2f FCFA  %s -> %s",
                id, date.format(FORMAT_DATE), type,
                montant, source == null ? "-" : source, destination == null ? "-" : destination);
        return suspecte ? base + "  ** SUSPECTE (" + motif + ") **" : base;
    }
}
