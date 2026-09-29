package com.ictu.banque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduit le jeu d'essai du rapport (section 5 — Tests et résultats).
 * Chaque banque de test écrit son propre journal.log dans un dossier
 * temporaire, pour ne jamais polluer le dépôt ni les autres tests.
 */
class BanqueTest {

    @TempDir
    Path dossierTemp;

    private Banque banque;

    @BeforeEach
    void setUp() {
        banque = new Banque(new Journal(dossierTemp.resolve("journal.log").toString()));
    }

    // 1. Créer Jordan (dépôt 200 000) et Alexandre (50 000) -> comptes CM00001 et CM00002
    @Test
    void creationDeDeuxComptesGenereDesNumerosSequentiels() {
        Compte jordan = banque.creerCompte("Jordan", "motdepasse", 200_000);
        Compte alexandre = banque.creerCompte("Alexandre", "motdepasse", 50_000);
        assertEquals("CM00001", jordan.getNumero());
        assertEquals("CM00002", alexandre.getNumero());
        assertEquals(2, banque.getNbComptes());
    }

    @Test
    void creationRefuseeSiDepotNegatifOuMotDePasseTropCourt() {
        assertThrows(IllegalArgumentException.class, () -> banque.creerCompte("X", "motdepasse", -1));
        assertThrows(IllegalArgumentException.class, () -> banque.creerCompte("X", "abc", 1000));
    }

