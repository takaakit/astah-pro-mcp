package com.astahpromcp.tool.knowledge.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DocumentDTO(
    @JsonPropertyDescription("The total number of chunks.")
    int totalChunks,
    
    @JsonPropertyDescription("The first chunk of the document text.")
    String firstChunk
) {
}
