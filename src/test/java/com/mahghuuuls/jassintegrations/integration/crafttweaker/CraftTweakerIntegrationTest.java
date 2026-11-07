package com.mahghuuuls.jassintegrations.integration.crafttweaker;

import com.mahghuuuls.jass.api.StaminaMutationResult;
import com.mahghuuuls.jass.api.StaminaPublicState;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftTweakerIntegrationTest {

    private static final double EPSILON = 1e-9;

    @Test
    void theApiIsFoundOnTheDevelopmentCraftTweaker() {
        assertEquals(Collections.emptyList(), new CraftTweakerIntegration().compatibilityProblems());
    }

    @Test
    void theScriptClassHasTheApprovedNameAndMethods() {
        assertEquals("mods.jassintegrations.Stamina", StaminaScripts.class.getAnnotation(ZenClass.class).value());
        Set<String> methods = new TreeSet<>();
        for (Method method : StaminaScripts.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ZenMethod.class)) {
                assertTrue(Modifier.isStatic(method.getModifiers()), method.getName());
                methods.add(method.getName());
            }
        }
        assertEquals(new TreeSet<>(Arrays.asList("canStartAction", "getDebt", "getEfficiency", "getMaxStamina",
                "getRegeneration", "getRegenerationDelay", "getStamina", "getVisibleStamina", "getWeight",
                "isSpendingPaused", "removeStamina", "restoreStamina", "setStamina", "spendStamina")), methods);
    }

    @Test
    void theScriptClassIsNotPickedUpByCraftTweakersOwnScan() {
        // The gate registers it, so the config switch can leave it out.
        assertFalse(Arrays.stream(StaminaScripts.class.getAnnotations())
                .anyMatch(a -> a.annotationType().getName().equals("crafttweaker.annotations.ZenRegister")));
    }

    @Test
    void actionIdsAreLowerCaseNamespaceAndPath() {
        ResourceLocation id = StaminaScriptCalls.actionId("mypack:dash");
        assertNotNull(id);
        assertEquals("mypack", id.getNamespace());
        assertEquals("dash", id.getPath());
        assertNotNull(StaminaScriptCalls.actionId("my_pack.x:skills/dash-2"));
        assertNull(StaminaScriptCalls.actionId("dash"), "a namespace is required");
        assertNull(StaminaScriptCalls.actionId("Bad Id!"));
        assertNull(StaminaScriptCalls.actionId("MyPack:dash"), "upper case would be changed silently by Minecraft");
        assertNull(StaminaScriptCalls.actionId(":dash"));
        assertNull(StaminaScriptCalls.actionId("mypack:"));
        assertNull(StaminaScriptCalls.actionId(null));
    }

    @Test
    void onlyFiniteNumbersPass() {
        assertTrue(StaminaScriptCalls.finite(0));
        assertTrue(StaminaScriptCalls.finite(-5));
        assertFalse(StaminaScriptCalls.finite(Double.NaN));
        assertFalse(StaminaScriptCalls.finite(Double.POSITIVE_INFINITY));
    }

    @Test
    void amountsAndCostsMustBeFiniteAndNotNegative() {
        assertNull(StaminaScriptCalls.amountProblem("amount", 0));
        assertNull(StaminaScriptCalls.amountProblem("amount", 5));
        assertTrue(StaminaScriptCalls.amountProblem("amount", -5).contains("amount -5.0"));
        assertTrue(StaminaScriptCalls.amountProblem("cost", -0.5).startsWith("cost "));
        assertNotNull(StaminaScriptCalls.amountProblem("amount", Double.NaN));
        assertNotNull(StaminaScriptCalls.amountProblem("amount", Double.POSITIVE_INFINITY));
        assertNotNull(StaminaScriptCalls.amountProblem("amount", Double.NEGATIVE_INFINITY));
    }

    @Test
    void aSetValueMayBeNegativeButMustBeFinite() {
        assertNull(StaminaScriptCalls.valueProblem(30));
        assertNull(StaminaScriptCalls.valueProblem(-10), "JASS clamps a value below the debt floor itself");
        assertNotNull(StaminaScriptCalls.valueProblem(Double.NaN));
        assertNotNull(StaminaScriptCalls.valueProblem(Double.NEGATIVE_INFINITY));
    }

    @Test
    void anInvalidActionIdNamesTheAction() {
        assertNull(StaminaScriptCalls.actionProblem("jit:dash"));
        assertTrue(StaminaScriptCalls.actionProblem("Bad Id!").contains("'Bad Id!'"));
    }

    @Test
    void theChangeIsTheDifferenceOfTheStoredStamina() {
        ResourceLocation cause = StaminaScriptCalls.SCRIPT_CAUSE;
        StaminaMutationResult removed = new StaminaMutationResult(true, 20, cause,
                new StaminaPublicState(60, 60), new StaminaPublicState(40, 60));
        assertEquals(-20.0, StaminaScriptCalls.change(removed), EPSILON);
        StaminaMutationResult restored = new StaminaMutationResult(true, 50, cause,
                new StaminaPublicState(40, 60), new StaminaPublicState(60, 60));
        assertEquals(20.0, StaminaScriptCalls.change(restored), EPSILON, "clamped at the maximum");
        assertEquals(0.0, StaminaScriptCalls.change(null), EPSILON);
    }

    @Test
    void withoutAPlayerEveryCallIsNeutralAndSilent() {
        List<String> warnings = new ArrayList<>();
        StaminaScriptCalls calls = new StaminaScriptCalls(warnings::add);
        assertEquals(0.0, calls.stamina(null), EPSILON);
        assertEquals(0.0, calls.maxStamina(null), EPSILON);
        assertFalse(calls.spendingPaused(null));
        assertEquals(0.0, calls.set(null, Double.NaN), EPSILON);
        assertEquals(0.0, calls.remove(null, -5), EPSILON);
        assertFalse(calls.spend(null, "Bad Id!", 10, false));
        assertFalse(calls.canStart(null, "mypack:dash"));
        assertEquals(Collections.emptyList(), warnings, "a call that cannot reach a player's stamina warns about nothing");
    }
}
