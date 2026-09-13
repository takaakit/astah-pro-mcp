package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CallableToolSummaryDTO(
    @JsonPropertyDescription("Name of the tool function, to be called from 'run_mcp_tool_script' as tools.<name>({ ... })")
    String name,

    @JsonPropertyDescription("What the tool function does. This says what it is for, not what arguments it takes; call 'get_info_of_tools_callable_from_mcp_tool_script' for those.")
    String description
) {
}
