package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CallableToolSummaryDTO(
    @JsonPropertyDescription("Name of the tool function, to be called from 'run_mcp_tool_script' as tools.<name>({ ... })")
    String name,

    @JsonPropertyDescription("What the tool function does")
    String description,

    @JsonPropertyDescription("Names of the arguments the tool function takes")
    List<String> arguments
) {
}
