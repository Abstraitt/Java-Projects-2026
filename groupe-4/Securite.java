package com.ictu.banque;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitaire de sécurité : hachage des mots de passe avec SHA-256.
 *
 * Aucun mot de passe n'est jamais conservé en clair. On ne stocke que
 * l'empreinte (64 caractères hexadécimaux). À la connexion, on hache la
 * saisie et on compare les deux empreintes.
 *
 * Limite assumée (voir rapport, section 6) : SHA-256 sans sel. Pour une
 * vraie mise en production, préférer BCrypt/Argon2.
 */
public final class Securite {

    private Securite() {
        // Classe utilitaire : pas d'instanciation.
    }

    /**
     * Calcule l'empreinte SHA-256 d'un texte, sous forme hexadécimale (64 car.).
     *
     * @param texte le texte en clair à hacher (ex. un mot de passe)
     * @return l'empreinte hexadécimale en minuscules
     */
    public static String hacher(String texte) {
        if (texte == null) {
            throw new IllegalArgumentException("Le texte à hacher ne peut pas être null.");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] octets = md.digest(texte.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(octets.length * 2);
            for (byte b : octets) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti disponible sur toute JVM standard (algorithme obligatoire).
            throw new IllegalStateException("Algorithme SHA-256 indisponible.", e);
        }
    }

    /**
     * Compare un mot de passe en clair à une empreinte déjà calculée.
     *
     * @param texteClair le mot de passe saisi par l'utilisateur
     * @param empreinte  l'empreinte stockée à comparer
     * @return true si le hachage du texte clair correspond à l'empreinte
     */
    public static boolean verifier(String texteClair, String empreinte) {
        if (texteClair == null || empreinte == null) {
            return false;
        }
        return hacher(texteClair).equals(empreinte);
    }
}
