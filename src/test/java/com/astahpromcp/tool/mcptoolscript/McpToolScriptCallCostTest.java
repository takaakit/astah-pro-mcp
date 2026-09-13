package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.astah.pro.AstahApiLock;
import com.astahpromcp.tool.astah.pro.FakeTransactionBoundary;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// Measures and records what a run of many tool calls costs, so that the script timeout is chosen from a number
// rather than from a guess.
//
// This runs outside the Astah GUI, so it covers the dispatch path itself: parsing arguments, calling the handler,
// draining the EDT, serializing the result. It does NOT cover the editing tools that capture an image of the diagram
// after every change, which are only meaningful inside the running Astah and are the likeliest thing to dominate a
// real script's duration. Read the figure below as a floor, not as the expected cost of an editing script.
@Slf4j
public class McpToolScriptCallCostTest {

    @TempDir
    Path workspaceDir;

    private ProjectAccessor projectAccessor;

    @BeforeEach
    void setUp() throws Exception {
        // A project of this test's own, so the measurement never touches a project the user has open
        projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
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
    void execute_ok_recordsTheCostOfManyToolCallsInOneRun() {
        ToolCatalog catalog = ToolCatalog.build(DiagramThumbnails.OMIT, workspaceDir.resolve("images"), workspaceDir);
        // Nothing is passed as published directly, so every tool the direct profile has stays script callable here.
        // The programmatic profile withholds 'get_astah_pro_mcp_version' from a script because calling it once is all
        // anyone ever does, but that is exactly what makes it the right handler to measure the dispatch path with:
        // it needs no project, no GUI and no arguments, so what the figure below reports is the path itself.
        AstahToolRegistry registry = AstahToolRegistry.from(
                catalog, ToolManifest.load().namesFor(ToolManifest.Profile.DIRECT), Set.of());

        McpToolScriptExecutor executor = new McpToolScriptExecutor(registry, 60, new FakeTransactionBoundary());

        assertNotNull(registry.find("get_astah_pro_mcp_version"),
                "The benchmark needs a tool function that works outside the Astah GUI");

        int callCount = McpServerConfig.MCP_TOOL_SCRIPT_MAX_CALLS;
        String script = "var n = 0;"
                + " for (var i = 0; i < " + callCount + "; i++) { n += tools.get_astah_pro_mcp_version({}).version.length; }"
                + " '' + n;";

        // Warm up, so that the figure reports steady-state cost rather than one-off class loading
        assertTrue(executor.execute(script).ok());

        long startNanos = System.nanoTime();
        McpToolScriptExecutor.Result result = executor.execute(script);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        assertTrue(result.ok(), result.errorMessage());

        log.info("MCP tool script call cost: {} query tool calls in one run took {} ms ({} ms per call, engine setup included)",
                callCount, elapsedMillis, String.format("%.2f", elapsedMillis / (double) callCount));

        assertTrue(elapsedMillis < TimeUnit.SECONDS.toMillis(McpServerConfig.MCP_TOOL_SCRIPT_TIMEOUT_SECONDS),
                "A run of " + callCount + " query tool calls took " + elapsedMillis
                        + " ms, which is at or past the script timeout of "
                        + McpServerConfig.MCP_TOOL_SCRIPT_TIMEOUT_SECONDS + " seconds. The call limit, the timeout, or the per-call cost, needs revisiting.");
    }
}
