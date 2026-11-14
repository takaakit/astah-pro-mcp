package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.astah.pro.project.inputdto.DiffProjectsDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.DiffStartDTO;
import com.astahpromcp.tool.projectdiff.ProjectDiffHost;
import com.astahpromcp.tool.projectdiff.ProjectDiffJob;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

// Tool for starting a comparison of two versions of a project, whose result is fetched with get_proj_diff_result
@Slf4j
public class ProjectDiffStartTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ProjectDiffJobs projectDiffJobs;
    private final ProjectDiffHost projectDiffHost;

    public ProjectDiffStartTool(ProjectAccessor projectAccessor, ProjectDiffJobs projectDiffJobs, ProjectDiffHost projectDiffHost) {
        this.projectAccessor = projectAccessor;
        this.projectDiffJobs = projectDiffJobs;
        this.projectDiffHost = projectDiffHost;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "diff_proj",
                "Start comparing two versions of an Astah project in the background, and return a 'diffId' at once. MCP client (you) MUST call the project diff guide tool before calling this tool function, to understand which projects to pass, how to start the comparison, and how to get and read its result.",
                this::diffProject,
                DiffProjectsDTO.class,
                DiffStartDTO.class)
        );
    }

    private DiffStartDTO diffProject(DiffProjectsDTO param) throws Exception {

        if (!param.isProjectDiffGuideCalled()) {
            throw new IllegalArgumentException("The comparison was not started, because 'isProjectDiffGuideCalled' is false. Call the project diff guide tool first, and then call this tool function again with 'isProjectDiffGuideCalled' set to true.");
        }

        String requestId = param.requestId() == null ? "" : param.requestId().strip();
        Path basePath = projectPathOf(param.baseProjectPath());
        boolean openTarget = param.targetProjectPath() == null || param.targetProjectPath().isBlank();
        Path targetPath = openTarget ? null : projectPathOf(param.targetProjectPath());

        // A repeated request gets the comparison it started, whatever has become of its files or of the open project since
        ProjectDiffJob earlier = projectDiffJobs.earlier(requestId, basePath, targetPath);
        if (earlier != null) {
            return startOf(earlier);
        }

        requireProjectFile(param.baseProjectPath());
        if (openTarget) {
            if (!projectAccessor.hasProject()) {
                throw new IllegalArgumentException("No project is open in Astah to compare with. Set 'targetProjectPath' to compare two project files.");
            }
        } else {
            requireProjectFile(param.targetProjectPath());
        }

        // A request with the same requestId may have got in since it was looked for, in which case its comparison is returned and nothing is prepared.
        // The comparison is prepared on this thread, which holds the Astah API lock; what then runs in the background never touches the Astah API.
        ProjectDiffJobs.Admission admission = projectDiffJobs.startComparison(requestId, basePath, targetPath, projectDiffHost);
        ProjectDiffJob job = admission.job();
        if (admission.created()) {
            log.info("Started the comparison {}: {} -> {}", job.diffId(), basePath, targetPath == null ? "the open project" : targetPath);
        }
        return startOf(job);
    }

    private static DiffStartDTO startOf(ProjectDiffJob job) {
        return new DiffStartDTO(job.diffId(), job.status(), job.base(), job.target());
    }

    // The path of a project file that a request names, as the requests are told apart by. Whether the file is there is checked only for a new comparison (requireProjectFile).
    private static Path projectPathOf(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("The specified file path is empty.");
        }
        ProjectAccessorTool.requireAbsolutePath(filePath);
        return Path.of(filePath).normalize();
    }

    private static void requireProjectFile(String filePath) {
        if (!new File(filePath).isFile()) {
            throw new IllegalArgumentException("The specified project file does not exist: " + filePath);
        }
    }
}
