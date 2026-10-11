package com.astahpromcp.tool;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.astahpromcp.tool.ToolCallStatistics.Route.DIRECT;
import static com.astahpromcp.tool.ToolCallStatistics.Route.MCP_TOOL_SCRIPT;
import static org.junit.jupiter.api.Assertions.*;

public class ToolCallStatisticsTest {

    private static final McpSchema.CallToolResult OK = ResponseSupport.success(Map.of("name", "Sample"));

    // The counts are process-wide, so every test starts from none
    @BeforeEach
    void reset() {
        ToolCallStatistics.logAndReset();
    }

    // An error result, a result whose 'ok' is false and a handler that threw (no result) all count as failures
    @Test
    void summaries_ok_countsEachRouteOnALineOfItsOwn() {
        ToolCallStatistics.record(DIRECT, "run_mcp_tool_script", OK);
        ToolCallStatistics.record(DIRECT, "run_mcp_tool_script", ResponseSupport.success(Map.of("ok", false)));
        ToolCallStatistics.record(DIRECT, "open_dgm", ResponseSupport.error("boom"));
        ToolCallStatistics.record(DIRECT, "open_dgm", ResponseSupport.error("boom"));
        ToolCallStatistics.record(DIRECT, "save_proj", null);
        ToolCallStatistics.record(MCP_TOOL_SCRIPT, "get_proj", OK);

        assertEquals(List.of(
                "Tool call statistics of direct calls: 5 calls, 1 succeeded, 4 failed, success rate 20.0%, failed tools (calls/failures): open_dgm 2/2, run_mcp_tool_script 2/1, save_proj 1/1",
                "Tool call statistics of calls from mcp tool scripts: 1 calls, 1 succeeded, 0 failed, success rate 100.0%, failed tools (calls/failures): none"),
                ToolCallStatistics.summaries());
    }

    @Test
    void logAndReset_ok_startsCountingAfresh() {
        ToolCallStatistics.record(DIRECT, "get_proj", OK);

        ToolCallStatistics.logAndReset();

        assertEquals(List.of(
                "Tool call statistics of direct calls: 0 calls, 0 succeeded, 0 failed, success rate n/a, failed tools (calls/failures): none",
                "Tool call statistics of calls from mcp tool scripts: 0 calls, 0 succeeded, 0 failed, success rate n/a, failed tools (calls/failures): none"),
                ToolCallStatistics.summaries());
    }
}
