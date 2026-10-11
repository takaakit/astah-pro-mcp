package com.astahpromcp.tool;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// Counts the tool calls from the start of the server to its stop, which logs them.
@Slf4j
public final class ToolCallStatistics {

    public enum Route {
        DIRECT("direct calls"),
        MCP_TOOL_SCRIPT("calls from mcp tool scripts");

        private final String label;

        Route(String label) {
            this.label = label;
        }
    }

    // Calls and failures of each tool, per route. Guarded by the class: counting takes nothing next to the call it counts.
    private static final Map<Route, Map<String, long[]>> COUNTS = new EnumMap<>(Route.class);

    private ToolCallStatistics() {
    }

    // A call counts as a failure unless ToolRegistrar would log it as 'ok'
    public static synchronized void record(Route route, String toolName, McpSchema.CallToolResult result) {
        long[] counts = COUNTS.computeIfAbsent(route, r -> new TreeMap<>()).computeIfAbsent(toolName, name -> new long[2]);
        counts[0]++;
        if (!"ok".equals(ToolRegistrar.outcomeOf(result))) {
            counts[1]++;
        }
    }

    // Log the statistics of each route at DEBUG, then start counting afresh
    public static synchronized void logAndReset() {
        summaries().forEach(log::debug);
        COUNTS.clear();
    }

    // One line per route
    static synchronized List<String> summaries() {
        List<String> summaries = new ArrayList<>();
        for (Route route : Route.values()) {
            Map<String, long[]> countsByTool = COUNTS.getOrDefault(route, Map.of());
            long calls = countsByTool.values().stream().mapToLong(counts -> counts[0]).sum();
            long failures = countsByTool.values().stream().mapToLong(counts -> counts[1]).sum();

            // Most failures first; the sort is stable, so a tie keeps the name order of the TreeMap
            String failedTools = countsByTool.entrySet().stream()
                    .filter(entry -> entry.getValue()[1] > 0)
                    .sorted(Comparator.comparingLong((Map.Entry<String, long[]> entry) -> entry.getValue()[1]).reversed())
                    .map(entry -> entry.getKey() + " " + entry.getValue()[0] + "/" + entry.getValue()[1])
                    .collect(Collectors.joining(", "));

            summaries.add(String.format(Locale.ROOT,
                    "Tool call statistics of %s: %d calls, %d succeeded, %d failed, success rate %s, failed tools (calls/failures): %s",
                    route.label, calls, calls - failures, failures,
                    calls == 0 ? "n/a" : String.format(Locale.ROOT, "%.1f%%", 100.0 * (calls - failures) / calls),
                    failedTools.isEmpty() ? "none" : failedTools));
        }
        return summaries;
    }
}
