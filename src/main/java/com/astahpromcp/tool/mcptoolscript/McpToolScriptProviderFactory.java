package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.ExclusiveToolProvider;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

// The tools that run mcp tool scripts and describe the tool functions those scripts can call.
@Slf4j
public class McpToolScriptProviderFactory {

    // Build the tools from the catalog an mcp tool script calls into
    public McpToolScriptTools createToolProviders(ToolCatalog catalog, ToolManifest manifest) {
        // The registry may hold these tools too, so they are built first and read the registry once it is built.
        AtomicReference<AstahToolRegistry> registryHolder = new AtomicReference<>();
        Supplier<AstahToolRegistry> registry = () -> {
            AstahToolRegistry built = registryHolder.get();
            if (built == null) {
                throw new IllegalStateException("The MCP tool script registry was read before it was built.");
            }
            return built;
        };

        List<ToolProvider> unlockedProviders = List.of(
                // Reads a classpath resource only, so it takes no lock either.
                new McpToolScriptExampleTool(),

                // Reads the registry only, so it must not take the Astah API lock: doing so would make a plain lookup queue behind whatever edit another agent is running.
                new CallableToolInfoTool(registry));
        List<ToolProvider> lockedProviders = List.of(
                // Takes the lock for the whole run, which is what serializes the tool calls the mcp tool script makes and what lets the dispatcher call unwrapped handlers without locking again.
                new McpToolScriptTool(new McpToolScriptExecutor(registry)));

        // The tools the registry can hold: the catalog's and these, none of them wrapped in the lock
        List<ToolDefinition> candidates = new ArrayList<>(catalog.all());
        for (ToolProvider provider : unlockedProviders) {
            candidates.addAll(provider.createToolDefinitions());
        }
        for (ToolProvider provider : lockedProviders) {
            candidates.addAll(provider.createToolDefinitions());
        }

        // The two columns are the whole rule: the direct column is what the registry can reach, and the reason column covers exactly the tools the programmatic profile hands the agent directly, which is what an mcp tool script may not call, saying why for each.
        AstahToolRegistry built = AstahToolRegistry.from(
                candidates,
                manifest.namesFor(ToolManifest.Profile.DIRECT),
                manifest.notMcpToolScriptCallableReasons());
        registryHolder.set(built);
        log.info("MCP tool script registry holds {} tool functions, {} of them callable from an mcp tool script and the rest published directly",
                built.size(), built.mcpToolScriptCallableNames().size());

        return new McpToolScriptTools(unlockedProviders, lockedProviders, built);
    }

    // The tools as built, before any profile selects among them, split by whether they hold the Astah API lock while they run, and the registry the mcp tool scripts they run call into
    public record McpToolScriptTools(List<ToolProvider> unlockedProviders, List<ToolProvider> lockedProviders, AstahToolRegistry registry) {

        public McpToolScriptTools {
            unlockedProviders = List.copyOf(unlockedProviders);
            lockedProviders = List.copyOf(lockedProviders);
        }

        // The names of every tool built here
        public Set<String> names() {
            Set<String> names = new LinkedHashSet<>();
            for (ToolProvider provider : allProviders()) {
                for (ToolDefinition definition : provider.createToolDefinitions()) {
                    names.add(definition.toolSchema().name());
                }
            }
            return names;
        }

        // The tools the given names select, as a profile publishes them.
        public List<ToolProvider> select(Set<String> names) {
            List<ToolProvider> selected = new ArrayList<>();
            for (ToolProvider provider : unlockedProviders) {
                FilteredToolProvider filtered = new FilteredToolProvider(provider, names);
                if (filtered.hasAnyExposedTool()) {
                    selected.add(filtered);
                }
            }
            for (ToolProvider provider : lockedProviders) {
                FilteredToolProvider filtered = new FilteredToolProvider(provider, names);
                if (filtered.hasAnyExposedTool()) {
                    selected.add(new ExclusiveToolProvider(filtered));
                }
            }
            return List.copyOf(selected);
        }

        private List<ToolProvider> allProviders() {
            List<ToolProvider> providers = new ArrayList<>(unlockedProviders);
            providers.addAll(lockedProviders);
            return providers;
        }
    }
}
