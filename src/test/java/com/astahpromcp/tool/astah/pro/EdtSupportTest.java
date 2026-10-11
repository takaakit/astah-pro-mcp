package com.astahpromcp.tool.astah.pro;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class EdtSupportTest {

    @Test
    void drain_ok_runsWorkQueuedBeforeIt() {
        AtomicBoolean done = new AtomicBoolean();
        // Slow on purpose: a drain that did not wait would return long before this finishes, so it would fail every run.
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            done.set(true);
        });

        EdtSupport.drain("dummy_tool", "carrying on with the test");

        assertTrue(done.get(), "Work queued on the EDT before the drain must have run when it returns");
    }

    @Test
    void drain_ok_returnsAtOnceOnTheEventDispatchThread() throws Exception {
        long started = System.nanoTime();
        SwingUtilities.invokeAndWait(() -> EdtSupport.drain("dummy_tool", "carrying on with the test", 10));
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        // Waiting from the EDT for its own no-op could only end at the timeout
        assertTrue(elapsedMillis < 5_000, "The drain on the EDT should return at once: " + elapsedMillis + " ms");
    }

    // The case the interrupt check exists for. AWT stops an idle EDT after about a second, and restarting it from an
    // interrupted thread is what swallows the interrupt (AWTAutoShutdown.activateBlockerThread). While the EDT is running,
    // posting from an interrupted thread loses nothing, so only a stopped EDT shows that the drain does not post.
    @Test
    void drain_ok_keepsTheInterruptWhenTheEdtWouldHaveToBeRestarted() throws Exception {
        assumeTrue(awaitNoEventDispatchThread(10, TimeUnit.SECONDS), "The EDT did not stop, so its restart cannot be reproduced");

        AtomicBoolean interruptKept = new AtomicBoolean();
        Thread caller = new Thread(() -> {
            Thread.currentThread().interrupt();
            EdtSupport.drain("dummy_tool", "carrying on with the test", 10);
            interruptKept.set(Thread.currentThread().isInterrupted());
        }, "interrupted-caller");
        caller.start();
        caller.join(15_000);

        assertFalse(caller.isAlive(), "The drain should have returned");
        assertTrue(interruptKept.get(), "The caller's interrupt flag must survive the drain, even when the EDT is down");
    }

    @Test
    void drain_ng_givesUpAfterTheTimeoutWhenTheEdtIsBlocked() throws Exception {
        CountDownLatch releaseEdt = blockEdt();
        try {
            long started = System.nanoTime();
            EdtSupport.drain("dummy_tool", "carrying on with the test", 1);
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

            assertTrue(elapsedMillis >= 900 && elapsedMillis < 5_000,
                    "The drain should give up at the timeout: " + elapsedMillis + " ms");
            assertFalse(Thread.currentThread().isInterrupted(), "Giving up must not leave an interrupt behind");
        } finally {
            releaseEdt.countDown();
        }
    }

    @Test
    void call_ok_runsTheTaskOnTheEventDispatchThread() throws Exception {
        boolean ranOnEdt = EdtSupport.call(SwingUtilities::isEventDispatchThread);

        assertTrue(ranOnEdt, "The task should have run on the EDT");
    }

    @Test
    void call_ok_runsTheTaskInPlaceOnTheEventDispatchThread() throws Exception {
        AtomicReference<String> result = new AtomicReference<>();
        long started = System.nanoTime();
        // Posting from the EDT and waiting there for the task could never end
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(EdtSupport.call(() -> "done"));
            } catch (Exception e) {
                fail(e);
            }
        });
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        assertEquals("done", result.get());
        assertTrue(elapsedMillis < 5_000, "The call on the EDT should return at once: " + elapsedMillis + " ms");
    }

    @Test
    void call_ng_throwsWhatTheTaskThrows() {
        Exception thrown = new Exception("dummy failure");

        Exception caught = assertThrows(Exception.class, () -> EdtSupport.call(() -> {
            throw thrown;
        }));

        assertSame(thrown, caught, "The task's own exception should reach the caller, not a wrapper");
    }

    @Test
    void call_ng_doesNotPostTheTaskFromAnInterruptedThread() throws Exception {
        AtomicBoolean ran = new AtomicBoolean();
        AtomicReference<Throwable> caught = new AtomicReference<>();
        AtomicBoolean interruptKept = new AtomicBoolean();
        Thread caller = new Thread(() -> {
            Thread.currentThread().interrupt();
            try {
                EdtSupport.call(() -> ran.getAndSet(true));
            } catch (Throwable t) {
                caught.set(t);
            }
            interruptKept.set(Thread.currentThread().isInterrupted());
        }, "interrupted-caller");
        caller.start();
        caller.join(15_000);

        assertFalse(caller.isAlive(), "The call should have returned");
        assertInstanceOf(RuntimeException.class, caught.get());
        assertTrue(interruptKept.get(), "The caller's interrupt flag must survive the call");
        SwingUtilities.invokeAndWait(() -> { });
        assertFalse(ran.get(), "A task from an interrupted thread must not run");
    }

    @Test
    void call_ng_doesNotRunTheTaskAfterTheCallerIsInterruptedWhileWaiting() throws Exception {
        AtomicBoolean ran = new AtomicBoolean();
        AtomicReference<Throwable> caught = new AtomicReference<>();
        AtomicBoolean interruptKept = new AtomicBoolean();
        CountDownLatch releaseEdt = blockEdt();
        Thread caller = new Thread(() -> {
            try {
                EdtSupport.call(() -> ran.getAndSet(true));
            } catch (Throwable t) {
                caught.set(t);
            }
            interruptKept.set(Thread.currentThread().isInterrupted());
        }, "waiting-caller");
        try {
            caller.start();
            // The task is queued behind the blocker, so the caller parks in the wait for it
            assertTrue(awaitState(caller, Thread.State.WAITING, 10, TimeUnit.SECONDS), "The caller should be waiting for the EDT");
            caller.interrupt();
            caller.join(15_000);
        } finally {
            releaseEdt.countDown();
        }

        assertFalse(caller.isAlive(), "The call should have returned");
        assertInstanceOf(RuntimeException.class, caught.get());
        assertInstanceOf(InterruptedException.class, caught.get().getCause());
        assertTrue(interruptKept.get(), "The caller's interrupt flag must survive the call");
        // Everything queued before this no-op, the cancelled task included, has been dispatched once it returns
        SwingUtilities.invokeAndWait(() -> { });
        assertFalse(ran.get(), "A task whose caller gave up before the EDT reached it must not run");
    }

    // Occupies the EDT until the returned latch is released. Always release it in a finally block, or later tests inherit a blocked EDT.
    private static CountDownLatch blockEdt() throws InterruptedException {
        CountDownLatch blocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            blocked.countDown();
            try {
                release.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        if (!blocked.await(10, TimeUnit.SECONDS)) {
            // Released before failing: the blocker is still queued, and once it runs nobody else could let it go
            release.countDown();
            fail("The EDT should have been blocked");
        }
        return release;
    }

    private static boolean awaitState(Thread thread, Thread.State state, long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            if (thread.getState() == state) {
                return true;
            }
            Thread.sleep(10);
        }
        return false;
    }

    private static boolean awaitNoEventDispatchThread(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            boolean edtAlive = Thread.getAllStackTraces().keySet().stream()
                    .anyMatch(thread -> thread.getName().startsWith("AWT-EventQueue") && thread.isAlive());
            if (!edtAlive) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }
}
