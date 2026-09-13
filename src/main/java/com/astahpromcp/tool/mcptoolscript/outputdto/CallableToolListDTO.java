package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CallableToolListDTO(
    @JsonPropertyDescription("Number of tool functions returned, which is every tool function an mcp tool script can call.")
    int total,

    @JsonPropertyDescription("Every tool function an mcp tool script can call, grouped by category and sorted by name within each category. The tool functions this server publishes directly are NOT here: they are already in your tool list and are called directly as MCP tools.")
    List<CallableToolSummaryDTO> tools
) {
}
