package com.ictu.banque;

/**
 * Représente un compte bancaire.
 *
 * Les attributs sont privés (encapsulation) : le solde ne se modifie que
 * par {@link #crediter(double)} et {@link #debiter(double)}, jamais
 * directement, afin de garder le contrôle des règles métier au même endroit.
 */
public class Compte {

    /** Nombre d'échecs de connexion consécutifs à partir duquel le compte est bloqué. */
    public static final int SEUIL_BLOCAGE = 3;

    private final String numero;
    private final String titulaire;
    private final String motDePasseHache;
    private double solde;
    private boolean bloque;
    private int echecs;

    public Compte(String numero, String titulaire, String motDePasseHache, double soldeInitial) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Le numéro de compte est obligatoire.");
        }
        if (titulaire == null || titulaire.isBlank()) {
            throw new IllegalArgumentException("Le nom du titulaire est obligatoire.");
        }
        if (soldeInitial < 0) {
            throw new IllegalArgumentException("Le solde initial ne peut pas être négatif.");
        }
        this.numero = numero;
        this.titulaire = titulaire;
        this.motDePasseHache = motDePasseHache;
        this.solde = soldeInitial;
        this.bloque = false;
        this.echecs = 0;
    }

    public String getNumero() {
        return numero;
    }

    public String getTitulaire() {
        return titulaire;
    }

    public String getMotDePasseHache() {
        return motDePasseHache;
    }

    public double getSolde() {
        return solde;
    }

    public boolean isBloque() {
        return bloque;
    }

    public int getEchecs() {
        return echecs;
    }

    /** Crédite le compte d'un montant strictement positif. */
    public void crediter(double montant) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant à créditer doit être positif.");
        }
        solde += montant;
    }

    /**
     * Débite le compte d'un montant strictement positif.
     * Le contrôle "solde suffisant ?" est fait par l'appelant (Banque) avant
     * l'appel, pour pouvoir renvoyer un message métier clair ; cette méthode
     * refuse quand même tout débit qui rendrait le solde négatif.
     */
    public void debiter(double montant) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant à débiter doit être positif.");
        }
        if (montant > solde) {
            throw new IllegalStateException("Solde insuffisant pour débiter ce montant.");
        }
        solde -= montant;
    }

    /** Enregistre un échec d'authentification ; bloque le compte au 3e échec. */
    public void ajouterEchec() {
        echecs++;
        if (echecs >= SEUIL_BLOCAGE) {
            bloque = true;
        }
    }

    /** Remet le compteur d'échecs à zéro (appelé après une connexion réussie). */
    public void reinitialiserEchecs() {
        echecs = 0;
    }

    public void bloquer() {
        bloque = true;
    }

    public void debloquer() {
        bloque = false;
        echecs = 0;
    }

    @Override
    public String toString() {
        return String.format("%s | %-20s | solde=%10.2f FCFA | %s",
                numero, titulaire, solde, bloque ? "BLOQUE" : "actif");
    }
}
