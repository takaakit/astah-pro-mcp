package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NodePresentationWithUrlHyperlinkDTO(
        @JsonPropertyDescription("Target node presentation identifier")
        String targetNodePresentationId,

        @JsonPropertyDescription("URL: must start with \"https://\".")
        String url,

        @JsonPropertyDescription("Comment for the hyperlink")
        String hyperlinkComment
) {
}
