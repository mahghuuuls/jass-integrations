package com.mahghuuuls.jassintegrations.integration.spartanweaponry;

import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WeaponTypeClassifierTest {

    static class FakeWeaponBase {
    }

    static class FakeDagger extends FakeWeaponBase {
    }

    /** A mapped class below another mapped class: the nearer mapping must win. */
    static class FakeParryingDagger extends FakeDagger {
    }

    /** An add-on weapon that subclasses a mapped weapon class, like Spartan Netherite's daggers. */
    static class FakeAddonDagger extends FakeDagger {
    }

    static class FakeOther {
    }

    private static WeaponTypeClassifier fakeClassifier() {
        Map<String, String> types = new HashMap<>();
        types.put(FakeDagger.class.getName(), "dagger");
        types.put(FakeParryingDagger.class.getName(), "parrying_dagger");
        return new WeaponTypeClassifier(types);
    }

    @Test
    void mappedClassGivesItsType() {
        assertEquals("dagger", fakeClassifier().classify(FakeDagger.class));
        assertEquals("parrying_dagger", fakeClassifier().classify(FakeParryingDagger.class));
    }

    @Test
    void subclassOfMappedClassGivesTheNearestType() {
        assertEquals("dagger", fakeClassifier().classify(FakeAddonDagger.class));
    }

    @Test
    void unmappedClassesGiveNoType() {
        WeaponTypeClassifier classifier = fakeClassifier();
        assertNull(classifier.classify(FakeWeaponBase.class));
        assertNull(classifier.classify(FakeOther.class));
        assertNull(classifier.classify(FakeOther.class), "cached answer stays null");
    }

    @Test
    void nearerMappingWinsOverAMappedParent() {
        assertEquals("parrying_dagger", fakeClassifier().classify(FakeParryingDagger.class));
    }

    /** Name matching equals instanceof only while no mapped Spartan class extends another mapped one. */
    @Test
    void noMappedSpartanClassExtendsAnother() throws ClassNotFoundException {
        ClassLoader loader = getClass().getClassLoader();
        for (String name : WeaponTypeClassifier.SPARTAN_CLASS_TYPES.keySet()) {
            for (Class<?> c = Class.forName(name, false, loader).getSuperclass(); c != null; c = c.getSuperclass()) {
                assertNull(WeaponTypeClassifier.SPARTAN_CLASS_TYPES.get(c.getName()), name + " extends " + c.getName());
            }
        }
    }

    @Test
    void spartanTableCoversExactlyTheConfigWeaponTypes() {
        assertEquals(new HashSet<>(Settings.weaponTypeKeys()),
                new HashSet<>(WeaponTypeClassifier.SPARTAN_CLASS_TYPES.values()));
        assertEquals(26, WeaponTypeClassifier.SPARTAN_CLASS_TYPES.size());
    }

    /** Every class name in the table exists in Spartan Weaponry 1.6.1, which is on the test class path. */
    @Test
    void spartanClassNamesExistInSpartanWeaponry() {
        assertEquals(Collections.emptyList(),
                IntegrationGate.missingClasses(WeaponTypeClassifier.SPARTAN_CLASS_TYPES.keySet()));
    }
}
