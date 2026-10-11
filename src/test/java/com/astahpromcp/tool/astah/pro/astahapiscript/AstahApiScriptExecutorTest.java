package com.astahpromcp.tool.astah.pro.astahapiscript;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.AstahApiLock;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class AstahApiScriptExecutorTest {

    // An astah api script reaches the whole JVM, so the script below signals through these instead of a sleep.
    public static final CountDownLatch SCRIPT_RUNNING = new CountDownLatch(1);
    public static final CountDownLatch RELEASE_SCRIPT = new CountDownLatch(1);
    public static final CountDownLatch RELEASE_TIMED_OUT_SCRIPT = new CountDownLatch(1);

    private ProjectAccessor projectAccessor;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.create();
    }

    @AfterEach
    void tearDown() throws Exception {
        AstahApiLock.clearSuspension();
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void execute_ng_timesOutAndDoesNotLeaveAccessSuspendedWhenTheScriptStops() throws Exception {
        // A 1 second budget against a script that blocks far longer, so the timeout path is taken
        AstahApiScriptExecutor executor = new AstahApiScriptExecutor(projectAccessor, 1);

        AstahApiScriptExecutor.Result result = executor.execute("java.lang.Thread.sleep(30000);");

        assertFalse(result.ok(), "A timed-out script must be reported as a failure");
        assertTrue(result.errorMessage().contains("timed out"),
                "Error message should tell the caller that the script timed out: " + result.errorMessage());

        // Thread.sleep responds to the interrupt, so the runner terminates and the suspension, which is derived from that thread's liveness, must lift by itself.
        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS),
                "Astah API access should be restored once the interrupted script thread terminates");
    }

    // The suspension reason is what the next tool call is refused with, so its wording is part of what the client sees.
    // It is read while the script is still running: the reason lifts as soon as the abandoned thread terminates.
    @Test
    void execute_ng_suspendsAccessWithTheTimeoutAsTheReason() throws Exception {
        AstahApiScriptExecutor executor = new AstahApiScriptExecutor(projectAccessor, 1);

        // Keeps running through its interrupt, so that the runner outlives the timeout.
        String script = """
                var test = Java.type('com.astahpromcp.tool.astah.pro.astahapiscript.AstahApiScriptExecutorTest');
                var millis = java.util.concurrent.TimeUnit.MILLISECONDS;
                var deadline = java.lang.System.nanoTime() + 30000000000;
                var released = false;
                while (!released && java.lang.System.nanoTime() < deadline) {
                    try { released = test.RELEASE_TIMED_OUT_SCRIPT.await(50, millis); } catch (e) { }
                }
                """;

        try {
            AstahApiScriptExecutor.Result result = executor.execute(script);

            assertFalse(result.ok(), "A timed-out script must be reported as a failure");
            assertTrue(result.errorMessage().startsWith("Astah API script execution timed out after 1 seconds."),
                    result.errorMessage());
            String reason = AstahApiLock.suspensionReason();
            assertNotNull(reason, "The script is still running, so access must be suspended");
            assertTrue(reason.startsWith("an astah api script that timed out after 1 seconds is still running. "), reason);

        } finally {
            RELEASE_TIMED_OUT_SCRIPT.countDown();
        }
        assertTrue(awaitAccessRestored(30, TimeUnit.SECONDS),
                "Astah API access should be restored once the abandoned runner terminates");
    }

    // The wait can be cut short by the caller as well as by the timeout: a cancelled or disconnected request interrupts
    // the thread waiting for the run. That interrupt does not stop the runner, so the same protection has to be in place,
    // and it has to be in place before this call returns, while it still holds the Astah API lock.
    @Test
    void execute_ng_suspendsAccessWhenTheWaitIsInterrupted() throws Exception {
        AstahApiScriptExecutor executor = new AstahApiScriptExecutor(projectAccessor, 60);

        // Keeps running through its interrupt, so that the runner is still alive while the assertions run.
        String script = """
                var test = Java.type('com.astahpromcp.tool.astah.pro.astahapiscript.AstahApiScriptExecutorTest');
                var millis = java.util.concurrent.TimeUnit.MILLISECONDS;
                test.SCRIPT_RUNNING.countDown();
                var deadline = java.lang.System.nanoTime() + 30000000000;
                var released = false;
                while (!released && java.lang.System.nanoTime() < deadline) {
                    try { released = test.RELEASE_SCRIPT.await(50, millis); } catch (e) { }
                }
                """;

        AtomicReference<AstahApiScriptExecutor.Result> result = new AtomicReference<>();
        AtomicBoolean interruptFlagKept = new AtomicBoolean();
        Thread caller = new Thread(() -> {
            result.set(executor.execute(script));
            interruptFlagKept.set(Thread.currentThread().isInterrupted());
        }, "interrupted-caller");
        caller.setDaemon(true);
        caller.start();

        assertTrue(SCRIPT_RUNNING.await(30, TimeUnit.SECONDS), "The script should have started");
        caller.interrupt();
        caller.join(10_000);

        assertFalse(caller.isAlive(), "The interrupted caller should have stopped waiting");
        assertFalse(result.get().ok(), "An interrupted wait must be reported as a failure");
        assertTrue(result.get().errorMessage().startsWith("Interrupted while waiting for astah api script execution."),
                result.get().errorMessage());
        String reason = AstahApiLock.suspensionReason();
        assertNotNull(reason, "The abandoned runner may still be calling the Astah API, so access must be suspended");
        // What the next tool call is refused with, so its wording is part of what the client sees
        assertTrue(reason.startsWith("an astah api script whose caller stopped waiting is still running. "), reason);
        assertTrue(interruptFlagKept.get(), "The caller's interrupt flag must be restored");

        RELEASE_SCRIPT.countDown();
        assertTrue(awaitAccessRestored(30, TimeUnit.SECONDS),
                "Astah API access should be restored once the abandoned runner terminates");
    }

    @Test
    void execute_ok_leavesAccessUsable() throws Exception {
        AstahApiScriptExecutor executor = new AstahApiScriptExecutor(projectAccessor, 30);

        AstahApiScriptExecutor.Result result = executor.execute("1 + 1;");

        assertTrue(result.ok(), "A well-behaved script should succeed: " + result.errorMessage());
        assertNull(AstahApiLock.suspensionReason(), "A successful script must not suspend Astah API access");
    }

    @Test
    void execute_ok_truncatesRunawayOutputAndSaysSo() {
        AstahApiScriptExecutor executor = new AstahApiScriptExecutor(projectAccessor, 30);

        AstahApiScriptExecutor.Result result = executor.execute(
                "for (var i = 0; i < 20000; i++) { print('0123456789012345678901234567890123456789'); } 'done';");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals("done", result.value());
        assertTrue(result.output().getBytes(StandardCharsets.UTF_8).length
                        <= McpServerConfig.ASTAH_API_SCRIPT_MAX_STDOUT_BYTES,
                "Output must stay within its limit");
        assertTrue(result.errorOutput().contains("truncated"),
                "A truncated output must say so: " + result.errorOutput());
    }

    @Test
    void execute_ng_stopsAPrintLoopOnceItTimesOut() throws Exception {
        AstahApiScriptExecutor executor = new AstahApiScriptExecutor(projectAccessor, 1);

        AstahApiScriptExecutor.Result result = executor.execute("while (true) { print('x'); }");

        assertFalse(result.ok(), "A timed-out script must be reported as a failure");
        assertTrue(result.errorMessage().contains("timed out"), result.errorMessage());
        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS),
                "The print loop should stop at its next print once it is interrupted");
    }

    @Test
    void execute_ng_abortsTheTransactionOfAStoppedPrintLoopWithoutFailing() throws Exception {
        AstahApiScriptExecutor executor = new AstahApiScriptExecutor(projectAccessor, 1);
        Logger logger = (Logger) LoggerFactory.getLogger(AstahApiScriptExecutor.class);
        ListAppender<ILoggingEvent> captured = new ListAppender<>();
        captured.start();
        logger.addAppender(captured);
        try {
            AstahApiScriptExecutor.Result result = executor.execute("""
                    Java.type('com.change_vision.jude.api.inf.editor.TransactionManager').beginTransaction();
                    while (true) { print('x'); }
                    """);

            assertFalse(result.ok());
            assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS),
                    "The print loop should stop at its next print once it is interrupted");
            assertFalse(TransactionManager.isInTransaction(), "The transaction the stopped script left open must be aborted");
            assertTrue(captured.list.stream().noneMatch(event -> event.getFormattedMessage().startsWith("Failed to abort")),
                    "Aborting the transaction must not be reported as failed: " + captured.list);

        } finally {
            logger.detachAppender(captured);
            captured.stop();
        }
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
