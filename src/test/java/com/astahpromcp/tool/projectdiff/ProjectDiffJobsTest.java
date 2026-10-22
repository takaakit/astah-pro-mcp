package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.projectdiff.engine.ProjectDiffEngine;
import com.astahpromcp.tool.projectdiff.outputdto.DiffFailureDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStage;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStatus;
import com.astahpromcp.tool.projectdiff.outputdto.NormalizationDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectKind;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.astahpromcp.tool.projectdiff.outputdto.SummaryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectDiffJobsTest {

    @TempDir
    Path root;

    // Waits for a result 200 ms, and for stopping 5 seconds
    private ProjectDiffJobs jobs(int maxJobsInProgress, int maxStoredJobs, long maxJobBytes, long maxTotalBytes) {
        return new ProjectDiffJobs(root.resolve("project-diff"), maxJobsInProgress, maxStoredJobs, maxJobBytes, maxTotalBytes, 200, 5_000);
    }

    private static ProjectSideDTO side(String path) {
        return new ProjectSideDTO(ProjectKind.FILE, path, "p", "", "", "", "", -1, List.of());
    }

    // Fails the first given number of deletions, as when Astah holds a file of the folder open, then deletes for real
    private static final class FailingDeleter implements ProjectDiffJobs.FolderDeleter {
        private final AtomicInteger failuresLeft;
        final AtomicInteger calls = new AtomicInteger();

        FailingDeleter(int failures) {
            this.failuresLeft = new AtomicInteger(failures);
        }

        @Override
        public boolean delete(Path directory) {
            calls.incrementAndGet();
            if (Files.exists(directory) && failuresLeft.getAndDecrement() > 0) {
                return false;
            }
            return ProjectDiffStore.delete(directory);
        }
    }

    private ProjectDiffJobs jobs(int maxStoredJobs, ProjectDiffJobs.FolderDeleter deleter) {
        return new ProjectDiffJobs(root.resolve("project-diff"), 2, maxStoredJobs, 100, 1_000, 200, 5_000, deleter);
    }

    // A comparison that ended with a result of no changes, written to its folder the way the background writes it
    private static ProjectDiffJob done(ProjectDiffJobs jobs, String requestId, Instant expiresAt) throws Exception {
        ProjectDiffJob job = jobs.admit(requestId, "a\nb").job();
        Files.createDirectories(job.directory());
        jobs.submit(job, () -> {
            try (ResultWriter writer = new ResultWriter(ProjectDiffStore.resultOf(job.directory()), job.diffId(), side("a"), side("b"), List.of(), ProjectDiffRunner.NOT_COMPARED)) {
                writer.begin(new ProjectDiffEngine.Outcome(SummaryDTO.empty(), new NormalizationDTO(1, true, List.of()), List.of()));
                writer.finish();
                job.complete(writer.header(), expiresAt);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        assertTrue(job.awaitEnd(5_000));
        jobs.finished(job);
        return job;
    }

    // A comparison whose folder holds a file of 40 bytes
    private static ProjectDiffJob withFile(ProjectDiffJobs jobs, String requestId) throws IOException {
        ProjectDiffJob job = jobs.admit(requestId, "a\nb").job();
        Files.createDirectories(job.directory().resolve("target"));
        Files.write(job.directory().resolve("target").resolve("out.xmi.part"), new byte[40]);
        return job;
    }

    @Test
    void admit_ok_returnsTheEarlierComparisonForTheSameRequestId() {
        ProjectDiffJobs jobs = jobs(2, 10, 100, 1_000);

        ProjectDiffJobs.Admission first = jobs.admit("req-1", "a\nb");
        ProjectDiffJobs.Admission again = jobs.admit("req-1", "a\nb");

        assertTrue(first.created());
        assertFalse(again.created());
        assertSame(first.job(), again.job());
    }

    @Test
    void admit_ng_refusesARequestIdUsedForOtherProjects() {
        ProjectDiffJobs jobs = jobs(2, 10, 100, 1_000);
        jobs.admit("req-1", "a\nb");

        assertThrows(IllegalArgumentException.class, () -> jobs.admit("req-1", "a\nc"));
    }

    @Test
    void earlier_ok_findsTheComparisonOfTheSameRequestIdWithoutTakingOneIn() {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        ProjectDiffJob job = jobs.admit("req-1", "a\nb").job();

        assertSame(job, jobs.earlier("req-1", "a\nb"));
        // Nothing for a requestId not used yet, nor for an empty one
        assertNull(jobs.earlier("req-2", "a\nb"));
        assertNull(jobs.earlier("", "a\nb"));
        assertEquals(100, jobs.reservedBytes());
    }

    @Test
    void earlier_ng_refusesARequestIdUsedForOtherProjects() {
        ProjectDiffJobs jobs = jobs(2, 10, 100, 1_000);
        jobs.admit("req-1", "a\nb");

        assertThrows(IllegalArgumentException.class, () -> jobs.earlier("req-1", "a\nc"));
    }

    @Test
    void admit_ng_refusesBeyondTheComparisonsInProgress() {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        jobs.admit("", "a\nb");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> jobs.admit("", "a\nb"));
        assertTrue(e.getMessage().contains("in progress"), e.getMessage());
    }

    @Test
    void admit_ng_refusesWhenTheDiskSpaceIsSpokenFor() {
        ProjectDiffJobs jobs = jobs(5, 10, 400, 1_000);
        jobs.admit("", "1");
        jobs.admit("", "2");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> jobs.admit("", "3"));
        assertTrue(e.getMessage().contains("disk space"), e.getMessage());
    }

    @Test
    void discard_ok_releasesEverythingTheComparisonHeld() throws Exception {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        ProjectDiffJob job = jobs.admit("req-1", "a\nb").job();
        Files.createDirectories(job.directory());

        jobs.discard(job);

        assertEquals(0, jobs.reservedBytes());
        assertFalse(Files.exists(job.directory()));
        assertNull(jobs.find(job.diffId()));
        // The requestId is free again, and the slot too
        assertTrue(jobs.admit("req-1", "a\nb").created());
    }

    @Test
    void discard_ok_remembersAFolderItCannotDeleteAndKeepsCountingItsSpace() throws Exception {
        ProjectDiffJobs jobs = jobs(10, new FailingDeleter(1));
        ProjectDiffJob job = withFile(jobs, "req-1");

        jobs.discard(job);

        assertTrue(Files.exists(job.directory()));
        assertEquals(1, jobs.pendingDeletionCount());
        assertEquals(40, jobs.reservedBytes());
        // The requestId is released all the same
        ProjectDiffJobs.Admission again = jobs.admit("req-1", "a\nb");
        assertTrue(again.created());
        jobs.discard(again.job());
    }

    @Test
    void retryPendingDeletions_ok_deletesTheFolderOnceItCanAndGivesItsSpaceBack() throws Exception {
        FailingDeleter deleter = new FailingDeleter(2);
        ProjectDiffJobs jobs = jobs(10, deleter);
        ProjectDiffJob job = withFile(jobs, "");
        jobs.discard(job);

        // The second attempt fails too
        jobs.retryPendingDeletions();
        assertEquals(1, jobs.pendingDeletionCount());
        assertEquals(40, jobs.reservedBytes());

        // Every call of the tools tries again
        assertNull(jobs.find("d404-000000"));
        assertEquals(0, jobs.pendingDeletionCount());
        assertEquals(0, jobs.reservedBytes());
        assertFalse(Files.exists(job.directory()));
        assertEquals(3, deleter.calls.get());
    }

    @Test
    void admit_ok_doesNotCountAFolderWaitingToBeDeletedAsAKeptComparison() throws Exception {
        ProjectDiffJobs jobs = jobs(1, new FailingDeleter(100));
        jobs.discard(withFile(jobs, ""));
        assertEquals(1, jobs.pendingDeletionCount());

        assertTrue(jobs.admit("", "c\nd").created());
    }

    @Test
    void admit_ng_saysWhatTheFoldersWaitingToBeDeletedTakeWhenItRefuses() throws Exception {
        // 100 bytes are set aside for each comparison, out of 120 in all
        ProjectDiffJobs jobs = new ProjectDiffJobs(root.resolve("project-diff"), 2, 10, 100, 120, 200, 5_000, new FailingDeleter(100));
        jobs.discard(withFile(jobs, ""));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> jobs.admit("", "c\nd"));

        assertTrue(e.getMessage().contains("1 folders of earlier comparisons (40 bytes)"), e.getMessage());
    }

    @Test
    void shutdown_ok_triesOnceMoreToDeleteTheFoldersWaitingToBeDeleted() throws Exception {
        ProjectDiffJobs jobs = jobs(10, new FailingDeleter(1));
        ProjectDiffJob job = withFile(jobs, "");
        jobs.discard(job);

        assertTrue(jobs.shutdown());

        assertEquals(0, jobs.pendingDeletionCount());
        assertFalse(Files.exists(job.directory()));
    }

    @Test
    void answer_ok_returnsTheResultWithItsExpiryAndKeepsItAWhileLonger() throws Exception {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        ProjectDiffJob job = done(jobs, "", Instant.now().plus(5, ChronoUnit.MINUTES));
        assertNull(job.lastDeliveredAt());

        Instant before = Instant.now();
        DiffResultDTO answer = jobs.answer(job.diffId(), 0, 0);
        Instant after = Instant.now();

        assertEquals(DiffStatus.DONE, answer.status());
        assertEquals(-1, answer.nextIndex());
        // Read with five minutes left, the result is kept for the read grace
        assertFalse(job.expiresAt().isBefore(before.plus(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES, ChronoUnit.MINUTES)));
        assertFalse(job.expiresAt().isAfter(after.plus(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES, ChronoUnit.MINUTES)));
        assertEquals(ProjectDiffJobs.format(job.expiresAt()), answer.expiresAt());
        // and is known to have been given out
        assertNotNull(job.lastDeliveredAt());
        assertTrue(jobs.shutdown());
    }

    @Test
    void answer_ng_refusesAStartIndexOutOfRange() throws Exception {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        ProjectDiffJob job = done(jobs, "", Instant.now().plus(5, ChronoUnit.MINUTES));

        assertThrows(IllegalArgumentException.class, () -> jobs.answer(job.diffId(), 1, 0));
        assertThrows(IllegalArgumentException.class, () -> jobs.answer(job.diffId(), 0, 1));
        // A refused page is not given out
        assertNull(job.lastDeliveredAt());
        assertTrue(jobs.shutdown());
    }

    @Test
    void admit_ok_deletesTheResultGivenOutLongestAgoToMakeRoom() throws Exception {
        ProjectDiffJobs jobs = jobs(2, 2, 100_000, 10_000_000);
        Instant later = Instant.now().plus(1, ChronoUnit.HOURS);
        ProjectDiffJob older = done(jobs, "req-older", later);
        ProjectDiffJob newer = done(jobs, "req-newer", later);
        // Both were given out before the read grace began
        older.delivered(Instant.now().minus(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES + 2, ChronoUnit.MINUTES));
        newer.delivered(Instant.now().minus(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES + 1, ChronoUnit.MINUTES));

        ProjectDiffJobs.Admission admission = jobs.admit("", "c\nd");

        assertTrue(admission.created());
        assertNull(jobs.find(older.diffId()));
        assertFalse(Files.exists(older.directory()));
        assertSame(newer, jobs.find(newer.diffId()));
        // The diffId of the deleted result is unknown, and its requestId starts a new comparison
        assertThrows(IllegalArgumentException.class, () -> jobs.answer(older.diffId(), 0, 0));
        jobs.discard(admission.job());
        ProjectDiffJobs.Admission again = jobs.admit("req-older", "a\nb");
        assertTrue(again.created());
        jobs.discard(again.job());
        assertTrue(jobs.shutdown());
    }

    @Test
    void admit_ng_keepsAResultNotYetGivenOutAndSaysWhenRoomIsMade() throws Exception {
        ProjectDiffJobs jobs = jobs(2, 1, 100_000, 10_000_000);
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
        ProjectDiffJob kept = done(jobs, "", expiresAt);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> jobs.admit("", "c\nd"));

        assertSame(kept, jobs.find(kept.diffId()));
        assertTrue(e.getMessage().contains("Try again at " + ProjectDiffJobs.format(expiresAt)), e.getMessage());
        assertTrue(jobs.shutdown());
    }

    @Test
    void admit_ng_keepsAResultGivenOutWithinTheReadGrace() throws Exception {
        ProjectDiffJobs jobs = jobs(2, 1, 100_000, 10_000_000);
        ProjectDiffJob kept = done(jobs, "", Instant.now().plus(1, ChronoUnit.HOURS));
        jobs.answer(kept.diffId(), 0, 0);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> jobs.admit("", "c\nd"));

        assertSame(kept, jobs.find(kept.diffId()));
        Instant freedAt = kept.lastDeliveredAt().plus(McpServerConfig.PROJECT_DIFF_READ_GRACE_MINUTES, ChronoUnit.MINUTES);
        assertTrue(e.getMessage().contains("Try again at " + ProjectDiffJobs.format(freedAt)), e.getMessage());
        assertTrue(jobs.shutdown());
    }

    @Test
    void answer_ng_refusesAPageBeyondTheFirstWhileRunning() throws Exception {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        ProjectDiffJob job = jobs.admit("", "a\nb").job();
        CountDownLatch release = new CountDownLatch(1);
        jobs.submit(job, () -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThrows(IllegalArgumentException.class, () -> jobs.answer(job.diffId(), 3, 0));
        assertThrows(IllegalArgumentException.class, () -> jobs.answer(job.diffId(), 0, 1));
        release.countDown();
        assertTrue(jobs.shutdown());
    }

    @Test
    void answer_ok_saysARunningComparisonIsRunningAfterTheWait() throws Exception {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        ProjectDiffJob job = jobs.admit("", "a\nb").job();
        job.recordSides(side("a"), side("b"));
        CountDownLatch release = new CountDownLatch(1);
        jobs.submit(job, () -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        DiffResultDTO answer = jobs.answer(job.diffId(), 0, 0);

        assertEquals(DiffStatus.RUNNING, answer.status());
        assertEquals("", answer.expiresAt());
        release.countDown();
        assertTrue(jobs.shutdown());
    }

    @Test
    void answer_ng_saysAnUnknownDiffIdIsUnknown() {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);

        assertThrows(IllegalArgumentException.class, () -> jobs.answer("d404-000000", 0, 0));
    }

    @Test
    void sweep_ok_deletesAResultPastItsTimeAndFreesItsRequestId() throws Exception {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        ProjectDiffJob job = jobs.admit("req-1", "a\nb").job();
        Files.createDirectories(job.directory());
        // A result whose time is already past
        jobs.submit(job, () -> job.fail(new DiffFailureDTO(DiffStage.COMPARE, "x", false), Instant.now().minusSeconds(1)));
        assertTrue(job.awaitEnd(5_000));
        jobs.finished(job);

        assertNull(jobs.find(job.diffId()));
        assertFalse(Files.exists(job.directory()));
        ProjectDiffJobs.Admission again = jobs.admit("req-1", "a\nb");
        assertTrue(again.created());
        jobs.discard(again.job());
        assertTrue(jobs.shutdown());
    }

    @Test
    void shutdown_ok_refusesNewComparisonsAndStopsTheRunningOne() throws Exception {
        ProjectDiffJobs jobs = jobs(2, 10, 100, 1_000);
        ProjectDiffJob job = jobs.admit("", "a\nb").job();
        AtomicReference<Boolean> interrupted = new AtomicReference<>(false);
        CountDownLatch started = new CountDownLatch(1);
        jobs.submit(job, () -> {
            started.countDown();
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));

        assertTrue(jobs.shutdown());

        assertTrue(job.stopRequested());
        assertTrue(interrupted.get());
        assertThrows(IllegalStateException.class, () -> jobs.admit("", "c\nd"));
    }

    @Test
    void shutdown_ng_isNotConfirmedWhileAComparisonIsStillBeingPrepared() {
        // Waits for stopping 300 ms
        ProjectDiffJobs jobs = new ProjectDiffJobs(root.resolve("project-diff"), 2, 10, 100, 1_000, 200, 300);
        ProjectDiffJob job = jobs.admit("", "a\nb").job();

        assertFalse(jobs.shutdown());
        assertTrue(job.stopRequested());

        // The preparation sees the stop and gives up
        jobs.discard(job);
        assertThrows(IllegalStateException.class, () -> jobs.submit(job, () -> { }));
    }

    @Test
    void start_ng_refusesToStartAProcessAfterShutdown() {
        ProjectDiffJobs jobs = jobs(1, 10, 100, 1_000);
        assertTrue(jobs.shutdown());

        assertThrows(IOException.class, () -> jobs.start(new ProcessBuilder("java", "-version")));
    }
}
