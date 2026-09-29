package com.ictu.banque;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void montantNulOuNegatifEstRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(Transaction.Type.DEPOT, null, "CM00001", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(Transaction.Type.DEPOT, null, "CM00001", -50));
    }

    @Test
    void concerneDetecteLaSourceEtLaDestination() {
        Transaction t = new Transaction(Transaction.Type.VIREMENT, "CM00001", "CM00002", 1000);
        assertTrue(t.concerne("CM00001"));
        assertTrue(t.concerne("CM00002"));
        assertFalse(t.concerne("CM00003"));
        assertFalse(t.concerne(null));
    }

    @Test
    void marquerSuspecteEnregistreLeMotif() {
        Transaction t = new Transaction(Transaction.Type.RETRAIT, "CM00001", null, 600_000);
        assertFalse(t.isSuspecte());
        t.marquerSuspecte("Montant eleve");
        assertTrue(t.isSuspecte());
        assertEquals("Montant eleve", t.getMotif());
    }
}
