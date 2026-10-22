package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.astah.pro.EdtSupport;
import com.astahpromcp.tool.projectdiff.export.ExportInformation;
import com.astahpromcp.tool.projectdiff.export.OpenProjectSnapshot;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

// Writes the XMI of the project open in Astah, unsaved changes included
@Slf4j
final class CurrentProjectXmiExporter {

    // How long to wait for the thread that watches transactions to end
    private static final long WATCHER_STOP_MILLIS = 1_000;

    private final ProjectAccessor projectAccessor;

    CurrentProjectXmiExporter(ProjectAccessor projectAccessor) {
        this.projectAccessor = projectAccessor;
    }

    // exportXMI() is no snapshot: an edit made while it writes can leave half of a transaction in the XMI. So the XMI is
    // thrown away when an edit is seen from just before the project is recorded until just after the XMI is written:
    // a committed edit fires projectChanged, and the wait for the EDT at the end lets a GUI edit finish firing it; an edit
    // still open, or one rolled back meanwhile, which fires nothing, shows in isInTransaction(), which a watcher thread
    // reads throughout. The tools of this server cannot edit meanwhile: diff_proj holds the Astah API lock.
    OpenProjectSnapshot capture(Path xmi) {
        if (!projectAccessor.hasProject()) {
            throw new IllegalStateException("No project is open in Astah.");
        }

        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        AtomicInteger events = new AtomicInteger();
        ProjectEventListener listener = new ProjectEventListener() {
            @Override
            public void projectOpened(ProjectEvent e) {
                events.incrementAndGet();
            }

            @Override
            public void projectClosed(ProjectEvent e) {
                events.incrementAndGet();
            }

            @Override
            public void projectChanged(ProjectEvent e) {
                // Counted even when it reports no edit unit: a project that was just opened, for one, reports several such events
                events.incrementAndGet();
            }
        };

        Path part = xmi.toAbsolutePath().resolveSibling(xmi.getFileName() + ".part");
        TransactionWatcher watcher = new TransactionWatcher(transactionManager::isInTransaction);
        projectAccessor.addProjectEventListener(listener);
        watcher.start();
        try {
            if (transactionManager.isInTransaction()) {
                throw conflict("the project was being edited");
            }

            Instant startedAt = Instant.now();
            IModel project = projectAccessor.getProject();
            String rootId = project.getId();
            String path = pathOf(projectAccessor);
            ExportInformation information = JsonSupport.OBJ_MAPPER.readValue(XmiExportMain.information(projectAccessor), ExportInformation.class);

            Exception exportFailure = null;
            try {
                projectAccessor.exportXMI(part.toString());
            } catch (Exception e) {
                exportFailure = e;
            }

            // Read everything that may have got in the way even when the export failed, so that a conflict is told apart from another failure, and the log says what the decision rested on
            EdtSupport.DrainOutcome drained = EdtSupport.drainAndReport(McpServerConfig.PROJECT_DIFF_CAPTURE_EDT_WAIT_SECONDS);
            boolean seen = watcher.stop();
            boolean openAtEnd = transactionManager.isInTransaction();
            boolean switched = !projectAccessor.hasProject() || !rootId.equals(projectAccessor.getProject().getId()) || !path.equals(pathOf(projectAccessor));
            Instant completedAt = Instant.now();
            log.debug("Captured the open project: events={}, transactionSeen={}, transactionOpenAtEnd={}, projectSwitched={}, edt={}, exportFailure={}",
                    events.get(), seen, openAtEnd, switched, drained, exportFailure == null ? "none" : exportFailure.toString());

            IllegalStateException failure = failureOf(conflictReason(events.get(), seen, openAtEnd, switched, drained), exportFailure);
            if (failure != null) {
                throw failure;
            }

            Files.move(part, xmi, StandardCopyOption.REPLACE_EXISTING);
            return new OpenProjectSnapshot(rootId, project.getName(), path, startedAt, completedAt, information);

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read the open project: " + e, e);

        } finally {
            watcher.stop();
            projectAccessor.removeProjectEventListener(listener);
            try {
                Files.deleteIfExists(part);
            } catch (IOException e) {
                // exportXMI() can leave the file open after it fails; the folder of the comparison is then deleted later (ProjectDiffJobs)
                log.debug("Failed to delete {}: {}", part, e.toString());
            }
        }
    }

    // What the capture failed with, or null when it did not fail: a conflict when something got in the way (conflictReason) or when Astah refused to write because a transaction was open, and otherwise the failure of the export.
    static IllegalStateException failureOf(String conflictReason, Exception exportFailure) {
        String reason = conflictReason;
        if (reason == null && exportFailure != null && exportFailure.getClass().getSimpleName().contains("BadTransaction")) {
            reason = "the project was being edited";
        }
        if (reason != null) {
            return new IllegalStateException("The open project could not be captured because " + reason + " while Astah wrote it. Start the comparison again in a moment.", exportFailure);
        }
        if (exportFailure != null) {
            return new IllegalStateException("Failed to write the XMI of the open project: " + exportFailure, exportFailure);
        }
        return null;
    }

    // Why the XMI cannot be trusted to be one state of the project, or null when nothing got in the way.
    // Package-private so that a test can check every reason without making Astah edit at the right moment.
    static String conflictReason(int events, boolean transactionSeen, boolean transactionOpenAtEnd, boolean projectSwitched,
                                 EdtSupport.DrainOutcome drained) {
        if (projectSwitched) {
            return "another project was opened, or the project was saved under another name";
        }
        if (events > 0 || transactionSeen || transactionOpenAtEnd) {
            return "the project was being edited";
        }
        if (drained == EdtSupport.DrainOutcome.TIMED_OUT) {
            return "Astah was busy and did not finish the work it had queued";
        }
        if (drained == EdtSupport.DrainOutcome.INTERRUPTED) {
            return "the capture was interrupted";
        }
        return null;
    }

    private static IllegalStateException conflict(String reason) {
        return failureOf(reason, null);
    }

    private static String pathOf(ProjectAccessor projectAccessor) throws Exception {
        String path = projectAccessor.getProjectPath();
        return path == null || path.equals(ProjectAccessorTool.UNSAVED_PROJECT_PATH) ? "" : path;
    }

    // Reads whether a transaction is open, as often as it can, until stopped, and remembers whether it ever was.
    // Package-private so that a test can check it on a transaction of its own.
    static final class TransactionWatcher {

        private final BooleanSupplier inTransaction;
        private final AtomicBoolean running = new AtomicBoolean();
        private final AtomicBoolean seen = new AtomicBoolean();
        private Thread thread;

        TransactionWatcher(BooleanSupplier inTransaction) {
            this.inTransaction = inTransaction;
        }

        void start() {
            running.set(true);
            thread = new Thread(() -> {
                while (running.get()) {
                    if (inTransaction.getAsBoolean()) {
                        seen.set(true);
                    }
                    Thread.onSpinWait();
                }
            }, "project-diff-transaction-watcher");
            thread.setDaemon(true);
            thread.start();
        }

        boolean seen() {
            return seen.get();
        }

        // Stop watching, and say whether a transaction was ever open meanwhile
        boolean stop() {
            if (running.getAndSet(false) && thread != null) {
                try {
                    thread.join(WATCHER_STOP_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return seen.get();
        }
    }
}
