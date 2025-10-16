package com.astahpromcp.tool.log;

import com.astahpromcp.config.LogbackConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.log.outputdto.LogFileDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

// Tool for obtaining the MCP server log file information
@Slf4j
public class McpServerLogFileTool implements ToolProvider {

    public McpServerLogFileTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_info_of_mcp_server_log",
                            "Return the log file information of the MCP server (astah-pro-mcp).",
                            this::getMcpServerLogFile,
                            NoInputDTO.class,
                            LogFileDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create mcp server log file tools", e);
            return List.of();
        }
    }

    private LogFileDTO getMcpServerLogFile(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get information of mcp server log: {}", param);
        
        String mcpServerLogFilePath = LogbackConfig.getLogFilePath().toString();
        return getLogFileStats(mcpServerLogFilePath);
    }

    private LogFileDTO getLogFileStats(String logFilePath) throws IOException {
        Path filePath = Paths.get(logFilePath);

        if (!Files.exists(filePath)) {
            log.warn("Log file not found: {}", logFilePath);
            throw new IOException("Log file not found: " + logFilePath);
        }

        long lineCount = 0L;
        long fileSize = 0L;

        try {
            fileSize = Files.size(filePath);
            try (Stream<String> lines = Files.lines(filePath)) {
                lineCount = lines.count();
            }
        } catch (IOException e) {
            log.warn("Could not read file stats for {}: {}", logFilePath, e.getMessage());
        }

        return new LogFileDTO(logFilePath, lineCount, fileSize);
    }
}
