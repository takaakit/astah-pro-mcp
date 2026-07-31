package com.astahpromcp.tool.astah.pro.script;

import com.astahpromcp.tool.astah.pro.AstahApiLock;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AstahScriptExecutorTest {

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
        AstahScriptExecutor executor = new AstahScriptExecutor(projectAccessor, 1);

        AstahScriptExecutor.Result result = executor.execute("java.lang.Thread.sleep(30000);");

        assertFalse(result.ok(), "A timed-out script must be reported as a failure");
        assertTrue(result.errorMessage().contains("timed out"),
                "Error message should tell the caller that the script timed out: " + result.errorMessage());

        // Thread.sleep responds to the interrupt, so the runner terminates and the suspension, which is derived from that thread's liveness, must lift by itself.
        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS),
                "Astah API access should be restored once the interrupted script thread terminates");
    }

    @Test
    void execute_ok_leavesAccessUsable() throws Exception {
        AstahScriptExecutor executor = new AstahScriptExecutor(projectAccessor, 30);

        AstahScriptExecutor.Result result = executor.execute("1 + 1;");

        assertTrue(result.ok(), "A well-behaved script should succeed: " + result.errorMessage());
        assertNull(AstahApiLock.suspensionReason(), "A successful script must not suspend Astah API access");
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
