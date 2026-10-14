package com.mahghuuuls.jassintegrations.integration;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationGateTest {

    @Test
    void missingTargetWinsOverEveryOtherState() {
        assertEquals(IntegrationGate.State.TARGET_MISSING, IntegrationGate.decide(false, true, true));
        assertEquals(IntegrationGate.State.TARGET_MISSING, IntegrationGate.decide(false, false, false));
    }

    @Test
    void switchedOffIntegrationIsOffByConfig() {
        assertEquals(IntegrationGate.State.OFF_BY_CONFIG, IntegrationGate.decide(true, false, true));
    }

    @Test
    void incompatibleTargetIsReported() {
        assertEquals(IntegrationGate.State.INCOMPATIBLE, IntegrationGate.decide(true, true, false));
    }

    @Test
    void loadedEnabledCompatibleRuns() {
        assertEquals(IntegrationGate.State.RUNNING, IntegrationGate.decide(true, true, true));
    }

    @Test
    void missingClassesNamesOnlyAbsentClasses() {
        assertEquals(Collections.singletonList("com.example.NotThere"),
                IntegrationGate.missingClasses(Arrays.asList("java.lang.String", "com.example.NotThere")));
    }

    @Test
    void hasMethodChecksNameAndExactParameters() {
        assertEquals(true, IntegrationGate.hasMethod("java.lang.String", "valueOf", "java.lang.Object"));
        assertEquals(false, IntegrationGate.hasMethod("java.lang.String", "valueOf", "java.lang.Thread"));
        assertEquals(false, IntegrationGate.hasMethod("java.lang.String", "noSuchMethod", "java.lang.Object"));
        assertEquals(false, IntegrationGate.hasMethod("com.example.NotThere", "valueOf", "java.lang.Object"));
        assertEquals(false, IntegrationGate.hasMethod("java.lang.String", "valueOf", "com.example.NotThere"));
    }

    @Test
    void everyEntryClassExists() {
        for (IntegrationGate.Entry entry : IntegrationGate.ENTRIES) {
            assertEquals(Collections.emptyList(), IntegrationGate.missingClasses(Collections.singletonList(entry.entryClass)),
                    entry.name);
        }
    }
}
