package com.astahpromcp.tool.mcptoolscript.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record McpToolScriptExampleDTO(
    @JsonPropertyDescription("Example name of the MCP tool script to return, spelled exactly as the catalogue spells it, such as 'create-sequence-diagram.js'.")
    String exampleName
) {
}
