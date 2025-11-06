package com.astahpromcp.tool.config;

import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Factory for creating config tools
@Slf4j
public class ConfigToolFactory {
    
    public ConfigToolFactory() {
        // No dependencies
    }

    public List<ToolProvider> createToolProviders(ToolCategoryFlags categoryFlags) {
        try {
            return List.of(
                    new ConfigTool()
            );
            
        } catch (Exception e) {
            log.warn("Error creating config tools", e);
            return List.of();
        }
    }
}
