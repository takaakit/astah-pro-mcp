package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.projectdiff.engine.ProjectDiffEngine;
import com.astahpromcp.tool.projectdiff.export.ExportInformation;
import com.astahpromcp.tool.projectdiff.export.OpenProjectSnapshot;
import com.astahpromcp.tool.projectdiff.export.SubprocessXmiExporter;
import com.astahpromcp.tool.projectdiff.outputdto.DiffFailureDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStage;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectKind;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

// The background part of one comparison, which never touches the Astah API: write the XMI of each project file in a
// separate process, compare the XMIs, and write the result
@Slf4j
final class ProjectDiffRunner implements Runnable {
    
    // Data that is never compared, or left out under a condition, which every result lists
    static final List<String> NOT_COMPARED = List.of(
            "the name of the root model, which Astah takes from the file name",
            "the data of images (an image is compared by its ID, position and size)",
            "the z-order (depth) of the messages and activations of sequence diagrams",
            "the points of the curved lines of mind maps",
            "the order of elements with an xmi.id, such as the order of attributes",
            "differences of less than 2 pixels in the width and position of the label of a message on a communication diagram, when nothing else about the label differs (showing the diagram makes Astah resize such labels)",
            "differences in the last digits of the coordinates and sizes of presentations, by no more than a billionth of the value (two processes can round the same value differently)",
            "a tagged value 'jude.diagram.updated_version' that only one side has, of the version of the Astah that read that side (loading a file gives it to a diagram saved without one)",
            "the XMI header");
    
    // One side of the comparison: the project as the answer shows it so far, the folder holding project.asta or out.xmi,
    // and, for the open project, what the capture recorded (null for a project file)
    public record Side(ProjectSideDTO project, Path directory, OpenProjectSnapshot snapshot) {
    }
    
    private final ProjectDiffJobs projectDiffJobs;
    private final ProjectDiffJob job;
    private final SubprocessXmiExporter.Plan plan;
    private final Side base;
    private final Side target;
    
    // The processes started and not yet waited for
    private SubprocessXmiExporter.Running baseRunning;
    private SubprocessXmiExporter.Running targetRunning;
    
    // The stage the comparison is in, and whether a failure in it is worth starting the same comparison again for
    private DiffStage stage = DiffStage.FILE_EXPORT;
    private boolean retryable;
    
    public ProjectDiffRunner(ProjectDiffJobs projectDiffJobs, ProjectDiffJob job, SubprocessXmiExporter.Plan plan, Side base, Side target) {
        this.projectDiffJobs = projectDiffJobs;
        this.job = job;
        this.plan = plan;
        this.base = base;
        this.target = target;
    }
    
    // A project file, before its XMI is written
    public static ProjectSideDTO fileSide(Path projectFile) {
        return new ProjectSideDTO(ProjectKind.FILE, projectFile.toString(), projectNameOf(projectFile), "", "", "", "", -1, List.of());
    }
    
    public static ProjectSideDTO openProjectSide(OpenProjectSnapshot snapshot) {
        ExportInformation information = snapshot.information();
        return new ProjectSideDTO(ProjectKind.OPEN_PROJECT, snapshot.path(), snapshot.projectName(), snapshot.rootId(),
                ProjectDiffJobs.format(snapshot.startedAt()), ProjectDiffJobs.format(snapshot.completedAt()),
                information.astahVersion(), information.projectModelVersion(), information.referenceProjects());
    }
    
