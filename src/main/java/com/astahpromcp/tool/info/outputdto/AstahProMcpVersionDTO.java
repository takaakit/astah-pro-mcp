package com.astahpromcp.tool.info.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AstahProMcpVersionDTO(
    @JsonPropertyDescription("Astah Pro MCP server version")
    String version
) {
}
