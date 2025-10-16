package com.astahpromcp.tool.log;

import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Factory for creating log tools
@Slf4j
public class LogToolFactory {

    public LogToolFactory() {
        // No dependencies
    }

    public List<ToolProvider> createToolProviders(ToolCategoryFlags categoryFlags) {
        try {
            return List.of(
                    new AstahProLogFileTool(),
                    new McpServerLogFileTool()
            );
            
        } catch (Exception e) {
            log.warn("Error creating log tools", e);
            return List.of();
        }
    }
}
