package com.mahghuuuls.jassintegrations.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShieldBashSwingTest {

    @Test
    void swingSkippedOnlyWhenActiveCostlySnapshotReceivedAndCannotSpend() {
        assertTrue(ShieldBashSwing.suppress(true, 8.0, true, false));
        assertFalse(ShieldBashSwing.suppress(false, 8.0, true, false));
        assertFalse(ShieldBashSwing.suppress(true, 0.0, true, false));
        assertFalse(ShieldBashSwing.suppress(true, 8.0, false, false));
        assertFalse(ShieldBashSwing.suppress(true, 8.0, true, true));
    }
}
