package com.ictu.banque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompteTest {

    private Compte compte;

    @BeforeEach
    void setUp() {
        compte = new Compte("CM00001", "Jordan", Securite.hacher("motdepasse"), 100_000);
    }

    @Test
    void creerUnCompteAvecSoldeInitialNegatifEstRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> new Compte("CM00002", "Alexandre", Securite.hacher("x"), -1));
    }

    @Test
    void crediterAugmenteLeSolde() {
        compte.crediter(50_000);
        assertEquals(150_000, compte.getSolde());
    }

    @Test
    void debiterDiminueLeSolde() {
        compte.debiter(30_000);
        assertEquals(70_000, compte.getSolde());
    }

    @Test
    void debiterPlusQueLeSoldeEstRefuse() {
        assertThrows(IllegalStateException.class, () -> compte.debiter(200_000));
    }

    @Test
    void montantNegatifOuNulEstRefusePourCrediterEtDebiter() {
        assertThrows(IllegalArgumentException.class, () -> compte.crediter(0));
        assertThrows(IllegalArgumentException.class, () -> compte.debiter(-10));
    }

    @Test
    void troisEchecsBloquentLeCompte() {
        assertFalse(compte.isBloque());
        compte.ajouterEchec();
        compte.ajouterEchec();
        assertFalse(compte.isBloque());
        compte.ajouterEchec();
        assertTrue(compte.isBloque());
    }

    @Test
    void reinitialiserEchecsRemetLeCompteurAZero() {
        compte.ajouterEchec();
        compte.ajouterEchec();
        compte.reinitialiserEchecs();
        assertEquals(0, compte.getEchecs());
    }

    @Test
    void debloquerReactiveLeCompteEtRemetLesEchecsAZero() {
        compte.ajouterEchec();
        compte.ajouterEchec();
        compte.ajouterEchec();
        assertTrue(compte.isBloque());
        compte.debloquer();
        assertFalse(compte.isBloque());
        assertEquals(0, compte.getEchecs());
    }
}
