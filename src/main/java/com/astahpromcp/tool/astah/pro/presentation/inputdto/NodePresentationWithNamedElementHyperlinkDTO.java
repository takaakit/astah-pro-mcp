package com.astahpromcp.tool.astah.pro.presentation.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NodePresentationWithNamedElementHyperlinkDTO(
        @JsonPropertyDescription("Target node presentation identifier")
        String targetNodePresentationId,

        @JsonPropertyDescription("Named element identifier to link to")
        String namedElementIdToLink,

        @JsonPropertyDescription("Comment for the hyperlink")
        String hyperlinkComment
) {
}
