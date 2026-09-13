package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CallableToolInfoDTO(
    @JsonPropertyDescription("Name of the tool function")
    String name,

    @JsonPropertyDescription("What the tool function does")
    String description,

    @JsonPropertyDescription("JSON schema of the arguments, which is the object to pass as tools.<name>({ ... })")
    String inputSchema,

    @JsonPropertyDescription("JSON schema of the structured result the tool function returns; empty for the tool functions that do not declare one")
    String outputSchema
) {
}
