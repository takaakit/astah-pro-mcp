package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithNamedElementHyperlinkDTO(
        @JsonPropertyDescription("Target named element identifier")
        String targetNamedElementId,

        @JsonPropertyDescription("Named element identifier to link to")
        String namedElementIdToLink,

        @JsonPropertyDescription("Comment for the hyperlink")
        String hyperlinkComment
) {
}
