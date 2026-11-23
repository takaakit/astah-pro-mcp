package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

@Slf4j
public class ArchitectureScenarioToolFactory {
    
    private final Path workspaceDir;
    
    public ArchitectureScenarioToolFactory(Path workspaceDir) {
        this.workspaceDir = workspaceDir;
    }
    
    public List<ToolProvider> createToolProviders() {
        try {
            return List.of(
                    new ArchitectureScenarioTool(workspaceDir)
            );
            
        } catch (Exception e) {
            log.warn("Error creating architecture scenario analysis tools", e);
            return List.of();
        }
    }
}
