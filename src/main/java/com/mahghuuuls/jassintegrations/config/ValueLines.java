package com.mahghuuuls.jassintegrations.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one parser for the configuration's {@code key=value,value,...} lines. A line that cannot be
 * used is skipped with one warning naming the option and the line, and its key keeps its default.
 */
final class ValueLines {

    /** Checks one field; returns a problem description, or {@code null} when the value is usable. */
    interface FieldRule {
        String check(double value);
    }

    static final FieldRule ZERO_OR_MORE = v -> v >= 0.0 ? null : "must be 0 or more";
    static final FieldRule SHARE = v -> v >= 0.0 && v <= 1.0 ? null : "must be between 0 and 1";

    private final String option;
    private final String format;
    private final FieldRule[] rules;
    private final boolean emptyAllowed;

    /**
     * @param option       the config key named in warnings, for example {@code spartan_weaponry.weapon_types}
     * @param format       the line format named in warnings
     * @param emptyAllowed whether an empty field is accepted as {@code NaN} ("not supplied")
     */
    ValueLines(String option, String format, boolean emptyAllowed, FieldRule... rules) {
        this.option = option;
        this.format = format;
        this.rules = rules;
        this.emptyAllowed = emptyAllowed;
    }

    /**
     * Parses {@code lines} on top of {@code defaults}. With {@code defaults} given, only its keys are
     * accepted; with {@code null}, any key is accepted (used to read the shipped defaults themselves).
     */
    Map<String, double[]> parse(String[] lines, Map<String, double[]> defaults, List<String> warnings) {
        Map<String, double[]> result = new LinkedHashMap<>();
        if (defaults != null) {
            result.putAll(defaults);
        }
        List<String> seen = new ArrayList<>();
        for (String raw : lines == null ? new String[0] : lines) {
            String line = raw == null ? "" : raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals <= 0) {
                warnings.add(rejected(line, "expected " + format));
                continue;
            }
            String key = line.substring(0, equals).trim();
            if (defaults != null && !defaults.containsKey(key)) {
                warnings.add(rejected(line, "unknown name \"" + key + "\""));
                continue;
            }
            if (seen.contains(key)) {
                warnings.add(rejected(line, "\"" + key + "\" is listed twice"));
                continue;
            }
            String[] fields = line.substring(equals + 1).split(",", -1);
            if (fields.length != rules.length) {
                warnings.add(rejected(line, "expected " + format));
                continue;
            }
            double[] values = new double[rules.length];
            String problem = null;
            for (int i = 0; i < rules.length && problem == null; i++) {
                String field = fields[i].trim();
                if (field.isEmpty()) {
                    values[i] = Double.NaN;
                    if (!emptyAllowed) {
                        problem = "every value is required";
                    }
                    continue;
                }
                try {
                    values[i] = Double.parseDouble(field);
                } catch (NumberFormatException e) {
                    problem = "\"" + field + "\" is not a number";
                    continue;
                }
                if (Double.isNaN(values[i]) || Double.isInfinite(values[i])) {
                    problem = "\"" + field + "\" is not a number";
                    continue;
                }
                String ruleProblem = rules[i].check(values[i]);
                if (ruleProblem != null) {
                    problem = "\"" + field + "\" " + ruleProblem;
                }
            }
            if (problem != null) {
                warnings.add(rejected(line, problem));
                continue;
            }
            seen.add(key);
            result.put(key, values);
        }
        return result;
    }

    private String rejected(String line, String problem) {
        return option + " line \"" + line + "\" ignored (" + problem + "); the default is kept";
    }
}
