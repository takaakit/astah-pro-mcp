package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.projectdiff.inputdto.DiffIdDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;

import java.util.List;

// Tool for obtaining the result of a comparison that diff_proj started. It never touches the Astah API.
public class ProjectDiffResultTool implements ToolProvider {

    private final ProjectDiffJobs projectDiffJobs;

    public ProjectDiffResultTool(ProjectDiffJobs projectDiffJobs) {
        this.projectDiffJobs = projectDiffJobs;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_proj_diff_result",
                "Return the result of a project comparison, page by page. MCP client (you) MUST call the project diff guide tool before reading the result, to understand how to get and read it, unless you called it before starting the comparison and still have its contents.",
                this::getProjectDiffResult,
                DiffIdDTO.class,
                DiffResultDTO.class)
        );
    }

    private DiffResultDTO getProjectDiffResult(DiffIdDTO param) throws Exception {
        String diffId = param.diffId() == null ? "" : param.diffId().strip();
        if (diffId.isEmpty()) {
            throw new IllegalArgumentException("The diffId is empty.");
        }
        return projectDiffJobs.answer(diffId, param.startIndex(), param.startPropertyIndex());
    }
}
