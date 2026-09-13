package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.manifest.ToolCatalog;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Every tool function of this MCP server, looked up by name, so that an mcp tool script can call one.
//
// The definitions held here have NOT been through ExclusiveToolProvider. That is deliberate and load-bearing:
// an mcp tool script runs on its own thread while the request thread holds the Astah API lock for the whole run, and a
// ReentrantLock is only reentrant for the thread that owns it, so taking the lock again from the script thread
// would block forever. See McpToolScriptDispatcher for the drain that the unwrapped handlers still need.
@Slf4j
public final class AstahToolRegistry {

    private final Map<String, ToolDefinition> definitions;
    private final Set<String> mcpToolScriptCallableNames;
    private final Map<String, NotMcpToolScriptCallableReason> notMcpToolScriptCallableReasons;

    private AstahToolRegistry(Map<String, ToolDefinition> definitions,
                              Set<String> mcpToolScriptCallableNames,
                              Map<String, NotMcpToolScriptCallableReason> notMcpToolScriptCallableReasons) {
        this.definitions = definitions;
        this.mcpToolScriptCallableNames = mcpToolScriptCallableNames;
        this.notMcpToolScriptCallableReasons = notMcpToolScriptCallableReasons;
    }

    // Build the registry from the catalog, holding the tools the given names select.
    public static AstahToolRegistry from(ToolCatalog catalog, Set<String> names, Set<String> publishedDirectly) {
        Map<String, ToolDefinition> definitions = new LinkedHashMap<>();
        Map<String, NotMcpToolScriptCallableReason> reasons = new LinkedHashMap<>();

        for (String name : names) {
            ToolDefinition definition = catalog.find(name);
            if (definition == null) {
                continue;
            }
            definitions.put(name, definition);

            // The derived reason wins when there is one.
            NotMcpToolScriptCallableReason reason = catalog.notMcpToolScriptCallableReason(name);
            if (reason == null && publishedDirectly.contains(name)) {
                reason = NotMcpToolScriptCallableReason.PUBLISHED_DIRECTLY;
            }
            if (reason != null) {
                reasons.put(name, reason);
            }
        }

        return of(definitions.values(), reasons);
    }

    // Build the registry from definitions that do not come from a catalog, with the reason each withheld one cannot be called from an mcp tool script.
    public static AstahToolRegistry of(Collection<ToolDefinition> toolDefinitions,
                                       Map<String, NotMcpToolScriptCallableReason> notMcpToolScriptCallableReasons) {
        Map<String, ToolDefinition> definitions = new LinkedHashMap<>();
        for (ToolDefinition definition : toolDefinitions) {
            definitions.put(definition.toolSchema().name(), definition);
        }

        Set<String> mcpToolScriptCallable = new LinkedHashSet<>();
        for (String name : definitions.keySet()) {
            if (!notMcpToolScriptCallableReasons.containsKey(name)) {
                mcpToolScriptCallable.add(name);
            }
        }

        return new AstahToolRegistry(
                Collections.unmodifiableMap(definitions),
                Collections.unmodifiableSet(mcpToolScriptCallable),
                Map.copyOf(notMcpToolScriptCallableReasons));
    }

    // The definition registered under the given name, or null when there is none
    public ToolDefinition find(String toolName) {
        return definitions.get(toolName);
    }

    public Collection<ToolDefinition> all() {
        return definitions.values();
    }

    public Set<String> names() {
        return definitions.keySet();
    }

    public Set<String> mcpToolScriptCallableNames() {
        return mcpToolScriptCallableNames;
    }

    public Collection<ToolDefinition> mcpToolScriptCallableDefinitions() {
        List<ToolDefinition> callable = new ArrayList<>();
        for (String name : mcpToolScriptCallableNames) {
            callable.add(definitions.get(name));
        }
        return Collections.unmodifiableList(callable);
    }

    // Why the given tool function cannot be called from an mcp tool script, or null when it can.
    public NotMcpToolScriptCallableReason notMcpToolScriptCallableReason(String toolName) {
        return notMcpToolScriptCallableReasons.get(toolName);
    }

    public int size() {
        return definitions.size();
    }
}
