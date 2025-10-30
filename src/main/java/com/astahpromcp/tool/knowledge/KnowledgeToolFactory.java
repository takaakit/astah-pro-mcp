package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

// Factory for creating knowledge tools
@Slf4j
public class KnowledgeToolFactory {

    private final Path workspaceDir;
    private final HttpClient httpClient;

    public KnowledgeToolFactory(Path workspaceDir) {
        this(workspaceDir, HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public KnowledgeToolFactory(Path workspaceDir, HttpClient httpClient) {
        this.workspaceDir = workspaceDir;
        this.httpClient = httpClient;
    }

    public List<ToolProvider> createToolProviders(ToolCategoryFlags categoryFlags) {
        try {
            AstahAPI astahApi = AstahAPI.getAstahAPI();
            ProjectAccessor projectAccessor = astahApi.getProjectAccessor();

            return List.of(
                    new DDDReferenceTool(workspaceDir, projectAccessor),
                    new AstahManualTool(workspaceDir, projectAccessor),
                    new ConceptualModelConventionTool(workspaceDir, httpClient),
                    new PlantumlGuideTool(workspaceDir, httpClient),
                    new ColorPaletteGuideTool(workspaceDir, httpClient)
            );
        } catch (Exception e) {
            log.warn("Error creating knowledge tools", e);
            return List.of();
        }
    }
}
