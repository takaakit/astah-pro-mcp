package com.astahpromcp.tool.log;

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

// Tool for obtaining the Astah Professional log file information
@Slf4j
public class AstahProLogFileTool implements ToolProvider {

    public AstahProLogFileTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_info_of_astah_log",
                            "Return the log file information of the Astah Professional.",
                            this::getAstahProLogFile,
                            NoInputDTO.class,
                            LogFileDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create astah pro log file tools", e);
            return List.of();
        }
    }

    private LogFileDTO getAstahProLogFile(McpSyncServerExchange exchange, NoInputDTO param) throws IOException {
        log.debug("Get information of astah pro log: {}", param);
        
        String userHome = System.getProperty("user.home");
        String astahProLogFilePath = Paths.get(userHome, ".astah", "professional", "astah.log").toString();
        return getLogFileStats(astahProLogFilePath);
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
