package com.mahghuuuls.jassintegrations.integration;

import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Decides once, at startup, which integrations run, and writes one status line for each. An
 * integration's entry class is loaded by name only after its target mod is known to be loaded,
 * so no target-mod class is ever touched when the target is absent.
 */
public final class IntegrationGate {

    /** Integration name, target mod id, config switch, entry class. Order is the startup line order. */
    static final List<Entry> ENTRIES = Collections.unmodifiableList(Arrays.asList(
            new Entry("Spartan Weaponry", "spartanweaponry", s -> s.spartanWeaponryEnabled,
                    "com.mahghuuuls.jassintegrations.integration.spartanweaponry.SpartanWeaponryIntegration"),
            new Entry("Spartan Shields", "spartanshields", s -> s.spartanShieldsEnabled,
                    "com.mahghuuuls.jassintegrations.integration.spartanshields.SpartanShieldsIntegration")
    ));

    private static final List<Integration> started = new ArrayList<>();

    private IntegrationGate() {
    }

    public enum State {
        RUNNING("running"),
        OFF_BY_CONFIG("off (switched off in the config)"),
        TARGET_MISSING("off (target mod not installed)"),
        INCOMPATIBLE("off (this version of the target mod is not supported)");

        final String text;

        State(String text) {
            this.text = text;
        }
    }

    /** What an integration's entry class provides. Called only when its target mod is loaded. */
    public interface Integration {

        /** Problems that stop the whole integration; empty when it can run. */
        List<String> compatibilityProblems();

        /** Registers the integration's providers and handlers. Called during pre-initialization. */
        void start();

        /** Runs after every mod has registered its items. */
        default void postInit() {
        }
    }

    static final class Entry {
        final String name;
        final String targetModId;
        final Predicate<Settings> enabled;
        final String entryClass;

        Entry(String name, String targetModId, Predicate<Settings> enabled, String entryClass) {
            this.name = name;
            this.targetModId = targetModId;
            this.enabled = enabled;
            this.entryClass = entryClass;
        }
    }

    static State decide(boolean targetLoaded, boolean enabled, boolean compatible) {
        if (!targetLoaded) {
            return State.TARGET_MISSING;
        }
        if (!enabled) {
            return State.OFF_BY_CONFIG;
        }
        return compatible ? State.RUNNING : State.INCOMPATIBLE;
    }

    /** Starts every integration that can run. Called once during pre-initialization. */
    public static void startAll() {
        Settings settings = Settings.current();
        for (Entry entry : ENTRIES) {
            boolean loaded = Loader.isModLoaded(entry.targetModId);
            boolean enabled = entry.enabled.test(settings);
            Integration integration = null;
            List<String> problems = Collections.emptyList();
            if (loaded && enabled) {
                try {
                    integration = (Integration) Class.forName(entry.entryClass).newInstance();
                    problems = integration.compatibilityProblems();
                } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                    problems = Collections.singletonList(e.toString());
                }
            }
            State state = decide(loaded, enabled, problems.isEmpty());
            if (state == State.INCOMPATIBLE) {
                DebugLog.warn(entry.name + ": " + state.text + ": " + String.join("; ", problems));
                continue;
            }
            if (state != State.RUNNING) {
                DebugLog.status(entry.name, state.text);
                continue;
            }
            try {
                integration.start();
            } catch (LinkageError | RuntimeException e) {
                DebugLog.warn(entry.name + ": off (failed to start: " + e + ")");
                continue;
            }
            started.add(integration);
            DebugLog.status(entry.name, state.text);
        }
    }

    /** Lets running integrations act once every mod has registered its items. */
    public static void postInitAll() {
        for (Integration integration : started) {
            integration.postInit();
        }
    }

    /** Names of classes that are not on the class path, without loading or initializing the others' dependents. */
    public static List<String> missingClasses(Iterable<String> classNames) {
        List<String> missing = new ArrayList<>();
        ClassLoader loader = IntegrationGate.class.getClassLoader();
        for (String name : classNames) {
            try {
                Class.forName(name, false, loader);
            } catch (ClassNotFoundException | LinkageError e) {
                missing.add(name);
            }
        }
        return missing;
    }
}
