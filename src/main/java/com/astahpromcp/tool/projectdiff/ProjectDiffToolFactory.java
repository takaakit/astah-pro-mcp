package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Factory for creating the project comparison tools that never touch the Astah API
@Slf4j
public class ProjectDiffToolFactory {

    private final ProjectDiffJobs projectDiffJobs;

    public ProjectDiffToolFactory(ProjectDiffJobs projectDiffJobs) {
        this.projectDiffJobs = projectDiffJobs;
    }

    public List<ToolProvider> createToolProviders() {
        try {
            return List.of(
                    new ProjectDiffResultTool(projectDiffJobs),
                    new ProjectDiffGuideTool()
            );

        } catch (Exception e) {
            log.warn("Error creating project diff tools", e);
            return List.of();
        }
    }
}
