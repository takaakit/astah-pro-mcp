package com.astahpromcp.tool.manifest;

import com.astahpromcp.tool.FilteredToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

@Slf4j
public final class ToolCatalog {
    
    private final List<ToolProvider> astahProviders;
    private final List<ToolProvider> otherProviders;
    private final Map<String, ToolDefinition> definitionsByName;
    private final Set<String> astahLockedNames;
    
    private ToolCatalog(List<ToolProvider> astahProviders, List<ToolProvider> otherProviders) {
        this.astahProviders = List.copyOf(astahProviders);
        this.otherProviders = List.copyOf(otherProviders);
        
        Map<String, ToolDefinition> definitions = new LinkedHashMap<>();
        Set<String> locked = new LinkedHashSet<>();
        
        index(this.astahProviders, definitions, locked);
        index(this.otherProviders, definitions, null);
        
        this.definitionsByName = Map.copyOf(definitions);
        this.astahLockedNames = Set.copyOf(locked);
    }
    
    private static void index(List<ToolProvider> providers,
            Map<String, ToolDefinition> definitions,
            Set<String> lockedNames) {
        
        for (ToolProvider provider : providers) {
            for (ToolDefinition definition : provider.createToolDefinitions()) {
                String name = definition.toolSchema().name();
                
                ToolDefinition previous = definitions.put(name, definition);
                if (previous != null) {
                    throw new IllegalStateException("Two providers register the same tool name: " + name);
                }
                if (lockedNames != null) {
                    lockedNames.add(name);
                }
            }
        }
    }
    
    // Build a catalog from the given providers: astahProviders are the ones that reach the Astah API, otherProviders the ones that never do.
    public static ToolCatalog of(List<ToolProvider> astahProviders, List<ToolProvider> otherProviders) {
        return new ToolCatalog(astahProviders, otherProviders);
    }
    
    public Collection<ToolDefinition> all() {
        return definitionsByName.values();
    }
    
    public Set<String> names() {
        return definitionsByName.keySet();
    }
    
    public ToolDefinition find(String name) {
        return definitionsByName.get(name);
    }
    
    public boolean contains(String name) {
        return definitionsByName.containsKey(name);
    }
    
    // The tools that reach the Astah API, and therefore need the process-wide lock
    public Set<String> astahLockedNames() {
        return astahLockedNames;
    }
    
    // Narrow the catalog to the named tools.
    public Selection select(Set<String> names) {
        return new Selection(filter(astahProviders, names), filter(otherProviders, names));
    }
    
    private static List<ToolProvider> filter(List<ToolProvider> providers, Set<String> names) {
        List<ToolProvider> selected = new ArrayList<>();
        for (ToolProvider provider : providers) {
            // Narrowed from the very same provider instance the catalog was built from, so that a provider
            // which caches what it loads does not end up with one cache per profile.
            FilteredToolProvider filtered = new FilteredToolProvider(provider, names);
            if (filtered.hasAnyExposedTool()) {
                selected.add(filtered);
            }
        }
        return selected;
    }
    
    // A narrowed catalog, still split by whether the tools reach the Astah API
    public record Selection(List<ToolProvider> astahProviders, List<ToolProvider> otherProviders) {
        
        // The providers as they are, without the Astah API lock
        public List<ToolProvider> unlocked() {
            List<ToolProvider> providers = new ArrayList<>(astahProviders);
            providers.addAll(otherProviders);
            return List.copyOf(providers);
        }
        
        // The providers as a profile registers them, with astahApiLock putting the ones that reach the Astah API behind the lock
        public List<ToolProvider> withAstahLock(UnaryOperator<ToolProvider> astahApiLock) {
            List<ToolProvider> providers = new ArrayList<>();
            for (ToolProvider provider : astahProviders) {
                providers.add(astahApiLock.apply(provider));
            }
            providers.addAll(otherProviders);
            return List.copyOf(providers);
        }
    }
}
