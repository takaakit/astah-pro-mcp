package com.astahpromcp.tool.knowledge.inputdto;

import com.astahpromcp.tool.knowledge.document.DocumentType;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DocumentChunkIndexDTO(
    @JsonPropertyDescription("Reference document to read")
    DocumentType documentType,
    
    @JsonPropertyDescription("Index of the chunk to retrieve. The index starts from 0.")
    int chunkIndex
) {
}
