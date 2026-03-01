package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NodePresentationWithFilePathHyperlinkDTO(
        @JsonPropertyDescription("Target node presentation identifier")
        String targetNodePresentationId,

        @JsonPropertyDescription("File path (absolute or relative)")
        String filePath,

        @JsonPropertyDescription("Comment for the hyperlink")
        String hyperlinkComment
) {
}
