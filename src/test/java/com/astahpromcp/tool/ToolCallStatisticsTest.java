package com.astahpromcp.tool;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        
        List<String> summaries = ToolCallStatistics.summaries();
        assertEquals(2, summaries.size(), summaries::toString);
        
        // Direct calls: 5 calls, 1 succeeded, 4 failed, 20.0%, then the calls and failures of each failed tool
        String direct = summaries.get(0);
        assertEquals(List.of(5, 1, 4, 20, 0, 2, 2, 2, 1, 1, 1), numbersIn(direct), direct);
        // The failed tools, most failures first
        assertTrue(direct.contains("open_dgm"), direct);
        assertTrue(direct.indexOf("open_dgm") < direct.indexOf("run_mcp_tool_script"), direct);
        assertTrue(direct.indexOf("run_mcp_tool_script") < direct.indexOf("save_proj"), direct);
        
        // Calls from mcp tool scripts: 1 call, 1 succeeded, 0 failed, 100.0%, and no failed tool
        String fromScripts = summaries.get(1);
        assertEquals(List.of(1, 1, 0, 100, 0), numbersIn(fromScripts), fromScripts);
        assertFalse(fromScripts.contains("get_proj"), fromScripts);
    }
    
    @Test
    void logAndReset_ok_startsCountingAfresh() {
        ToolCallStatistics.record(DIRECT, "get_proj", OK);
        
        ToolCallStatistics.logAndReset();
        
        // Every route has 0 calls, 0 succeeded and 0 failed, no success rate and no failed tool
        List<String> summaries = ToolCallStatistics.summaries();
        assertEquals(2, summaries.size(), summaries::toString);
        for (String summary : summaries) {
            assertEquals(List.of(0, 0, 0), numbersIn(summary), summary);
            assertFalse(summary.contains("get_proj"), summary);
        }
    }
    
    private static List<Integer> numbersIn(String text) {
        return Pattern.compile("\\d+").matcher(text).results()
                .map(match -> Integer.parseInt(match.group()))
                .toList();
    }
}
