package com.astahpromcp.tool.visualization;

import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Factory for creating visualization tools
@Slf4j
public class VisualizationToolFactory {

    public VisualizationToolFactory() {
        // No dependencies
    }

    public List<ToolProvider> createToolProviders(ToolCategoryFlags categoryFlags) {
        try {
            AstahAPI api = AstahAPI.getAstahAPI();
            ProjectAccessor projectAccessor = api.getProjectAccessor();

            return List.of(
                    new PlantumlTool(projectAccessor)
            );
            
        } catch (Exception e) {
            log.warn("Error creating visualization tools", e);
            return List.of();
        }
    }
}