    @Override
    public void run() {
        try {
            // A disk that is full or busy may not be so on another try
            enter(DiffStage.FILE_EXPORT, true);
            SubprocessXmiExporter.prepare(base.directory(), plan);
            if (target.snapshot() == null) {
                SubprocessXmiExporter.prepare(target.directory(), plan);
            }
            
            // Start both processes before waiting for either, so that the two sides are written at the same time
            enter(DiffStage.FILE_EXPORT, false);
            baseRunning = start(ProjectDiffStore.BASE, base);
            if (target.snapshot() == null) {
                targetRunning = start(ProjectDiffStore.TARGET, target);
            }
            
            // Each process is forgotten once it has been waited for, which also stops it whatever happened
            SubprocessXmiExporter.Running awaited = baseRunning;
            baseRunning = null;
            ExportInformation baseInformation = await(ProjectDiffStore.BASE, base, awaited);
            ExportInformation targetInformation;
            if (targetRunning != null) {
                awaited = targetRunning;
                targetRunning = null;
                targetInformation = await(ProjectDiffStore.TARGET, target, awaited);
            } else {
                targetInformation = target.snapshot().information();
            }
            
            ProjectSideDTO baseSide = sideOf(base, baseInformation);
            ProjectSideDTO targetSide = sideOf(target, targetInformation);
            job.recordSides(baseSide, targetSide);
            
            // For when the two sides differ where they should not: the runtime measures the text that shapes fit
            log.debug("The comparison {} wrote the base on {} and the target on {}", job.diffId(), baseInformation.runtime(), targetInformation.runtime());
            
            enter(DiffStage.RESULT, false);
            checkJobSize();
            
            enter(DiffStage.COMPARE, false);
            StopCheck checkStop = new StopCheck(job::stopRequested, McpServerConfig.PROJECT_DIFF_COMPARE_TIMEOUT_SECONDS, McpServerConfig.PROJECT_DIFF_STOP_CHECK_INTERVAL);
            Path baseXmi = base.directory().resolve(SubprocessXmiExporter.XMI_FILE_NAME);
            Path targetXmi = target.directory().resolve(SubprocessXmiExporter.XMI_FILE_NAME);
            XmiIndex baseIndex = read(baseXmi, checkStop);
            XmiIndex targetIndex = read(targetXmi, checkStop);
            
            List<String> warnings = formatWarnings(baseSide, baseInformation, targetSide, targetInformation);
            
            try (ResultWriter writer = new ResultWriter(ProjectDiffStore.resultOf(job.directory()), job.diffId(), baseSide, targetSide, warnings, NOT_COMPARED)) {
                ProjectDiffEngine.compare(
                        new ProjectDiffEngine.Input(baseXmi, baseIndex, baseSide.projectName(), baseSide.astahVersion()),
                        new ProjectDiffEngine.Input(targetXmi, targetIndex, targetSide.projectName(), targetSide.astahVersion()),
                        McpServerConfig.PROJECT_DIFF_VERIFIED_XMI_VERSIONS, writer, checkStop);
                writer.finish();
                
                enter(DiffStage.RESULT, false);
                checkJobSize();
                cleanUp();
                job.complete(writer.header(), retentionEnd());
                
            } catch (IOException e) {
                // Writing the result failed, or reading an XMI again for it; neither says that the projects cannot be compared
                enter(DiffStage.RESULT, true);
                throw new IllegalStateException("Failed to write the result: " + e.getMessage(), e);
            }
            
        } catch (IllegalStateException e) {
            fail(e.getMessage());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("The comparison was stopped.");
            
        } catch (Throwable t) {
            log.error("Failed to run the comparison {}", job.diffId(), t);
            fail(String.valueOf(t));
            
        } finally {
            abandonProcesses();
            projectDiffJobs.finished(job);
        }
    }
    
    private void enter(DiffStage stage, boolean retryable) {
        this.stage = stage;
        this.retryable = retryable;
    }
    
    // Start the process that writes the XMI of one side's project file, saying which side it was when it cannot be started
    private SubprocessXmiExporter.Running start(String sideName, Side side) {
        try {
            return SubprocessXmiExporter.start(side.directory(), plan, projectDiffJobs);
        } catch (IllegalStateException e) {
            throw new IllegalStateException(failureOf(sideName, side, e.getMessage(), false), e);
        }
    }
    
