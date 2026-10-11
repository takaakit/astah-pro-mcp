package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithFilePathHyperlinkDTO(
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId,

    @JsonPropertyDescription("File path: an absolute path, or a path relative to the folder of the Astah project file (.asta).")
    String filePath,

    @JsonPropertyDescription("Comment for the hyperlink")
    String hyperlinkComment
) {
}
