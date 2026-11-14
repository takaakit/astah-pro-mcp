package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.projectdiff.export.OpenProjectSnapshot;
import com.astahpromcp.tool.projectdiff.export.SubprocessXmiExporter;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;

import java.nio.file.Files;
import java.nio.file.Path;

// Prepares one comparison on the caller's thread, which holds the Astah API lock: copies the project files, captures the open project through the host when it is the target, and returns the rest for the background.
final class ProjectDiffPreparation implements ProjectDiffJobs.Preparation {

    private final ProjectDiffJobs projectDiffJobs;
    private final ProjectDiffHost host;
    private final Path basePath;
    // null when the target is the project open in Astah
    private final Path targetPath;

    ProjectDiffPreparation(ProjectDiffJobs projectDiffJobs, ProjectDiffHost host, Path basePath, Path targetPath) {
        this.projectDiffJobs = projectDiffJobs;
        this.host = host;
        this.basePath = basePath;
        this.targetPath = targetPath;
    }

    @Override
    public Runnable prepare(ProjectDiffJob job) throws Exception {
        SubprocessXmiExporter.Plan plan = SubprocessXmiExporter.plan(System.getProperty("java.home"), host.astahInstallPath(), host.xmiExportMainClass());
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
            snapshot = host.captureOpenProject(targetDirectory.resolve(SubprocessXmiExporter.XMI_FILE_NAME));
            targetSide = ProjectDiffRunner.openProjectSide(snapshot);
            job.recordSides(baseSide, targetSide);

            projectDiffJobs.retryPendingDeletions();  // Astah has sometimes let go of the files an earlier failed export held open once an export succeeds
        }

        return new ProjectDiffRunner(projectDiffJobs, job, plan,
                new ProjectDiffRunner.Side(baseSide, baseDirectory, null),
                new ProjectDiffRunner.Side(targetSide, targetDirectory, snapshot));
    }
}