    // Wait for the process of one side, saying which side it was and where its log is kept when it fails
    private ExportInformation await(String sideName, Side side, SubprocessXmiExporter.Running running) throws InterruptedException {
        try {
            return SubprocessXmiExporter.await(running, projectDiffJobs, job::stopRequested);
        } catch (IllegalStateException e) {
            throw new IllegalStateException(failureOf(sideName, side, e.getMessage(), true), e);
        }
    }
    
    // A failure to write the XMI of one side's project file
    private String failureOf(String sideName, Side side, String message, boolean withLog) {
        String failure = "Writing the XMI of the " + sideName + ", " + side.project().path() + ", failed: " + message;
        if (withLog) {
            failure += " The whole log of its process is kept at " + job.directory().resolve(sideName + ".log") + ".";
        }
        return failure;
    }
    
    // Stop a process the comparison never got to wait for, because the other side failed first. Done before the files
    // are cleaned up, so that no process is still writing into a folder being deleted.
    private void abandonProcesses() {
        if (baseRunning != null) {
            SubprocessXmiExporter.abandon(baseRunning, projectDiffJobs);
            baseRunning = null;
        }
        if (targetRunning != null) {
            SubprocessXmiExporter.abandon(targetRunning, projectDiffJobs);
            targetRunning = null;
        }
    }
    
    private void fail(String message) {
        log.info("The comparison {} failed at {}: {}", job.diffId(), stage, message);
        abandonProcesses();
        cleanUp();
        boolean stopped = job.stopRequested();
        job.fail(new DiffFailureDTO(stage, stopped ? "The comparison was stopped." : message, retryable && !stopped), retentionEnd());
    }
    
    private static Instant retentionEnd() {
        return Instant.now().plus(McpServerConfig.PROJECT_DIFF_MINIMUM_RETENTION_MINUTES, ChronoUnit.MINUTES);
    }
    
    private static XmiIndex read(Path xmi, Runnable checkStop) {
        try {
            return XmiIndex.read(xmi, McpServerConfig.PROJECT_DIFF_MAX_XMI_BYTES, checkStop);
            
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the XMI " + xmi + ": " + e.getMessage(), e);
        }
    }
    
    // Run over and over while the XMIs are read and compared
    static final class StopCheck implements Runnable {
        
        private final BooleanSupplier stopRequested;
        private final long timeoutSeconds;
        private final long deadlineNanos;
        private final int interval;
        private int runs;
        
        StopCheck(BooleanSupplier stopRequested, long timeoutSeconds, int interval) {
            this.stopRequested = stopRequested;
            this.timeoutSeconds = timeoutSeconds;
            this.deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
            this.interval = interval;
        }
        
        @Override
        public void run() {
            if (++runs < interval) {
                return;
            }
            runs = 0;
            
            // An interrupt stops it too, as it stops the wait for the processes that write the XMIs
            if (stopRequested.getAsBoolean() || Thread.currentThread().isInterrupted()) {
                throw new IllegalStateException("The comparison was stopped.");
            }
            if (System.nanoTime() - deadlineNanos >= 0) {
                throw new IllegalStateException("Reading and comparing the XMIs took longer than " + timeoutSeconds + " seconds.");
            }
        }
    }
    
    // A comparison that has grown beyond the disk space set aside for one fails, rather than fill the disk
    private void checkJobSize() {
        long size = ProjectDiffStore.sizeOf(job.directory());
        if (size > McpServerConfig.PROJECT_DIFF_MAX_JOB_BYTES) {
            throw new IllegalStateException(String.format(Locale.ROOT, "The comparison's files take %,d bytes, more than the %,d bytes set aside for one comparison.",
                    size, McpServerConfig.PROJECT_DIFF_MAX_JOB_BYTES));
        }
    }
    
