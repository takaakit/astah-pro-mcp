package com.astahpromcp.tool.manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ToolManifest {

    public static final String RESOURCE_PATH = "/toolmanifest/tools.tsv";

    // category, name, direct and programmatic, then the reason, which a row without one may leave out.
    private static final int COLUMN_COUNT_WITHOUT_REASON = 4;
    private static final int COLUMN_COUNT_WITH_REASON = 5;

    // Each profile has a column of its own, which takes the values listed here
    public enum Profile {
        DIRECT("direct", Reach.TOOL_LIST, Reach.NONE),
        PROGRAMMATIC("programmatic", Reach.TOOL_LIST, Reach.MCP_TOOL_SCRIPT, Reach.NONE);

        private final String columnName;
        private final Set<Reach> reaches;

        Profile(String columnName, Reach... reaches) {
            this.columnName = columnName;
            this.reaches = EnumSet.copyOf(Arrays.asList(reaches));
        }

        public String columnName() {
            return columnName;
        }

        // Whether this profile's column takes the given value
        boolean takes(Reach reach) {
            return reaches.contains(reach);
        }

        // The codes this profile's column takes, in the order Reach declares them
        List<String> codes() {
            List<String> codes = new ArrayList<>();
            for (Reach reach : reaches) {
                codes.add(reach.code());
            }
            return codes;
        }
    }

    // How a profile reaches a tool: what the manifest writes in that profile's column. A value means the same in every column.
    public enum Reach {
        TOOL_LIST("x"),
        MCP_TOOL_SCRIPT("s"),
        NONE("-");

        private final String code;

        Reach(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        // The reach whose code is exactly the given text, or null when no reach has it
        static Reach ofCode(String code) {
            for (Reach reach : values()) {
                if (reach.code.equals(code)) {
                    return reach;
                }
            }
            return null;
        }
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
    }

    // One row of the manifest
    public record Entry(
        Category category,
        String name,
        Reach direct,
        Reach programmatic,
        NotMcpToolScriptCallableReason reason
    ) {

        // The value of the given profile's column
        public Reach reachOn(Profile profile) {
            return switch (profile) {
                case DIRECT -> direct;
                case PROGRAMMATIC -> programmatic;
            };
        }

        // Whether this row publishes to the given profile
        public boolean publishesTo(Profile profile) {
            return reachOn(profile) == Reach.TOOL_LIST;
        }

        public boolean reachable() {
            for (Profile profile : Profile.values()) {
                if (reachOn(profile) != Reach.NONE) {
                    return true;
                }
            }
            return false;
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

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            int lineNumber = i + 1;
            String trimmed = raw.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            Entry entry = parseRow(raw, lineNumber, source);

            // A name may appear only once
            if (entries.containsKey(entry.name())) {
                throw new IllegalStateException(
                        source + ":" + lineNumber + ": tool '" + entry.name() + "' appears more than once");
            }
            entries.put(entry.name(), entry);
        }

        return new ToolManifest(entries);
    }

    // Four or five columns, a known category, a non-empty name, in each profile's column a value that column takes,
    // column 'programmatic' reaching whatever column 'direct' publishes, and a known reason exactly when column 'programmatic' is 'x'
    private static Entry parseRow(String raw, int lineNumber, String source) {
        String[] columns = raw.split("\t", -1);
        if (columns.length != COLUMN_COUNT_WITHOUT_REASON && columns.length != COLUMN_COUNT_WITH_REASON) {
            throw new IllegalStateException(source + ":" + lineNumber + ": expected " + COLUMN_COUNT_WITHOUT_REASON
                    + " or " + COLUMN_COUNT_WITH_REASON + " tab-separated columns but found " + columns.length + ": " + raw.trim());
        }

        Category category = parseCategory(columns[0], lineNumber, source);

        String name = columns[1].trim();
        if (name.isEmpty()) {
            throw new IllegalStateException(source + ":" + lineNumber + ": the tool name is empty");
        }

        Reach direct = parseReach(columns[2], Profile.DIRECT, lineNumber, source);
        Reach programmatic = parseReach(columns[3], Profile.PROGRAMMATIC, lineNumber, source);
        NotMcpToolScriptCallableReason reason = columns.length == COLUMN_COUNT_WITH_REASON
                ? parseReason(columns[4], lineNumber, source)
                : null;

        // Whatever the direct profile publishes, the programmatic profile reaches too, or an agent there could not use it at all.
        if (direct == Reach.TOOL_LIST && programmatic == Reach.NONE) {
            throw new IllegalStateException(source + ":" + lineNumber + ": tool '" + name
                    + "' is published on the direct profile but the programmatic profile does not reach it. Set column 'programmatic' to '"
                    + Reach.TOOL_LIST.code() + "' or '" + Reach.MCP_TOOL_SCRIPT.code() + "'");
        }

        // A row with programmatic = x hands the tool to the agent directly on the programmatic profile, so an mcp tool script cannot call it and the row has to say why.
        boolean publishedDirectlyOnTheProgrammaticProfile = programmatic == Reach.TOOL_LIST;
        if (publishedDirectlyOnTheProgrammaticProfile && reason == null) {
            throw new IllegalStateException(source + ":" + lineNumber + ": tool '" + name
                    + "' is published directly on the programmatic profile, so column 'reason' has to say why an mcp tool script cannot call it: one of "
                    + NotMcpToolScriptCallableReason.codes());
        }
        if (!publishedDirectlyOnTheProgrammaticProfile && reason != null) {
            throw new IllegalStateException(source + ":" + lineNumber + ": tool '" + name
                    + "' is not published directly on the programmatic profile, so nothing would ever show its reason '" + reason.code()
                    + "'. Leave column 'reason' empty");
        }

        return new Entry(category, name, direct, programmatic, reason);
    }

    private static Category parseCategory(String column, int lineNumber, String source) {
        String value = column.trim();
        Category category = Category.ofLabel(value);
        if (category == null) {
            List<String> labels = new ArrayList<>();
            for (Category candidate : Category.values()) {
                labels.add(candidate.label());
            }

            throw new IllegalStateException(source + ":" + lineNumber + ": column 'category' must be one of " + labels + " but was '" + value + "'");
        }
        return category;
    }

    private static Reach parseReach(String column, Profile profile, int lineNumber, String source) {
        String value = column.trim();
        Reach reach = Reach.ofCode(value);
        if (reach == null || !profile.takes(reach)) {
            throw new IllegalStateException(source + ":" + lineNumber + ": column '" + profile.columnName()
                    + "' must be one of " + profile.codes() + " but was '" + value + "'");
        }
        return reach;
    }

    // An empty column is no reason
    private static NotMcpToolScriptCallableReason parseReason(String column, int lineNumber, String source) {
        String value = column.trim();
        if (value.isEmpty()) {
            return null;
        }
        NotMcpToolScriptCallableReason reason = NotMcpToolScriptCallableReason.ofCode(value);
        if (reason == null) {
            throw new IllegalStateException(source + ":" + lineNumber + ": column 'reason' must be one of "
                    + NotMcpToolScriptCallableReason.codes() + " but was '" + value + "'");
        }
        return reason;
    }

    // The tools the given profile publishes, in file order
    public Set<String> namesFor(Profile profile) {
        return namesFor(profile, Reach.TOOL_LIST);
    }

    // The tools whose column of the given profile holds the given value, in file order
    public Set<String> namesFor(Profile profile, Reach reach) {
        Set<String> names = new LinkedHashSet<>();
        for (Entry entry : entriesByName.values()) {
            if (entry.reachOn(profile) == reach) {
                names.add(entry.name());
            }
        }
        return names;
    }

    // The tools the given profile reaches in any way, in file order
    public Set<String> namesReachedOn(Profile profile) {
        Set<String> names = new LinkedHashSet<>();
        for (Entry entry : entriesByName.values()) {
            if (entry.reachOn(profile) != Reach.NONE) {
                names.add(entry.name());
            }
        }
        return names;
    }

    // Why an mcp tool script cannot call each tool the programmatic profile publishes directly, in file order
    public Map<String, NotMcpToolScriptCallableReason> notMcpToolScriptCallableReasons() {
        Map<String, NotMcpToolScriptCallableReason> reasons = new LinkedHashMap<>();
        for (Entry entry : entriesByName.values()) {
            if (entry.reason() != null) {
                reasons.put(entry.name(), entry.reason());
            }
        }
        return reasons;
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
