package com.astahpromcp.tool.mcptoolscript.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CallableToolNamesDTO(
    @JsonPropertyDescription("Names of the tool functions to describe, spelled exactly as 'get_all_tools_callable_from_mcp_tool_script' spells them. There is a limit on how many names one call accepts, and a large answer may return fewer than you asked for; whatever is left out is named in 'problems' for you to ask for again.")
    List<String> toolNames
) {
}