    // The notes on reading the result that the two projects call for. Each says which side it is about, and a note that both sides call for is given once, for both.
    static List<String> formatWarnings(ProjectSideDTO base, ExportInformation baseInformation, ProjectSideDTO target, ExportInformation targetInformation) {
        List<String> warnings = new ArrayList<>();
        
        boolean baseOlder = convertedOnLoading(base, baseInformation);
        boolean targetOlder = convertedOnLoading(target, targetInformation);
        if (baseOlder || targetOlder) {
            String saved;
            if (baseOlder && targetOlder) {
                saved = "Both the base, '" + base.projectName() + "' (model version " + baseInformation.projectModelVersion() + "), and the target, '"
                        + target.projectName() + "' (model version " + targetInformation.projectModelVersion() + "), were saved by an older Astah (this Astah writes "
                        + baseInformation.astahModelVersion() + ")";
            } else {
                ProjectSideDTO side = baseOlder ? base : target;
                ExportInformation information = baseOlder ? baseInformation : targetInformation;
                saved = (baseOlder ? "The base, '" : "The target, '") + side.projectName() + "', was saved by an older Astah (model version "
                        + information.projectModelVersion() + "; this Astah writes " + information.astahModelVersion() + ")";
            }
            warnings.add(saved + ". Astah converts such a file when it loads it, which can move positions and z-orders slightly, so some changes may come from the conversion rather than from an edit."
                    + " Such differences can show even when a file is compared with itself, and they can change from one comparison to the next.");
        }
        
        boolean baseReferences = !baseInformation.referenceProjects().isEmpty();
        boolean targetReferences = !targetInformation.referenceProjects().isEmpty();
        if (baseReferences && targetReferences) {
            warnings.add("Both the base, '" + base.projectName() + "', and the target, '" + target.projectName() + "', reference other projects (see their 'referenceProjects')."
                    + " The elements they take from them are part of their XMIs, so they appear in the result too, including changes that come only from updating the references.");
        } else if (baseReferences || targetReferences) {
            String side = baseReferences ? "the base" : "the target";
            String name = baseReferences ? base.projectName() : target.projectName();
            warnings.add(Character.toUpperCase(side.charAt(0)) + side.substring(1) + ", '" + name + "', references other projects (see 'referenceProjects' of " + side + ")."
                    + " The elements it takes from them are part of its XMI, so they appear in the result too, including changes that come only from updating the references.");
        }
        return warnings;
    }
    
    // Whether Astah converted the project when it loaded it from a file saved by an older Astah
    private static boolean convertedOnLoading(ProjectSideDTO side, ExportInformation information) {
        boolean neverSaved = side.kind() == ProjectKind.OPEN_PROJECT && side.path().isEmpty();
        return !neverSaved && information.isOlderFormat();
    }
    
    private static ProjectSideDTO sideOf(Side side, ExportInformation information) {
        ProjectSideDTO project = side.project();
        return new ProjectSideDTO(project.kind(), project.path(), project.projectName(), project.projectRootId(),
                project.captureStartedAt(), project.captureCompletedAt(),
                information.astahVersion(), information.projectModelVersion(), information.referenceProjects());
    }
    
    // The name a project file gives its project: its file name without the extension
    private static String projectNameOf(Path projectFile) {
        String name = projectFile.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
    
    // Keep the result and the logs of the separate processes; delete the copies of the project files and the XMIs
    private void cleanUp() {
        for (String side : List.of(ProjectDiffStore.BASE, ProjectDiffStore.TARGET)) {
            Path directory = job.directory().resolve(side);
            Path logFile = directory.resolve(SubprocessXmiExporter.LOG_FILE_NAME);
            try {
                if (Files.exists(logFile)) {
                    Files.move(logFile, job.directory().resolve(side + ".log"), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                log.warn("Failed to keep the log of a process of the comparison {}", job.diffId(), e);
            }
            projectDiffJobs.deleteOrRemember(directory, job);
        }
    }
}
