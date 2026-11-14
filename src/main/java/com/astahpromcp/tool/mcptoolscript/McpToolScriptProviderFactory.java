package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.FilteredToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

// The tools that run mcp tool scripts and describe the tool functions those scripts can call.
@Slf4j
public class McpToolScriptProviderFactory {
    
    // Build the tools from the catalog an mcp tool script calls into. The host keeps every run safe while it calls the catalog's tool functions.
    public McpToolScriptTools createToolProviders(ToolCatalog catalog, ToolManifest manifest, McpToolScriptHost host) {
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
                new McpToolScriptTool(new McpToolScriptExecutor(registry, host)));
        
        // The tools the registry can hold: the catalog's and these, none of them wrapped in the lock
        List<ToolDefinition> candidates = new ArrayList<>(catalog.all());
        for (ToolProvider provider : unlockedProviders) {
            candidates.addAll(provider.createToolDefinitions());
        }
        for (ToolProvider provider : lockedProviders) {
            candidates.addAll(provider.createToolDefinitions());
        }
        
        // The programmatic column is the whole rule. The registry holds every tool that column reaches: the 's' ones are what an mcp tool script may call,
        // and the 'x' ones, which the profile hands the agent directly, are held only so that a script that calls one is told why it cannot, in the words of the reason column.
        AstahToolRegistry built = AstahToolRegistry.from(
                candidates,
                manifest.namesReachedOn(ToolManifest.Profile.PROGRAMMATIC),
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
        
        // The tools the given names select, as a profile publishes them. astahApiLock puts a provider behind the Astah API lock.
        public List<ToolProvider> select(Set<String> names, UnaryOperator<ToolProvider> astahApiLock) {
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
                    selected.add(astahApiLock.apply(filtered));
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