    // 2. Connexion CM00001 avec le bon mot de passe -> authentification réussie
    @Test
    void authentificationReussieAvecLeBonMotDePasse() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 200_000);
        Compte connecte = banque.authentifier(c.getNumero(), "motdepasse");
        assertNotNull(connecte);
        assertEquals(c.getNumero(), connecte.getNumero());
    }

    @Test
    void authentificationEchoueAvecUnMauvaisMotDePasse() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 200_000);
        assertNull(banque.authentifier(c.getNumero(), "faux"));
    }

    // 3. Retrait de 500 000 FCFA sur un solde de 200 000 -> refusé (solde insuffisant)
    @Test
    void retraitSuperieurAuSoldeEstRefuse() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 200_000);
        assertFalse(banque.retirer(c.getNumero(), 500_000));
        assertEquals(200_000, c.getSolde());
    }

    // 4. Virement de 100 000 vers CM00002 -> effectué, les deux soldes mis à jour
    @Test
    void virementMetAJourLesDeuxSoldes() {
        Compte jordan = banque.creerCompte("Jordan", "motdepasse", 200_000);
        Compte alexandre = banque.creerCompte("Alexandre", "motdepasse", 50_000);
        assertTrue(banque.virer(jordan.getNumero(), alexandre.getNumero(), 100_000));
        assertEquals(100_000, jordan.getSolde());
        assertEquals(150_000, alexandre.getSolde());
    }

    @Test
    void virementVersSoiMemeEstRefuse() {
        Compte jordan = banque.creerCompte("Jordan", "motdepasse", 200_000);
        assertFalse(banque.virer(jordan.getNumero(), jordan.getNumero(), 1000));
    }

    @Test
    void virementVersUnCompteInexistantEstRefuse() {
        Compte jordan = banque.creerCompte("Jordan", "motdepasse", 200_000);
        assertFalse(banque.virer(jordan.getNumero(), "CM99999", 1000));
    }

    @Test
    void virementVersUnCompteBloqueEstRefuse() {
        Compte jordan = banque.creerCompte("Jordan", "motdepasse", 200_000);
        Compte alexandre = banque.creerCompte("Alexandre", "motdepasse", 50_000);
        banque.bloquerCompte(alexandre.getNumero());
        assertFalse(banque.virer(jordan.getNumero(), alexandre.getNumero(), 1000));
    }

    // 5. Quatre retraits rapides de 1 000 -> le 4e est marqué suspect (fréquence anormale)
    @Test
    void quatriemeRetraitRapideEstMarqueSuspectPourFrequenceAnormale() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 100_000);
        for (int i = 0; i < 3; i++) {
            assertTrue(banque.retirer(c.getNumero(), 1000));
        }
        assertTrue(banque.retirer(c.getNumero(), 1000));

        List<Transaction> historique = banque.historique(c.getNumero());
        assertEquals(4, historique.size());
        assertFalse(historique.get(0).isSuspecte());
        assertFalse(historique.get(1).isSuspecte());
        assertFalse(historique.get(2).isSuspecte());
        assertTrue(historique.get(3).isSuspecte());
        assertEquals("Frequence anormale", historique.get(3).getMotif());
    }

    @Test
    void montantSuperieurOuEgalAuSeuilEstMarqueSuspectPourMontantEleve() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 1_000_000);
        assertTrue(banque.retirer(c.getNumero(), Banque.SEUIL_MONTANT));
        List<Transaction> h = banque.historique(c.getNumero());
        assertTrue(h.get(0).isSuspecte());
        assertEquals("Montant eleve", h.get(0).getMotif());
    }

    @Test
    void transactionSuspecteResteVisibleDansLHistoriqueEtLaListeDesSuspectes() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 1_000_000);
        banque.retirer(c.getNumero(), Banque.SEUIL_MONTANT);
        assertEquals(1, banque.historique(c.getNumero()).size());
        assertEquals(1, banque.transactionsSuspectes().size());
    }

    // 6. Historique puis export du relevé -> fichier releve_CM00001.txt créé
    @Test
    void exportDuReleveCreeUnFichierAvecLeBonNom() throws IOException {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 200_000);
        banque.deposer(c.getNumero(), 10_000);
        String cheminAttendu = "releve_" + c.getNumero() + ".txt";
        try {
            String nomFichier = banque.exporterReleve(c.getNumero());
            assertEquals(cheminAttendu, nomFichier);
            Path fichier = Path.of(nomFichier);
            assertTrue(Files.exists(fichier));
            String contenu = Files.readString(fichier);
            assertTrue(contenu.contains(c.getNumero()));
            assertTrue(contenu.contains("Jordan"));
        } finally {
            Files.deleteIfExists(Path.of(cheminAttendu));
        }
    }

    @Test
    void exportDUnCompteInexistantEchoue() {
        assertThrows(IllegalArgumentException.class, () -> banque.exporterReleve("CM99999"));
    }

    // 7. Trois mauvais mots de passe -> compte bloqué
    @Test
    void troisMauvaisMotsDePasseBloquentLeCompte() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 200_000);
        for (int i = 0; i < 3; i++) {
            assertNull(banque.authentifier(c.getNumero(), "faux" + i));
        }
        assertTrue(c.isBloque());
        assertNull(banque.authentifier(c.getNumero(), "motdepasse")); // même le bon mot de passe est refusé
    }

    // 8. Administration : suspectes, débloquer -> liste affichée, compte réactivé
    @Test
    void administrationPeutListerLesComptesEtDebloquer() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 200_000);
        for (int i = 0; i < 3; i++) {
            banque.authentifier(c.getNumero(), "faux");
        }
        assertTrue(c.isBloque());
        assertEquals(1, banque.listerComptes().size());

        assertTrue(banque.debloquerCompte(c.getNumero()));
        assertFalse(c.isBloque());
        assertNotNull(banque.authentifier(c.getNumero(), "motdepasse"));
    }

    @Test
    void bloquerOuDebloquerUnCompteInexistantRetourneFaux() {
        assertFalse(banque.bloquerCompte("CM99999"));
        assertFalse(banque.debloquerCompte("CM99999"));
    }

    @Test
    void depotRefusePourUnCompteBloqueOuMontantInvalide() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 200_000);
        assertFalse(banque.deposer(c.getNumero(), -100));
        banque.bloquerCompte(c.getNumero());
        assertFalse(banque.deposer(c.getNumero(), 1000));
    }

    @Test
    void rechercherCompteEstInsensibleALaCasseEtRetourneNullSiIntrouvable() {
        Compte c = banque.creerCompte("Jordan", "motdepasse", 1000);
        assertSame(c, banque.rechercherCompte(c.getNumero().toLowerCase()));
        assertNull(banque.rechercherCompte("inexistant"));
        assertNull(banque.rechercherCompte(null));
    }
}
