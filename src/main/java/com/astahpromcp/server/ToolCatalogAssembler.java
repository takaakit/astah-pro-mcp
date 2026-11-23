package com.astahpromcp.server;

import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.assembly.AstahProToolFactory;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.config.ConfigToolFactory;
import com.astahpromcp.tool.info.InfoToolFactory;
import com.astahpromcp.tool.knowledge.KnowledgeToolFactory;
import com.astahpromcp.tool.log.LogToolFactory;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import com.astahpromcp.tool.projectdiff.ProjectDiffToolFactory;
import com.astahpromcp.tool.scenario.ArchitectureScenarioToolFactory;
import com.astahpromcp.tool.visualization.VisualizationToolFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Assembles the tool catalogs from every tool module
public final class ToolCatalogAssembler {
    
    private ToolCatalogAssembler() {
    }
    
    // Build one catalog
    public static ToolCatalog build(DiagramThumbnails thumbnails, Path imageOutputDir, Path workspaceDir, Path documentCacheDir, ProjectDiffJobs projectDiffJobs) {
        return ToolCatalog.of(astahProviders(thumbnails, imageOutputDir, projectDiffJobs), sharedProviders(workspaceDir, documentCacheDir, projectDiffJobs));
    }
    
    // Build both thumbnail variants, sharing the providers the setting does not affect.
    // Every catalog gets the same comparisons, so that a comparison started from either one has its result found by the tool that fetches it.
    public static Map<DiagramThumbnails, ToolCatalog> buildAll(Path imageOutputDir, Path workspaceDir, Path documentCacheDir, ProjectDiffJobs projectDiffJobs) {
        List<ToolProvider> shared = sharedProviders(workspaceDir, documentCacheDir, projectDiffJobs);
        
        Map<DiagramThumbnails, ToolCatalog> catalogs = new EnumMap<>(DiagramThumbnails.class);
        for (DiagramThumbnails thumbnails : DiagramThumbnails.values()) {
            catalogs.put(thumbnails, ToolCatalog.of(astahProviders(thumbnails, imageOutputDir, projectDiffJobs), shared));
        }
        return catalogs;
    }
    
    // The providers every profile puts behind the Astah API lock
    private static List<ToolProvider> astahProviders(DiagramThumbnails thumbnails, Path imageOutputDir, ProjectDiffJobs projectDiffJobs) {
        return new AstahProToolFactory(imageOutputDir, projectDiffJobs)
                .createRawToolProviders(thumbnails);
    }
    
    private static List<ToolProvider> sharedProviders(Path workspaceDir, Path documentCacheDir, ProjectDiffJobs projectDiffJobs) {
        List<ToolProvider> providers = new ArrayList<>();
        providers.addAll(new LogToolFactory().createToolProviders());
        providers.addAll(new KnowledgeToolFactory(workspaceDir, documentCacheDir).createToolProviders());
        providers.addAll(new VisualizationToolFactory().createToolProviders());
        providers.addAll(new ConfigToolFactory().createToolProviders());
        providers.addAll(new InfoToolFactory().createToolProviders());
        providers.addAll(new ProjectDiffToolFactory(projectDiffJobs).createToolProviders());
        providers.addAll(new ArchitectureScenarioToolFactory(workspaceDir).createToolProviders());
        return providers;
    }
}
