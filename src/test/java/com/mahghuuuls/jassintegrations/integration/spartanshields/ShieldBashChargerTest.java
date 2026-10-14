package com.mahghuuuls.jassintegrations.integration.spartanshields;

import org.junit.jupiter.api.Test;

import net.minecraft.util.EnumHand;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShieldBashChargerTest {

    @Test
    void aBashThatSpartanWouldNotPerformIsNeverCharged() {
        assertEquals(ShieldBashCharger.Decision.NOT_A_BASH, ShieldBashCharger.decide(false, true, false, 8.0));
        assertEquals(ShieldBashCharger.Decision.NOT_A_BASH, ShieldBashCharger.decide(true, false, false, 8.0));
        assertEquals(ShieldBashCharger.Decision.NOT_A_BASH, ShieldBashCharger.decide(true, true, true, 8.0));
    }

    @Test
    void costZeroIsFreeAndAboveZeroIsCharged() {
        assertEquals(ShieldBashCharger.Decision.FREE, ShieldBashCharger.decide(true, true, false, 0.0));
        assertEquals(ShieldBashCharger.Decision.CHARGE, ShieldBashCharger.decide(true, true, false, 8.0));
        assertEquals(ShieldBashCharger.Decision.CHARGE, ShieldBashCharger.decide(true, true, false, 0.5));
    }

    /**
     * Spartan Shields 1.5.5 is on the test class path, without Mixins applied: both patched methods
     * exist (no "missing" problem), and the missing marker is reported as "not applied".
     */
    @Test
    void realSpartanTargetsExistAndUnpatchedClassesAreReported() {
        List<String> problems = SpartanShieldsIntegration.bashHookProblems(true,
                SpartanShieldsIntegration.PACKET_CLASS, SpartanShieldsIntegration.CLIENT_HANDLER_CLASS);
        assertEquals(2, problems.size(), problems.toString());
        assertTrue(problems.get(0).startsWith("bash Mixin not applied"), problems.get(0));
        assertTrue(problems.get(1).startsWith("swing Mixin not applied"), problems.get(1));
    }

    @Test
    void missingMethodsAreReportedAndTheClientHandlerOnlyOnTheClient() {
        List<String> client = SpartanShieldsIntegration.bashHookProblems(true, "java.lang.String", "java.lang.String");
        assertEquals(2, client.size(), client.toString());
        assertTrue(client.get(0).startsWith("missing java.lang.String#handleServerSide"), client.get(0));
        assertTrue(client.get(1).startsWith("missing java.lang.String#onInputEvent"), client.get(1));
        List<String> server = SpartanShieldsIntegration.bashHookProblems(false, "java.lang.String", "java.lang.String");
        assertEquals(1, server.size(), server.toString());
    }

    @Test
    void aProblemLeavesTheBashCostOff() {
        assertFalse(SpartanShieldsIntegration.activateBash(Collections.singletonList("missing X#handleServerSide")));
        assertFalse(ShieldBashCharger.isActive());
        assertTrue(ShieldBashCharger.allowBash(null, EnumHand.MAIN_HAND));
    }
}
