package com.astahpromcp.tool.visualization;

import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.common.ImageConvertSupport;
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
            ImageConvertSupport imageConvertSupport = new ImageConvertSupport();

            return List.of(
                    new PlantumlTool(),
                    new GraphvizTool(imageConvertSupport)
            );

        } catch (Exception e) {
            log.warn("Error creating visualization tools", e);
            return List.of();
        }
    }
}
