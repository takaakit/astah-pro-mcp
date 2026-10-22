package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.astah.pro.project.inputdto.DiffProjectsDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.DiffStartDTO;
import com.astahpromcp.tool.projectdiff.ProjectDiffJob;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import com.astahpromcp.tool.projectdiff.ProjectDiffRunner;
import com.astahpromcp.tool.projectdiff.ProjectDiffStore;
import com.astahpromcp.tool.projectdiff.export.OpenProjectSnapshot;
import com.astahpromcp.tool.projectdiff.export.SubprocessXmiExporter;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Tool for starting a comparison of two versions of a project, whose result is fetched with get_proj_diff_result
@Slf4j
public class ProjectDiffStartTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ProjectDiffJobs projectDiffJobs;
    private final CurrentProjectXmiExporter currentProjectXmiExporter;

    public ProjectDiffStartTool(ProjectAccessor projectAccessor, ProjectDiffJobs projectDiffJobs) {
        this.projectAccessor = projectAccessor;
        this.projectDiffJobs = projectDiffJobs;
        this.currentProjectXmiExporter = new CurrentProjectXmiExporter(projectAccessor);
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
        String inputKey = basePath + "\n" + (openTarget ? "(the open project)" : targetPath.toString());

        // A repeated request gets the comparison it started, whatever has become of its files or of the open project since
        ProjectDiffJob earlier = projectDiffJobs.earlier(requestId, inputKey);
        if (earlier != null) {
            return new DiffStartDTO(earlier.diffId(), earlier.status(), earlier.base(), earlier.target());
        }

        requireProjectFile(param.baseProjectPath());
        if (openTarget) {
            if (!projectAccessor.hasProject()) {
                throw new IllegalArgumentException("No project is open in Astah to compare with. Set 'targetProjectPath' to compare two project files.");
            }
        } else {
            requireProjectFile(param.targetProjectPath());
        }

        ProjectDiffJobs.Admission admission = projectDiffJobs.admit(requestId, inputKey);
        ProjectDiffJob job = admission.job();
        if (!admission.created()) {
            // A request with the same requestId got in since it was looked for
            return new DiffStartDTO(job.diffId(), job.status(), job.base(), job.target());
        }

        try {
            return prepare(job, basePath, targetPath);
        } catch (Throwable t) {
            projectDiffJobs.discard(job);
            throw t;
        }
    }

    // Copy the project files, capture the open project if it is the target, and hand the rest to the background.
    // This runs while the Astah API lock is held; the rest, which never touches the Astah API, does not (ProjectDiffRunner).
    private DiffStartDTO prepare(ProjectDiffJob job, Path basePath, Path targetPath) throws Exception {
        SubprocessXmiExporter.Plan plan = SubprocessXmiExporter.plan(System.getProperty("java.home"), projectAccessor.getAstahInstallPath(), XmiExportMain.class);
        SubprocessXmiExporter.checkCommandLine(plan);

        Path baseDirectory = ProjectDiffStore.baseOf(job.directory());
        Path targetDirectory = ProjectDiffStore.targetOf(job.directory());
        ProjectSideDTO baseSide = ProjectDiffRunner.fileSide(basePath);
        ProjectSideDTO targetSide = targetPath == null ? null : ProjectDiffRunner.fileSide(targetPath);
        job.recordSides(baseSide, targetSide);

        ProjectDiffStore.copy(basePath, baseDirectory.resolve(SubprocessXmiExporter.PROJECT_FILE_NAME), job::stopRequested);

        OpenProjectSnapshot snapshot = null;
        if (targetPath != null) {
            ProjectDiffStore.copy(targetPath, targetDirectory.resolve(SubprocessXmiExporter.PROJECT_FILE_NAME), job::stopRequested);
        } else {
            Files.createDirectories(targetDirectory);
            snapshot = currentProjectXmiExporter.capture(targetDirectory.resolve(SubprocessXmiExporter.XMI_FILE_NAME));
            targetSide = ProjectDiffRunner.openProjectSide(snapshot);
            job.recordSides(baseSide, targetSide);
            
            projectDiffJobs.retryPendingDeletions();  // Astah has sometimes let go of the files an earlier failed export held open once an export succeeds
        }

        projectDiffJobs.submit(job, new ProjectDiffRunner(projectDiffJobs, job, plan,
                new ProjectDiffRunner.Side(baseSide, baseDirectory, null),
                new ProjectDiffRunner.Side(targetSide, targetDirectory, snapshot)));
        log.info("Started the comparison {}: {} -> {}", job.diffId(), basePath, targetPath == null ? "the open project" : targetPath);

        return new DiffStartDTO(job.diffId(), job.status(), baseSide, targetSide);
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
