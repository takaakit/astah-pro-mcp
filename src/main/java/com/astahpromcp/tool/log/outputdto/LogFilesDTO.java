package com.astahpromcp.tool.log.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LogFilesDTO(
    @JsonPropertyDescription("Log file of Astah Professional")
    LogFileDTO astahLog,

    @JsonPropertyDescription("Log file of Astah Pro MCP server")
    LogFileDTO mcpServerLog
) {
}
