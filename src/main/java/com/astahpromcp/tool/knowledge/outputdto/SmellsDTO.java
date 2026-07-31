package com.astahpromcp.tool.knowledge.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SmellsDTO(
    @JsonPropertyDescription("Contents of architectural smells and design smells that should be avoided")
    String contents,

    @JsonPropertyDescription("Absolute path of the stored smells file")
    String smellFilePath
) {
}
