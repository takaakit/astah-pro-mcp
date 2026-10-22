package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.projectdiff.engine.NormalizationRule;
import com.astahpromcp.tool.projectdiff.export.ProcessLauncher;
import com.astahpromcp.tool.projectdiff.outputdto.DiffFailureDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStatus;
import com.astahpromcp.tool.projectdiff.outputdto.NormalizationDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectKind;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.astahpromcp.tool.projectdiff.outputdto.SummaryDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// The comparisons of the server: which are being prepared, which run, which results are kept, and what they occupy.
@Slf4j
public final class ProjectDiffJobs implements ProcessLauncher {

    private final ProjectDiffStore store;
    private final int maxJobsInProgress;
    private final int maxStoredJobs;
    private final long maxJobBytes;
    private final long maxTotalBytes;
    private final long resultWaitMillis;
    private final long stopWaitMillis;

    private final FolderDeleter deleter;

    private final Object lock = new Object();
    private final Map<String, ProjectDiffJob> jobs = new LinkedHashMap<>();
    private final Map<String, String> diffIdByRequestId = new HashMap<>();
    private final Set<Process> processes = new HashSet<>();
    private final Map<Path, PendingDeletion> pendingDeletions = new LinkedHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();
    private boolean accepting = true;
    private long reservedBytes;
    private int preparing;
    private ExecutorService executor;

    // Deletes a folder and everything in it, and says whether it is gone
    @FunctionalInterface
    interface FolderDeleter {
        boolean delete(Path directory);
    }

    // A folder that could not be deleted, typically because exportXMI() failed and left a file in it open, which Astah may hold until it ends.
    private static final class PendingDeletion {
        final Path directory;
        final long bytes;
        final ProjectDiffJob owner;
        int attempts = 1;

        PendingDeletion(Path directory, long bytes, ProjectDiffJob owner) {
            this.directory = directory;
            this.bytes = bytes;
            this.owner = owner;
        }
    }

    public ProjectDiffJobs(Path root) {
        this(root,
            McpServerConfig.PROJECT_DIFF_MAX_JOBS_IN_PROGRESS,
            McpServerConfig.PROJECT_DIFF_MAX_STORED_JOBS,
            McpServerConfig.PROJECT_DIFF_MAX_JOB_BYTES,
            McpServerConfig.PROJECT_DIFF_MAX_TOTAL_BYTES,
            TimeUnit.SECONDS.toMillis(McpServerConfig.PROJECT_DIFF_RESULT_WAIT_SECONDS),
            TimeUnit.SECONDS.toMillis(McpServerConfig.PROJECT_DIFF_STOP_WAIT_SECONDS));
    }

    // Package-private so that a test can reach the limits with a few comparisons, and does not sit through the waits.
    ProjectDiffJobs(Path root, int maxJobsInProgress, int maxStoredJobs, long maxJobBytes, long maxTotalBytes, long resultWaitMillis, long stopWaitMillis) {
        this(root, maxJobsInProgress, maxStoredJobs, maxJobBytes, maxTotalBytes, resultWaitMillis, stopWaitMillis, ProjectDiffStore::delete);
    }

    ProjectDiffJobs(Path root, int maxJobsInProgress, int maxStoredJobs, long maxJobBytes, long maxTotalBytes, long resultWaitMillis, long stopWaitMillis,
                    FolderDeleter deleter) {
        this.store = new ProjectDiffStore(root);
        this.maxJobsInProgress = maxJobsInProgress;
        this.maxStoredJobs = maxStoredJobs;
        this.maxJobBytes = maxJobBytes;
        this.maxTotalBytes = maxTotalBytes;
        this.resultWaitMillis = resultWaitMillis;
        this.stopWaitMillis = stopWaitMillis;
        this.deleter = deleter;
    }

    // What accepting a request led to: a new comparison to prepare, or the one an earlier request with the same requestId started
    public record Admission(
        ProjectDiffJob job,
        boolean created
    ) {
    }

