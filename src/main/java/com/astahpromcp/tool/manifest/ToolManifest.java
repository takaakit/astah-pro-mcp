package com.astahpromcp.tool.manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// The single record of which tools each profile publishes.
public final class ToolManifest {

    public static final String RESOURCE_PATH = "/toolmanifest/tools.tsv";

    // The directive that ends the body section and begins the withheld section
    private static final String WITHHELD_DIRECTIVE = "#@withheld";

    private static final int COLUMN_COUNT = 4;

    public enum Profile {
        DIRECT,
        PROGRAMMATIC
    }

    public enum Category {
        COMMON("Common"),
        CLASS_DIAGRAM("Class Diagram"),
        SEQUENCE_DIAGRAM("Sequence Diagram"),
        ACTIVITY_DIAGRAM("Activity Diagram"),
        STATE_MACHINE_DIAGRAM("State Machine Diagram"),
        USE_CASE_DIAGRAM("Use Case Diagram"),
        COMMUNICATION_DIAGRAM("Communication Diagram"),
        COMPOSITE_STRUCTURE_DIAGRAM("Composite Structure Diagram"),
        REQUIREMENT_DIAGRAM("Requirement Diagram"),
        ER_DIAGRAM("ER Diagram"),
        MIND_MAP_DIAGRAM("Mind Map Diagram");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        // The category whose label is exactly the given text, or null when no category has it
        static Category ofLabel(String label) {
            for (Category category : values()) {
                if (category.label.equals(label)) {
                    return category;
                }
            }
            return null;
        }

        static List<String> labels() {
            List<String> labels = new ArrayList<>();
            for (Category category : values()) {
                labels.add(category.label);
            }
            return labels;
        }
    }

    // One row of the manifest
    public record Entry(Category category, String name, boolean direct, boolean programmatic, boolean withheld) {

        // Whether this row publishes to the given profile
        public boolean publishesTo(Profile profile) {
            return switch (profile) {
                case DIRECT -> direct;
                case PROGRAMMATIC -> programmatic;
            };
        }
    }

    private final Map<String, Entry> entriesByName;

    private ToolManifest(Map<String, Entry> entriesByName) {
        this.entriesByName = entriesByName;
    }

    // Read the manifest from the classpath. Any problem is fatal (see the class comment).
    public static ToolManifest load() {
        try (InputStream in = ToolManifest.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Tool manifest not found on the classpath: " + RESOURCE_PATH);
            }
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return parse(lines, RESOURCE_PATH);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the tool manifest: " + RESOURCE_PATH, e);
        }
    }

    // Parse the given lines. Exposed so that a test can feed a deliberately broken manifest.
    public static ToolManifest parse(List<String> lines, String source) {
        Map<String, Entry> entries = new LinkedHashMap<>();
        Set<String> allDashInBody = new TreeSet<>();
        Set<String> publishedInWithheld = new TreeSet<>();
        boolean withheldSection = false;

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            int lineNumber = i + 1;
            String trimmed = raw.trim();

            if (trimmed.equals(WITHHELD_DIRECTIVE)) {
                withheldSection = true;
                continue;
            }
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            Entry entry = parseRow(raw, lineNumber, source, withheldSection);

            // A name may appear only once
            if (entries.containsKey(entry.name())) {
                throw new IllegalStateException(
                        source + ":" + lineNumber + ": tool '" + entry.name() + "' appears more than once");
            }
            entries.put(entry.name(), entry);

            // An all-dash row belongs to the withheld section, and every withheld row is all-dash
            boolean allDash = !entry.direct() && !entry.programmatic();
            if (allDash && !withheldSection) {
                allDashInBody.add(entry.name());
            }
            if (!allDash && withheldSection) {
                publishedInWithheld.add(entry.name());
            }
        }

        if (!allDashInBody.isEmpty()) {
            throw new IllegalStateException(source + ": these rows publish to no profile but are outside the '"
                    + WITHHELD_DIRECTIVE + "' section, so an intentional withholding cannot be told apart from a"
                    + " forgotten 'x': " + allDashInBody);
        }
        if (!publishedInWithheld.isEmpty()) {
            throw new IllegalStateException(source + ": these rows are in the '" + WITHHELD_DIRECTIVE
                    + "' section but still publish to a profile: " + publishedInWithheld);
        }

        return new ToolManifest(entries);
    }

    // Exactly four columns, a known category, a non-empty name, and every flag either 'x' or '-'
    private static Entry parseRow(String raw, int lineNumber, String source, boolean withheld) {
        String[] columns = raw.split("\t", -1);
        if (columns.length != COLUMN_COUNT) {
            throw new IllegalStateException(source + ":" + lineNumber + ": expected " + COLUMN_COUNT
                    + " tab-separated columns but found " + columns.length + ": " + raw.trim());
        }

        Category category = parseCategory(columns[0], lineNumber, source);

        String name = columns[1].trim();
        if (name.isEmpty()) {
            throw new IllegalStateException(source + ":" + lineNumber + ": the tool name is empty");
        }

        return new Entry(category,
                name,
                parseFlag(columns[2], "direct", lineNumber, source),
                parseFlag(columns[3], "programmatic", lineNumber, source),
                withheld);
    }

    private static Category parseCategory(String column, int lineNumber, String source) {
        String value = column.trim();
        Category category = Category.ofLabel(value);
        if (category == null) {
            throw new IllegalStateException(source + ":" + lineNumber + ": column 'category' must be one of " + Category.labels() + " but was '" + value + "'");
        }
        return category;
    }

    private static boolean parseFlag(String column, String columnName, int lineNumber, String source) {
        String value = column.trim();
        if (value.equals("x")) {
            return true;
        }
        if (value.equals("-")) {
            return false;
        }
        throw new IllegalStateException(source + ":" + lineNumber + ": column '" + columnName
                + "' must be 'x' or '-' but was '" + value + "'");
    }

    // The tools the given profile publishes, in file order
    public Set<String> namesFor(Profile profile) {
        Set<String> names = new LinkedHashSet<>();
        for (Entry entry : entriesByName.values()) {
            if (entry.publishesTo(profile)) {
                names.add(entry.name());
            }
        }
        return names;
    }

    // The tools the withheld section holds, in file order
    public Set<String> withheldNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Entry entry : entriesByName.values()) {
            if (entry.withheld()) {
                names.add(entry.name());
            }
        }
        return names;
    }

    public Set<String> allNames() {
        return new LinkedHashSet<>(entriesByName.keySet());
    }

    public Entry find(String name) {
        return entriesByName.get(name);
    }

    public int size() {
        return entriesByName.size();
    }
}
