package com.astahpromcp.tool.manifest;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.AstahProToolFactory;
import com.astahpromcp.tool.astah.pro.ExclusiveToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.config.ConfigToolFactory;
import com.astahpromcp.tool.info.InfoToolFactory;
import com.astahpromcp.tool.knowledge.KnowledgeToolFactory;
import com.astahpromcp.tool.log.LogToolFactory;
import com.astahpromcp.tool.mcptoolscript.FilteredToolProvider;
import com.astahpromcp.tool.visualization.VisualizationToolFactory;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // Build a catalog from the given providers, for tests.
    static ToolCatalog of(List<ToolProvider> astahProviders, List<ToolProvider> otherProviders) {
        return new ToolCatalog(astahProviders, otherProviders);
    }

    // Build one catalog
    public static ToolCatalog build(DiagramThumbnails thumbnails, Path imageOutputDir, Path workspaceDir) {
        return new ToolCatalog(astahProviders(thumbnails, imageOutputDir), sharedProviders(workspaceDir));
    }

    // Build both thumbnail variants, sharing the providers the setting does not affect.
    public static Map<DiagramThumbnails, ToolCatalog> buildAll(Path imageOutputDir, Path workspaceDir) {
        List<ToolProvider> shared = sharedProviders(workspaceDir);

        Map<DiagramThumbnails, ToolCatalog> catalogs = new EnumMap<>(DiagramThumbnails.class);
        for (DiagramThumbnails thumbnails : DiagramThumbnails.values()) {
            catalogs.put(thumbnails, new ToolCatalog(astahProviders(thumbnails, imageOutputDir), shared));
        }
        return catalogs;
    }

    // The providers every profile puts behind the Astah API lock
    private static List<ToolProvider> astahProviders(DiagramThumbnails thumbnails, Path imageOutputDir) {
        return new AstahProToolFactory(imageOutputDir)
                .createRawToolProviders(thumbnails);
    }

    // The five factories that never touch the Astah API, so they are not wrapped for any profile
    private static List<ToolProvider> sharedProviders(Path workspaceDir) {
        List<ToolProvider> providers = new ArrayList<>();
        providers.addAll(new LogToolFactory().createToolProviders());
        providers.addAll(new KnowledgeToolFactory(workspaceDir).createToolProviders());
        providers.addAll(new VisualizationToolFactory().createToolProviders());
        providers.addAll(new ConfigToolFactory().createToolProviders());
        providers.addAll(new InfoToolFactory().createToolProviders());
        return providers;
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

        // The providers as a profile registers them: the ones that reach the Astah API hold the lock while they run
        public List<ToolProvider> withAstahLock() {
            List<ToolProvider> providers = new ArrayList<>();
            for (ToolProvider provider : astahProviders) {
                providers.add(new ExclusiveToolProvider(provider));
            }
            providers.addAll(otherProviders);
            return List.copyOf(providers);
        }
    }
}