    // Accept a comparison, or refuse it with a message saying why.
    public Admission admit(String requestId, String inputKey) {
        sweep();
        Admission admission;
        List<ProjectDiffJob> evicted;
        synchronized (lock) {
            if (!accepting) {
                throw new IllegalStateException("The MCP server is stopping, so it accepts no comparison.");
            }

            ProjectDiffJob earlier = findEarlier(requestId, inputKey);
            if (earlier != null) {
                return new Admission(earlier, false);
            }

            long inProgress = jobs.values().stream().filter(ProjectDiffJob::inProgress).count();
            if (inProgress >= maxJobsInProgress) {
                throw new IllegalStateException(inProgress + " comparisons are already in progress, which is as many as this server runs at once. Get their results, then try again.");
            }

            evicted = makeRoom(Instant.now());
            for (ProjectDiffJob job : evicted) {
                forget(job);
            }

            String diffId = "d" + sequence.incrementAndGet() + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x100000, 0x1000000));
            ProjectDiffJob job = new ProjectDiffJob(diffId, requestId, inputKey, store.directoryOf(diffId), maxJobBytes);
            jobs.put(diffId, job);
            if (!requestId.isEmpty()) {
                diffIdByRequestId.put(requestId, diffId);
            }
            reservedBytes += maxJobBytes;
            preparing++;
            admission = new Admission(job, true);
        }

        for (ProjectDiffJob job : evicted) {
            log.debug("Delete the comparison {} to make room; its result was last given out at {}", job.diffId(), job.lastDeliveredAt());
            deleteOrRemember(job.directory(), null);
        }
        return admission;
    }

    // The comparison that an earlier request with the same requestId started, or null when there is none.
    public ProjectDiffJob earlier(String requestId, String inputKey) {
        sweep();
        synchronized (lock) {
            return findEarlier(requestId, inputKey);
        }
    }

    // The comparison of an earlier request with the same requestId, or null; an empty requestId has none. Called with the lock held.
    private ProjectDiffJob findEarlier(String requestId, String inputKey) {
        if (requestId.isEmpty()) {
            return null;
        }
        String diffId = diffIdByRequestId.get(requestId);
        ProjectDiffJob earlier = diffId == null ? null : jobs.get(diffId);
        if (earlier != null && !earlier.inputKey().equals(inputKey)) {
            throw new IllegalArgumentException("The requestId '" + requestId + "' was already used for a comparison of other projects. Use a new requestId for each comparison.");
        }
        return earlier;
    }

    // The results to delete to make room for one more comparison, or a refusal saying when room will be made.
    // Nothing is deleted unless that makes enough room. Called with the lock held.
    private List<ProjectDiffJob> makeRoom(Instant now) {
        if (jobs.size() < maxStoredJobs && reservedBytes + maxJobBytes <= maxTotalBytes) {
            return List.of();
        }

        Duration grace = Duration.ofMinutes(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES);
        List<ProjectDiffJob> candidates = new ArrayList<>();
        for (ProjectDiffJob job : jobs.values()) {
            Instant freedAt = job.freedAt(grace);
            if (freedAt != null && job.lastDeliveredAt() != null && !freedAt.isAfter(now)) {
                candidates.add(job);
            }
        }
        candidates.sort(Comparator.comparing(ProjectDiffJob::lastDeliveredAt));

        int kept = jobs.size();
        long bytes = reservedBytes;
        List<ProjectDiffJob> evicted = new ArrayList<>();
        for (ProjectDiffJob candidate : candidates) {
            if (kept < maxStoredJobs && bytes + maxJobBytes <= maxTotalBytes) {
                break;
            }
            evicted.add(candidate);
            kept--;
            bytes -= candidate.reservedBytes();
        }

        if (kept >= maxStoredJobs) {
            throw new IllegalStateException("This server already keeps " + jobs.size() + " comparisons, which is as many as it keeps. " + whenRoomIsMade(grace) + pendingDeletionNote());
        }
        if (bytes + maxJobBytes > maxTotalBytes) {
            throw new IllegalStateException("The disk space set aside for comparisons is used up by the results kept. " + whenRoomIsMade(grace) + pendingDeletionNote());
        }
        return evicted;
    }

    // When a result kept will make room, for a refusal. Called with the lock held.
    private String whenRoomIsMade(Duration grace) {
        Instant earliest = null;
        for (ProjectDiffJob job : jobs.values()) {
            Instant freedAt = job.freedAt(grace);
            if (freedAt != null && (earliest == null || freedAt.isBefore(earliest))) {
                earliest = freedAt;
            }
        }
        String rule = "A result is deleted to make room once it has been fetched and " + grace.toMinutes() + " minutes have passed since it was last fetched; one never fetched is kept until its 'expiresAt'.";
        if (earliest == null) {
            return "Every one of them is still in progress; try again once they have ended and their results have been fetched. " + rule;
        }
        return "Try again at " + format(earliest) + " or later. " + rule;
    }

    // The preparation failed or was refused
    public void discard(ProjectDiffJob job) {
        synchronized (lock) {
            forget(job);
            preparing--;
            lock.notifyAll();
        }
        deleteOrRemember(job.directory(), null);
    }

    // Delete a folder, or remember it to delete later when it cannot be deleted now.
    void deleteOrRemember(Path directory, ProjectDiffJob owner) {
        if (deleter.delete(directory)) {
            return;
        }
        long bytes = owner == null ? ProjectDiffStore.sizeOf(directory) : 0;
        synchronized (lock) {
            if (pendingDeletions.containsKey(directory)) {
                return;
            }
            pendingDeletions.put(directory, new PendingDeletion(directory, bytes, owner));
            reservedBytes += bytes;
        }
        log.info("Could not delete {} ({} bytes), probably because Astah still holds a file in it open. It stays counted in the disk space of the comparisons and is deleted once it can be.",
                directory, bytes);
    }

    // Try again to delete the folders that could not be deleted
    public void retryPendingDeletions() {
        List<PendingDeletion> pending;
        synchronized (lock) {
            pending = new ArrayList<>(pendingDeletions.values());
        }
        for (PendingDeletion deletion : pending) {
            boolean deleted = deleter.delete(deletion.directory);
            // A kept comparison that owned the folder takes less disk space now
            long ownerSize = deleted && deletion.owner != null ? ProjectDiffStore.sizeOf(deletion.owner.directory()) : -1;
            int attempts;
            synchronized (lock) {
                if (pendingDeletions.get(deletion.directory) != deletion) {
                    continue;
                }
                attempts = ++deletion.attempts;
                if (deleted) {
                    pendingDeletions.remove(deletion.directory);
                    reservedBytes -= deletion.bytes;
                    ProjectDiffJob owner = deletion.owner;
                    if (owner != null && jobs.get(owner.diffId()) == owner && !owner.inProgress()) {
                        reservedBytes += ownerSize - owner.reservedBytes();
                        owner.updateReservedBytes(ownerSize);
                    }
                }
            }
            if (deleted) {
                log.info("Deleted {} at attempt {}", deletion.directory, attempts);
            } else {
                log.debug("Still could not delete {} (attempt {})", deletion.directory, attempts);
            }
        }
    }

    // What a refusal says about the folders that could not be deleted yet, which take disk space too
    private String pendingDeletionNote() {
        if (pendingDeletions.isEmpty()) {
            return "";
        }
        long bytes = pendingDeletions.values().stream().mapToLong(deletion -> deletion.bytes).sum();
        return " " + pendingDeletions.size() + " folders of earlier comparisons (" + bytes + " bytes) could not be deleted yet, because Astah still holds files in them open; they are deleted as soon as it lets go.";
    }

    // Hand a prepared comparison to the background
    public void submit(ProjectDiffJob job, Runnable work) {
        synchronized (lock) {
            if (!accepting) {
                throw new IllegalStateException("The MCP server is stopping, so it runs no more comparisons.");
            }
            if (executor == null) {
                AtomicInteger threads = new AtomicInteger();
                executor = Executors.newFixedThreadPool(McpServerConfig.PROJECT_DIFF_CONCURRENT_JOBS, runnable -> {
                    Thread thread = new Thread(runnable, "project-diff-" + threads.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                });
            }
            job.markRunning();
            try {
                job.attachFuture(executor.submit(work));
            } catch (RejectedExecutionException e) {
                throw new IllegalStateException("Failed to start the comparison: " + e.getMessage(), e);
            }
            preparing--;
            lock.notifyAll();
        }
    }

    // The background is done with a comparison
    void finished(ProjectDiffJob job) {
        long size = ProjectDiffStore.sizeOf(job.directory());
        synchronized (lock) {
            if (jobs.get(job.diffId()) == job) {
                reservedBytes += size - job.reservedBytes();
                job.updateReservedBytes(size);
            }
        }
    }

    public ProjectDiffJob find(String diffId) {
        sweep();
        synchronized (lock) {
            return jobs.get(diffId);
        }
    }

    // The answer of get_proj_diff_result: wait for the comparison up to the time allowed, then say where it stands, with the page of its changes that starts at startIndex and startPropertyIndex once it is done.
    public DiffResultDTO answer(String diffId, int startIndex, int startPropertyIndex) throws InterruptedException {
        ProjectDiffJob job = find(diffId);
        if (job == null) {
            throw new IllegalArgumentException("No comparison has the diffId '" + diffId + "'. Its result may have been deleted after it expired, or the MCP server was restarted since.");
        }

        job.awaitEnd(resultWaitMillis);
        NormalizationDTO noNormalization = new NormalizationDTO(NormalizationRule.VERSION, false, List.of());
        int maxBytes = McpServerConfig.PROJECT_DIFF_MAX_RESPONSE_BYTES;

        return switch (job.state()) {
            case DONE -> {
                DiffResultDTO header = job.result();
                try {
                    ResultPages.checkStart(header, startIndex, startPropertyIndex);
                    // Given out now: kept at least for the read grace, and not deleted to make room meanwhile (makeRoom)
                    Instant now = Instant.now();
                    job.keepUntil(now.plus(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES, ChronoUnit.MINUTES));
                    job.delivered(now);
                    yield ResultPages.page(header, format(job.expiresAt()), startIndex, startPropertyIndex, maxBytes, McpServerConfig.PROJECT_DIFF_MAX_RESPONSE_VALUE_CHARS);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read the result of the comparison " + diffId + ": " + e.getMessage(), e);
                }
            }
            case FAILED -> {
                requireFirstPage(startIndex, startPropertyIndex, "the comparison failed and has no changes");
                Instant now = Instant.now();
                job.keepUntil(now.plus(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES, ChronoUnit.MINUTES));
                job.delivered(now);
                yield ResponseLimiter.fitHeader(new DiffResultDTO(job.diffId(), DiffStatus.FAILED, format(job.expiresAt()), job.failure(), orEmpty(job.base()), orEmpty(job.target()),
                        noNormalization, List.of(), ProjectDiffRunner.NOT_COMPARED, SummaryDTO.empty(), 0, 0, -1, 0, List.of(), false, false, ""), maxBytes);
            }
            case PREPARING, RUNNING -> {
                requireFirstPage(startIndex, startPropertyIndex, "the comparison is still running");
                yield ResponseLimiter.fitHeader(new DiffResultDTO(job.diffId(), DiffStatus.RUNNING, "", DiffFailureDTO.empty(), orEmpty(job.base()), orEmpty(job.target()),
                        noNormalization, List.of(), ProjectDiffRunner.NOT_COMPARED, SummaryDTO.empty(), 0, 0, -1, 0, List.of(), false, false, ""), maxBytes);
            }
        };
    }

    private static void requireFirstPage(int startIndex, int startPropertyIndex, String why) {
        if (startIndex != 0 || startPropertyIndex != 0) {
            throw new IllegalArgumentException("The startIndex and the startPropertyIndex must be 0 here, because " + why + ".");
        }
    }

    private static ProjectSideDTO orEmpty(ProjectSideDTO side) {
        return side != null ? side : new ProjectSideDTO(ProjectKind.FILE, "", "", "", "", "", "", -1, List.of());
    }

    static String format(Instant instant) {
        return instant == null ? "" : OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()).toString();
    }

    // Delete the results kept past their time, and try again to delete the folders that could not be deleted
    private void sweep() {
        retryPendingDeletions();

        Instant now = Instant.now();
        List<ProjectDiffJob> expired = new ArrayList<>();
        synchronized (lock) {
            for (ProjectDiffJob job : jobs.values()) {
                if (job.expired(now)) {
                    expired.add(job);
                }
            }
            for (ProjectDiffJob job : expired) {
                forget(job);
            }
        }
        for (ProjectDiffJob job : expired) {
            log.debug("Delete the expired comparison {}", job.diffId());
            deleteOrRemember(job.directory(), null);
        }
    }

    private void forget(ProjectDiffJob job) {
        if (jobs.remove(job.diffId()) == job) {
            reservedBytes -= job.reservedBytes();
            if (!job.requestId().isEmpty()) {
                diffIdByRequestId.remove(job.requestId(), job.diffId());
            }
        }
    }

    @Override
    public Process start(ProcessBuilder builder) throws IOException {
        synchronized (lock) {
            if (!accepting) {
                throw new IOException("The MCP server is stopping, so it starts no process.");
            }
            Process process = builder.start();
            processes.add(process);
            return process;
        }
    }

    @Override
    public void finished(Process process) {
        synchronized (lock) {
            processes.remove(process);
        }
    }

    // Stop every comparison, and say whether everything that writes to the comparisons' files is known to have stopped.
    public boolean shutdown() {
        List<ProjectDiffJob> all;
        List<Process> running;
        ExecutorService background;
        synchronized (lock) {
            accepting = false;
            all = new ArrayList<>(jobs.values());
            running = new ArrayList<>(processes);
            background = executor;
        }
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(stopWaitMillis);

        for (ProjectDiffJob job : all) {
            job.requestStop();
        }

        boolean confirmed = true;
        for (Process process : running) {
            process.destroyForcibly();
        }
        try {
            for (Process process : running) {
                if (!process.waitFor(remainingMillis(deadline), TimeUnit.MILLISECONDS)) {
                    log.warn("The process {} of a comparison did not end", process.pid());
                    confirmed = false;
                }
            }

            if (background != null) {
                background.shutdownNow();
                if (!background.awaitTermination(remainingMillis(deadline), TimeUnit.MILLISECONDS)) {
                    log.warn("The background of the comparisons did not stop");
                    confirmed = false;
                }
            }

            synchronized (lock) {
                while (preparing > 0) {
                    long remaining = remainingMillis(deadline);
                    if (remaining <= 0) {
                        log.warn("{} comparisons were still being prepared", preparing);
                        confirmed = false;
                        break;
                    }
                    lock.wait(remaining);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            confirmed = false;
        }

        // Once more before the workspace is deleted; what still cannot be deleted is left to the next start (WorkspaceDirectories)
        retryPendingDeletions();
        return confirmed;
    }

    private static long remainingMillis(long deadlineNanos) {
        return Math.max(0, TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime()));
    }

    // For tests
    long reservedBytes() {
        synchronized (lock) {
            return reservedBytes;
        }
    }

    // For tests
    int pendingDeletionCount() {
        synchronized (lock) {
            return pendingDeletions.size();
        }
    }
}
