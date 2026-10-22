package com.astahpromcp.tool.log;

import com.astahpromcp.config.LogbackConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.log.outputdto.LogFileDTO;
import com.astahpromcp.tool.log.outputdto.LogFilesDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class LogFileTool implements ToolProvider {

    private final Supplier<Path> astahLogPath;
    private final Supplier<Path> mcpServerLogPath;

    public LogFileTool() {
        this(() -> Paths.get(System.getProperty("user.home"), ".astah", "professional", "astah.log"),
             LogbackConfig::getLogFilePath);
    }

    LogFileTool(Supplier<Path> astahLogPath, Supplier<Path> mcpServerLogPath) {
        this.astahLogPath = astahLogPath;
        this.mcpServerLogPath = mcpServerLogPath;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "get_info_of_logs",
                    "Return the log file information (path, line count, size) of both Astah Professional and Astah Pro MCP server.",
                    this::getLogFiles,
                    NoInputDTO.class,
                    LogFilesDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create log file tools", e);
            return List.of();
        }
    }

    private LogFilesDTO getLogFiles(NoInputDTO param) {
        return new LogFilesDTO(
            getLogFile(astahLogPath.get()),
            getLogFile(mcpServerLogPath.get()));
    }

    private static LogFileDTO getLogFile(Path filePath) {
        if (filePath == null) {
            log.warn("Log file path is not set");
            return LogFileDTO.unread("", "The log file path is not set.");
        }

        String logFilePath = filePath.toString();
        if (!Files.exists(filePath)) {
            log.warn("Log file not found: {}", logFilePath);
            return LogFileDTO.unread(logFilePath, "Log file not found: " + logFilePath);
        }

        try {
            long fileSize = Files.size(filePath);
            long lineCount;
            try (Stream<String> lines = Files.lines(filePath)) {
                lineCount = lines.count();
            }
            return LogFileDTO.read(logFilePath, lineCount, fileSize);

        } catch (IOException | UncheckedIOException e) {
            log.warn("Could not read log file {}: {}", logFilePath, e.getMessage());
            return LogFileDTO.unread(logFilePath, "Could not read log file: " + logFilePath + ". " + e);
        }
    }
}
