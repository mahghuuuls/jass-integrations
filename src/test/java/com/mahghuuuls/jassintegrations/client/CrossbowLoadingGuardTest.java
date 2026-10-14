package com.mahghuuuls.jassintegrations.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrossbowLoadingGuardTest {

    /** REQ-033: the start rule does not depend on the loading drain, so there is no drain argument. */
    @Test
    void loadingRefusedOnlyWithASnapshotThatCannotSpend() {
        assertTrue(CrossbowLoadingGuard.refuse(true, false));
        assertFalse(CrossbowLoadingGuard.refuse(false, false));
        assertFalse(CrossbowLoadingGuard.refuse(true, true));
    }
}
