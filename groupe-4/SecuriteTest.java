package com.ictu.banque;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecuriteTest {

    @Test
    void hacherProduitUneEmpreinteDe64CaracteresHexadecimaux() {
        String empreinte = Securite.hacher("motdepasse");
        assertEquals(64, empreinte.length());
        assertTrue(empreinte.matches("[0-9a-f]+"));
    }

    @Test
    void hacherEstDeterministe() {
        assertEquals(Securite.hacher("abc123"), Securite.hacher("abc123"));
    }

    @Test
    void deuxTextesDifferentsDonnentDesEmpreintesDifferentes() {
        assertNotEquals(Securite.hacher("abc123"), Securite.hacher("abc124"));
    }

    @Test
    void verifierReconnaitLeBonMotDePasse() {
        String empreinte = Securite.hacher("SuperSecret1");
        assertTrue(Securite.verifier("SuperSecret1", empreinte));
    }

    @Test
    void verifierRejetteLeMauvaisMotDePasse() {
        String empreinte = Securite.hacher("SuperSecret1");
        assertFalse(Securite.verifier("MauvaisMotDePasse", empreinte));
    }

    @Test
    void hacherRefuseNull() {
        assertThrows(IllegalArgumentException.class, () -> Securite.hacher(null));
    }
}
