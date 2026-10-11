package com.astahpromcp.tool.astah.pro;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Runs work that may call the Astah API on a runner thread of its own, with a deadline, and owns what has to happen when the caller gives up on it.
public final class WatchedRunner {

    // How a run ended, as far as the caller can tell.
    public enum Kind {
        COMPLETED,
        TIMED_OUT,
        INTERRUPTED,
        FAILED
    }

    // What became of one run: value is set only for COMPLETED, failure only for FAILED.
    public record Outcome<T>(Kind kind, T value, Throwable failure) {
    }

    private WatchedRunner() {
    }

    // subject names the work in the suspension reason ("an mcp tool script"). onAbandon runs before the runner is interrupted.
    // Called while the caller holds the Astah API lock: the suspension is registered before this returns, so that whoever acquires the lock next sees it.
    public static <T> Outcome<T> run(String threadName, String subject, long timeoutSeconds, Callable<T> work, Runnable onAbandon) {
        FutureTask<T> task = new FutureTask<>(work);
        // One thread per run: a lingering stuck runner must not poison later runs.
        Thread runner = new Thread(task, threadName);
        runner.setDaemon(true);
        runner.start();

        try {
            return new Outcome<>(Kind.COMPLETED, task.get(timeoutSeconds, TimeUnit.SECONDS), null);

        } catch (TimeoutException e) {
            abandon(runner, onAbandon, String.format("%s that timed out after %d seconds is still running", subject, timeoutSeconds));
            return new Outcome<>(Kind.TIMED_OUT, null, null);

        } catch (InterruptedException e) {
            // The wait was cut short, not the work: this thread stopped waiting while the runner may still be calling the Astah API.
            Thread.currentThread().interrupt();
            abandon(runner, onAbandon, subject + " whose caller stopped waiting is still running");
            return new Outcome<>(Kind.INTERRUPTED, null, null);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return new Outcome<>(Kind.FAILED, null, cause != null ? cause : e);
        }
    }

    public static <T> Outcome<T> run(String threadName, String subject, long timeoutSeconds, Callable<T> work) {
        return run(threadName, subject, timeoutSeconds, work, () -> { });
    }

    private static void abandon(Thread runner, Runnable onAbandon, String reason) {
        try {
            // Before the interrupt, so that a runner woken by it already finds that it must not start further work.
            onAbandon.run();
        } finally {
            // Best effort: a runner blocked in an interruptible call stops here. One stuck in a computation loop cannot be
            // stopped at all: Java has no way to terminate a thread, and killing one mid-transaction would corrupt the model anyway.
            runner.interrupt();
            // The abandoned runner may still be calling the Astah API, so no tool may run until it terminates. Whether waiting
            // helps or Astah must be restarted is not known yet; AstahApiLock diagnoses it when a later tool call is refused.
            AstahApiLock.suspend(runner, reason);
        }
    }
}
