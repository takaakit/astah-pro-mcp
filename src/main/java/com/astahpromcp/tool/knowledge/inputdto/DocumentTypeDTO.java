package com.astahpromcp.tool.knowledge.inputdto;

import com.astahpromcp.tool.knowledge.document.DocumentType;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DocumentTypeDTO(
    @JsonPropertyDescription("Reference document to read")
    DocumentType documentType
) {
}
