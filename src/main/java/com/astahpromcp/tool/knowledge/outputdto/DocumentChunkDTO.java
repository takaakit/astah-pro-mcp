package com.astahpromcp.tool.knowledge.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DocumentChunkDTO(
    @JsonPropertyDescription("The document content of this chunk. If no chunk data exists, an empty string is set.")
    String content
) {
}
