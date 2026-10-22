package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.projectdiff.outputdto.DiffFailureDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStatus;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

// One comparison, from the moment it was accepted until its result is deleted.
// Its state changes on several threads (the tool call that prepares it, the background that runs it, the calls that read it, and stopping), so every change goes through a synchronized method.
public final class ProjectDiffJob {

    public enum State {
        PREPARING,
        RUNNING,
        DONE,
        FAILED
    }

    private final String diffId;
    private final String requestId;
    private final String inputKey;
    private final Path directory;
    private final CountDownLatch ended = new CountDownLatch(1);

    private State state = State.PREPARING;
    private boolean stopRequested;
    private ProjectSideDTO base;
    private ProjectSideDTO target;
    private DiffResultDTO result;
    private DiffFailureDTO failure = DiffFailureDTO.empty();
    private Instant expiresAt;
    private Instant lastDeliveredAt;    // When get_proj_diff_result last gave the ended comparison to an agent; null while it never has
    private long reservedBytes;
    private Future<?> future;

    ProjectDiffJob(String diffId, String requestId, String inputKey, Path directory, long reservedBytes) {
        this.diffId = diffId;
        this.requestId = requestId;
        this.inputKey = inputKey;
        this.directory = directory;
        this.reservedBytes = reservedBytes;
    }

    public String diffId() {
        return diffId;
    }

    String requestId() {
        return requestId;
    }

    String inputKey() {
        return inputKey;
    }

    public Path directory() {
        return directory;
    }

    public synchronized State state() {
        return state;
    }

    public synchronized DiffStatus status() {
        return switch (state) {
            case PREPARING, RUNNING -> DiffStatus.RUNNING;
            case DONE -> DiffStatus.DONE;
            case FAILED -> DiffStatus.FAILED;
        };
    }

    public synchronized boolean stopRequested() {
        return stopRequested;
    }

    synchronized void requestStop() {
        stopRequested = true;
        if (future != null) {
            future.cancel(true);
        }
    }

    public synchronized ProjectSideDTO base() {
        return base;
    }

    public synchronized ProjectSideDTO target() {
        return target;
    }

    public synchronized void recordSides(ProjectSideDTO base, ProjectSideDTO target) {
        this.base = base;
        this.target = target;
    }

    // Set before the work is handed to the background, which may finish it before the hand-over returns
    synchronized void markRunning() {
        if (state == State.PREPARING) {
            state = State.RUNNING;
        }
    }

    synchronized void attachFuture(Future<?> future) {
        this.future = future;
        if (stopRequested) {
            future.cancel(true);
        }
    }

    synchronized void complete(DiffResultDTO result, Instant expiresAt) {
        this.result = result;
        this.state = State.DONE;
        this.expiresAt = expiresAt;
        ended.countDown();
    }

    synchronized void fail(DiffFailureDTO failure, Instant expiresAt) {
        this.failure = failure;
        this.state = State.FAILED;
        this.expiresAt = expiresAt;
        ended.countDown();
    }

    public synchronized DiffResultDTO result() {
        return result;
    }

    public synchronized DiffFailureDTO failure() {
        return failure;
    }

    public synchronized Instant expiresAt() {
        return expiresAt;
    }

    // Keep the result at least until the given time
    synchronized void keepUntil(Instant time) {
        if (expiresAt != null && expiresAt.isBefore(time)) {
            expiresAt = time;
        }
    }

    synchronized boolean expired(Instant now) {
        return (state == State.DONE || state == State.FAILED) && expiresAt != null && expiresAt.isBefore(now);
    }

    // get_proj_diff_result gave the ended comparison to an agent
    synchronized void delivered(Instant at) {
        if (state == State.DONE || state == State.FAILED) {
            lastDeliveredAt = at;
        }
    }

    synchronized Instant lastDeliveredAt() {
        return lastDeliveredAt;
    }

    // When the result may be deleted to make room for another comparison
    synchronized Instant freedAt(Duration readGrace) {
        if (state != State.DONE && state != State.FAILED) {
            return null;
        }
        if (lastDeliveredAt == null) {
            return expiresAt;
        }
        Instant afterGrace = lastDeliveredAt.plus(readGrace);
        return expiresAt != null && expiresAt.isBefore(afterGrace) ? expiresAt : afterGrace;
    }

    synchronized boolean inProgress() {
        return state == State.PREPARING || state == State.RUNNING;
    }

    synchronized long reservedBytes() {
        return reservedBytes;
    }

    synchronized void updateReservedBytes(long bytes) {
        this.reservedBytes = bytes;
    }

    // Wait until the job ends, or until the time is up; true when it ended
    boolean awaitEnd(long millis) throws InterruptedException {
        return ended.await(millis, TimeUnit.MILLISECONDS);
    }
}
