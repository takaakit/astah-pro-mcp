package com.astahpromcp.tool.log.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LogFileDTO(
    @JsonPropertyDescription("Log file path")
    String logFilePath,

    @JsonPropertyDescription("True if the log file was read")
    boolean ok,

    @JsonPropertyDescription("Number of lines in the log file, or -1 when 'ok' is false")
    long lineCount,

    @JsonPropertyDescription("File size in bytes, or -1 when 'ok' is false")
    long fileSize,

    @JsonPropertyDescription("Why the log file could not be read when 'ok' is false; empty otherwise")
    String errorMessage
) {
    public static LogFileDTO read(String logFilePath, long lineCount, long fileSize) {
        return new LogFileDTO(logFilePath, true, lineCount, fileSize, "");
    }

    public static LogFileDTO unread(String logFilePath, String errorMessage) {
        return new LogFileDTO(logFilePath, false, -1L, -1L, errorMessage);
    }
}
