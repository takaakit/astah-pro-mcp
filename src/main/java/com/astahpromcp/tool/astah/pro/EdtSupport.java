package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.config.McpServerConfig;
import lombok.extern.slf4j.Slf4j;

import javax.swing.SwingUtilities;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class EdtSupport {

    private EdtSupport() {
    }

    // Wait until everything Astah queued on the EDT has run, so that whatever reads the model next sees the edit that produced it.
    // nextStep names what the caller goes on to do, which the log reports when the wait is cut short.
    public static void drain(String toolName, String nextStep) {
        drain(toolName, nextStep, McpServerConfig.EDT_FLUSH_TIMEOUT_SECONDS);
    }

    static void drain(String toolName, String nextStep, long timeoutSeconds) {
        switch (drainAndReport(timeoutSeconds)) {
            case DRAINED -> {
            }
            case TIMED_OUT -> log.warn("EDT did not drain within {} seconds; {} anyway @tool={}", timeoutSeconds, nextStep, toolName);
            case INTERRUPTED -> log.warn("Interrupted while draining the EDT; {} anyway @tool={}", nextStep, toolName);
        }
    }

    // How a wait for the EDT ended
    public enum DrainOutcome {
        DRAINED,
        TIMED_OUT,
        INTERRUPTED
    }

    // Wait until everything Astah queued on the EDT before this call has run, and say whether it did.
    public static DrainOutcome drainAndReport(long timeoutSeconds) {
        // On the EDT nothing queued can run before the caller returns to the queue, so there is nothing to wait for
        if (SwingUtilities.isEventDispatchThread()) {
            return DrainOutcome.DRAINED;
        }

        // A thread told to stop does not wait, and keeps its interrupt. Posting to the EDT from it could also lose that interrupt: when AWT has to restart a stopped EDT, AWTAutoShutdown.activateBlockerThread() swallows it.
        if (Thread.currentThread().isInterrupted()) {
            return DrainOutcome.INTERRUPTED;
        }

        // Wait with a bounded timeout instead of invokeAndWait: a blocked EDT must not hold the caller forever.
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(latch::countDown);
        try {
            return latch.await(timeoutSeconds, TimeUnit.SECONDS) ? DrainOutcome.DRAINED : DrainOutcome.TIMED_OUT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DrainOutcome.INTERRUPTED;
        }
    }

    // Run a task that touches Swing components on the EDT, wait for it, and throw what it throws.
    // Swing components may be created, read and changed only on the EDT, so finding a component belongs in the task too.
    // No timeout, unlike drain: once the EDT has started the task it cannot be called off, and no code on the EDT takes the Astah API lock, so the wait cannot deadlock.
    public static <T> T call(Callable<T> task) throws Exception {
        // A thread told to stop does not start the task, and keeps its interrupt. Checked before any AWT call: the first one in the JVM
        // initializes the toolkit, and that, like restarting a stopped EDT (see drain), swallows the interrupt in AWTAutoShutdown.activateBlockerThread().
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Interrupted before the task was run on the EDT.");
        }

        if (SwingUtilities.isEventDispatchThread()) {
            return task.call();
        }

        FutureTask<T> future = new FutureTask<>(task);
        SwingUtilities.invokeLater(future);
        try {
            return future.get();

        } catch (InterruptedException e) {
            // A task the EDT has not started yet must not run after its caller has given up on it.
            future.cancel(false);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the EDT.", e);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            if (cause instanceof Error err) {
                throw err;
            }
            throw e;
        }
    }
}
