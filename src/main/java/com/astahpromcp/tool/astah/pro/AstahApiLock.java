package com.astahpromcp.tool.astah.pro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

// Process-wide lock that serializes access to the Astah API.
@Slf4j
public final class AstahApiLock {

    // Fair lock: requests from multiple AI agents are served FIFO.
    public static final ReentrantLock LOCK = new ReentrantLock(true);

    // Grace period before concluding that the abandoned thread is ignoring its interrupt: a thread waiting at an interruptible point dies within microseconds, but reporting "it ignored the interrupt" in that instant would be a lie.
    private static final long INTERRUPT_GRACE_NANOS = TimeUnit.SECONDS.toNanos(1);

    // A thread that a timed-out tool gave up on, with the reason to report to the client and the instant it was interrupted. While that thread is alive it may still be calling the Astah API.
    private record Suspension(Thread cause, String reason, long interruptedAtNanos) {
    }

    private static final AtomicReference<Suspension> SUSPENSION = new AtomicReference<>();

    private AstahApiLock() {
    }

    // Declare that a tool interrupted and then abandoned a thread that may still be using the Astah API. Holding the lock cannot protect the model from such a thread, so every tool call must be refused until it terminates.
    public static void suspend(Thread cause, String reason) {
        SUSPENSION.set(new Suspension(cause, reason, System.nanoTime()));
        log.error("Astah API access suspended: {}", reason);
    }

    // The reason Astah API access is currently unusable, or null when it is usable.
    // Derived from the abandoned thread's liveness, so the suspension lifts by itself as soon as that thread terminates; there is no state to reset and no window where the reason and the thread disagree.
    public static String suspensionReason() {
        Suspension suspension = SUSPENSION.get();
        if (suspension == null || !suspension.cause().isAlive()) {
            return null;
        }

        return suspension.reason() + ". " + recoveryAdvice(suspension);
    }

    // Whether the abandoned thread has ignored its interrupt decides which recovery the client should expect: waiting only helps a thread that can still reach an interruptible point.
    private static String recoveryAdvice(Suspension suspension) {
        long elapsedNanos = System.nanoTime() - suspension.interruptedAtNanos();
        if (elapsedNanos < INTERRUPT_GRACE_NANOS) {
            return "It was interrupted a moment ago and may still be stopping, so retry shortly.";
        }

        long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(elapsedNanos);

        if (suspension.cause().isInterrupted()) {
            return String.format(
                "It has ignored its interrupt for %ds, so it is not waiting at an interruptible point (most likely a computation loop) and will not stop on its own: ask the user to restart Astah.",
                elapsedSeconds);
        }

        return String.format(
            "It consumed its interrupt but has kept running for %ds, so the script is swallowing the interruption and will not stop on its own: ask the user to restart Astah.",
            elapsedSeconds);
    }

    // Visible for tests so that a test which suspends access cannot leak that state into later tests.
    public static void clearSuspension() {
        SUSPENSION.set(null);
    }
}
