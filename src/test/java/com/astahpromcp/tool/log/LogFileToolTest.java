package com.astahpromcp.tool.log;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.log.outputdto.LogFileDTO;
import com.astahpromcp.tool.log.outputdto.LogFilesDTO;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LogFileToolTest {

    private static final String TOOL_NAME = "get_info_of_logs";

    @TempDir
    Path dir;

    private static LogFilesDTO getLogFiles(Path astahLog, Path mcpServerLog) {
        List<ToolDefinition> definitions = new LogFileTool(() -> astahLog, () -> mcpServerLog).createToolDefinitions();
        assertEquals(1, definitions.size());
        ToolDefinition definition = definitions.get(0);
        assertEquals(TOOL_NAME, definition.toolSchema().name());

        McpSchema.CallToolResult result = definition.toolHandler().apply(null, new McpSchema.CallToolRequest(TOOL_NAME, Map.of(), null));
        assertNotEquals(Boolean.TRUE, result.isError(), "a log that cannot be read must not fail the tool");
        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), LogFilesDTO.class);
    }

    private static void assertRead(Path expectedPath, long expectedLineCount, LogFileDTO actual) throws Exception {
        assertEquals(expectedPath.toString(), actual.logFilePath());
        assertTrue(actual.ok());
        assertEquals(expectedLineCount, actual.lineCount());
        assertEquals(Files.size(expectedPath), actual.fileSize());
        assertEquals("", actual.errorMessage());
    }

    private static void assertUnread(String expectedPath, String expectedMessagePrefix, LogFileDTO actual) {
        assertEquals(expectedPath, actual.logFilePath());
        assertFalse(actual.ok());
        assertEquals(-1L, actual.lineCount());
        assertEquals(-1L, actual.fileSize());
        assertTrue(actual.errorMessage().startsWith(expectedMessagePrefix), actual.errorMessage());
    }

    @Test
    void getLogFiles_ok_reportsBothLogs() throws Exception {
        Path astahLog = Files.writeString(dir.resolve("astah.log"), "a\nb\nc\n");
        Path mcpServerLog = Files.writeString(dir.resolve("astah-pro-mcp.log"), "x\ny\n");

        LogFilesDTO logFiles = getLogFiles(astahLog, mcpServerLog);

        assertRead(astahLog, 3, logFiles.astahLog());
        assertRead(mcpServerLog, 2, logFiles.mcpServerLog());
    }

    @Test
    void getLogFiles_ng_missingLogDoesNotHideTheOther() throws Exception {
        Path astahLog = dir.resolve("astah.log");
        Path mcpServerLog = Files.writeString(dir.resolve("astah-pro-mcp.log"), "x\n");

        LogFilesDTO logFiles = getLogFiles(astahLog, mcpServerLog);

        assertUnread(astahLog.toString(), "Log file not found: " + astahLog, logFiles.astahLog());
        assertRead(mcpServerLog, 1, logFiles.mcpServerLog());
    }

    @Test
    void getLogFiles_ng_pathNotSet() throws Exception {
        Path astahLog = Files.writeString(dir.resolve("astah.log"), "a\n");

        LogFilesDTO logFiles = getLogFiles(astahLog, null);

        assertRead(astahLog, 1, logFiles.astahLog());
        assertUnread("", "The log file path is not set.", logFiles.mcpServerLog());
    }

    @Test
    void getLogFiles_ng_logThatIsNotUtf8DoesNotHideTheOther() throws Exception {
        Path astahLog = Files.write(dir.resolve("astah.log"), new byte[] {'a', '\n', (byte) 0xFF, (byte) 0xFE, '\n'});
        Path mcpServerLog = Files.writeString(dir.resolve("astah-pro-mcp.log"), "x\n");

        LogFilesDTO logFiles = getLogFiles(astahLog, mcpServerLog);

        assertUnread(astahLog.toString(), "Could not read log file: " + astahLog, logFiles.astahLog());
        assertRead(mcpServerLog, 1, logFiles.mcpServerLog());
    }
}
