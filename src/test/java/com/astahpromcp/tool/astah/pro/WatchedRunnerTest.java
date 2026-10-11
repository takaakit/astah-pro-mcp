package com.astahpromcp.tool.astah.pro;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class WatchedRunnerTest {

    @AfterEach
    void tearDown() {
        AstahApiLock.clearSuspension();
    }

    @Test
    void run_ok_returnsTheValueOfTheWork() {
        WatchedRunner.Outcome<String> outcome = WatchedRunner.run("test-runner", "a test script", 10, () -> "done");

        assertEquals(WatchedRunner.Kind.COMPLETED, outcome.kind());
        assertEquals("done", outcome.value());
        assertNull(AstahApiLock.suspensionReason(), "A finished runner must not suspend Astah API access");
    }

    @Test
    void run_ng_reportsAFailureOfTheWork() {
        IllegalStateException failure = new IllegalStateException("work failed");
        WatchedRunner.Outcome<String> outcome = WatchedRunner.run("test-runner", "a test script", 10, () -> { throw failure; });

        assertEquals(WatchedRunner.Kind.FAILED, outcome.kind());
        assertSame(failure, outcome.failure());
        assertNull(AstahApiLock.suspensionReason(), "A runner that failed has stopped, so it must not suspend Astah API access");
    }

    // The runner must already know that it was abandoned when the interrupt wakes it, or it could start further work.
    // Observed rather than raced: the runner spins without consuming its interrupt, so its flag tells onAbandon whether
    // the interrupt has already been sent. A runner that wakes up and reads a flag would almost always lose to onAbandon.
    @Test
    void run_ng_abandonsBeforeInterruptingWhenTheWaitTimesOut() throws Exception {
        AtomicReference<Thread> runner = new AtomicReference<>();
        AtomicBoolean interruptedBeforeAbandon = new AtomicBoolean(true);
        AtomicBoolean release = new AtomicBoolean();
        try {
            WatchedRunner.Outcome<String> outcome = WatchedRunner.run("test-runner", "a test script", 1,
                    () -> {
                        runner.set(Thread.currentThread());
                        while (!release.get()) {
                            Thread.onSpinWait();
                        }
                        return "late";
                    },
                    () -> interruptedBeforeAbandon.set(runner.get().isInterrupted()));

            assertEquals(WatchedRunner.Kind.TIMED_OUT, outcome.kind());
            assertFalse(interruptedBeforeAbandon.get(), "onAbandon must run before the runner is interrupted");
            assertTrue(runner.get().isInterrupted(), "The runner should have been interrupted after onAbandon");
            String reason = AstahApiLock.suspensionReason();
            assertNotNull(reason, "The abandoned runner is still alive, so access must be suspended");
            assertTrue(reason.startsWith("a test script that timed out after 1 seconds is still running. "), reason);

        } finally {
            release.set(true);
        }
        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS), "Access should be restored once the runner terminates");
    }

    // The suspension must be visible the moment run returns: a caller still holding the Astah API lock releases it right after.
    @Test
    void run_ng_suspendsBeforeReturningWhenTheWaitIsInterrupted() throws Exception {
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean abandoned = new AtomicBoolean();
        AtomicReference<WatchedRunner.Outcome<String>> outcome = new AtomicReference<>();
        AtomicReference<String> reasonOnReturn = new AtomicReference<>();
        AtomicBoolean interruptKept = new AtomicBoolean();

        Thread caller = new Thread(() -> {
            outcome.set(WatchedRunner.run("test-runner", "a test script", 60, () -> {
                running.countDown();
                return holdIgnoringInterrupts(release);
            }, () -> abandoned.set(true)));
            // Read on the caller's own thread right after run returns: the moment a caller holding the lock would release it
            reasonOnReturn.set(AstahApiLock.suspensionReason());
            interruptKept.set(Thread.currentThread().isInterrupted());
        }, "interrupted-caller");
        caller.setDaemon(true);
        caller.start();
        try {
            assertTrue(running.await(10, TimeUnit.SECONDS), "The runner should have started");
            caller.interrupt();
            caller.join(10_000);

            assertFalse(caller.isAlive(), "The interrupted caller should have stopped waiting");
            assertEquals(WatchedRunner.Kind.INTERRUPTED, outcome.get().kind());
            assertTrue(abandoned.get(), "onAbandon must run when the wait is interrupted");
            assertNotNull(reasonOnReturn.get(), "Access must be suspended before run returns");
            assertTrue(reasonOnReturn.get().startsWith("a test script whose caller stopped waiting is still running. "),
                    reasonOnReturn.get());
            assertTrue(interruptKept.get(), "The caller's interrupt flag must be restored");

        } finally {
            release.countDown();
        }
        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS), "Access should be restored once the runner terminates");
    }

    // The suspension is the protection; a failing onAbandon must not skip it.
    @Test
    void run_ng_suspendsEvenWhenOnAbandonThrows() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        try {
            assertThrows(IllegalStateException.class, () -> WatchedRunner.run("test-runner", "a test script", 1,
                    () -> holdIgnoringInterrupts(release),
                    () -> { throw new IllegalStateException("abandon failed"); }));

            assertNotNull(AstahApiLock.suspensionReason(), "Access must be suspended even though onAbandon failed");

        } finally {
            release.countDown();
        }
        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS), "Access should be restored once the runner terminates");
    }

    // Stands in for a script that does not stop when interrupted, so that the runner outlives run and the suspension can be observed.
    private static String holdIgnoringInterrupts(CountDownLatch release) {
        boolean released = false;
        while (!released) {
            try {
                released = release.await(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Swallowed on purpose: this stands in for a script that does not stop when it is interrupted.
            }
        }
        return "late";
    }

    private static boolean awaitAccessRestored(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            if (AstahApiLock.suspensionReason() == null) {
                return true;
            }
            Thread.sleep(50);
        }

        return AstahApiLock.suspensionReason() == null;
    }
}
