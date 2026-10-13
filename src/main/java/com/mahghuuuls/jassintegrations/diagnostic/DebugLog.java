package com.mahghuuuls.jassintegrations.diagnostic;

import com.mahghuuuls.jassintegrations.config.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

/**
 * The only writer of this mod's log lines. Startup status lines and warnings are always written;
 * per-event lines only while {@code debug_logging} is on.
 */
public final class DebugLog {

    private static final Logger LOGGER = LogManager.getLogger("JASS Integrations");

    private DebugLog() {
    }

    /** One line per integration at startup: {@code <integration>: <state>}. */
    public static void status(String integration, String state) {
        LOGGER.info("{}: {}", integration, state);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static boolean enabled() {
        return Settings.current().debugLogging;
    }

    /** Writes the line only while {@code debug_logging} is on; the message is built only then. */
    public static void debug(Supplier<String> message) {
        if (enabled()) {
            LOGGER.info("[debug] {}", message.get());
        }
    }
}
