package com.astahpromcp.tool.knowledge.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DocumentChunkDTO(
    @JsonPropertyDescription("The document contents of this chunk.")
    String contents
) {
}
