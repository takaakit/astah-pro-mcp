package com.astahpromcp.tool.mcptoolscript.inputdto;

import com.astahpromcp.config.McpServerConfig;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CallableToolNamesDTO(
    @JsonPropertyDescription("Names of the tool functions to describe, spelled exactly as the chunks of 'get_chunk_of_tools_callable_from_mcp_tool_script' spell them. One call accepts at most " + McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES + " names, and a large answer may return fewer than you asked for; whatever is left out is named in 'problems' for you to ask for again.")
    List<String> toolNames
) {
}
