package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithFilePathHyperlinkDTO(
        @JsonPropertyDescription("Target named element identifier")
        String targetNamedElementId,

        @JsonPropertyDescription("File path (absolute or relative)")
        String filePath,

        @JsonPropertyDescription("Comment for the hyperlink")
        String hyperlinkComment
) {
}
