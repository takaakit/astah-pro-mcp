package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.List;

// Factory for creating knowledge tools
@Slf4j
public class KnowledgeToolFactory {

    private final Path workspaceDir;
    private final HttpClient httpClient;

    public KnowledgeToolFactory(Path workspaceDir) {
        this(workspaceDir, KnowledgeToolSupport.newHttpClient());
    }

    public KnowledgeToolFactory(Path workspaceDir, HttpClient httpClient) {
        this.workspaceDir = workspaceDir;
        this.httpClient = httpClient;
    }

    public List<ToolProvider> createToolProviders(ToolCategoryFlags categoryFlags) {
        try {
            return List.of(
                    new UMLModelingArchitectureInsightsTool(workspaceDir),
                    new DDDReferenceTool(workspaceDir),
                    new OCLSpecificationTool(workspaceDir),
                    new SystemsEngineeringKnowledgeTool(workspaceDir),
                    new AstahProUserGuideTool(workspaceDir, httpClient),
                    new ConceptualModelConventionTool(workspaceDir, httpClient),
                    new PlantumlGuideTool(workspaceDir, httpClient),
                    new ColorPaletteGuideTool(workspaceDir, httpClient),
                    new ORImpedanceMismatchKnowledgeTool(workspaceDir, httpClient)
            );
        } catch (Exception e) {
            log.warn("Error creating knowledge tools", e);
            return List.of();
        }
    }
}
