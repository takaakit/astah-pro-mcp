package com.astahpromcp.tool.log.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LogFileDTO(
    @JsonPropertyDescription("Log file path")
    String logFilePath,
    
    @JsonPropertyDescription("Number of lines in the log file")
    long lineCount,
    
    @JsonPropertyDescription("File size in bytes")
    long fileSize
) {
}
