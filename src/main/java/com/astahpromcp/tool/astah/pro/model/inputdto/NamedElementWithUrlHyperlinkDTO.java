package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithUrlHyperlinkDTO(
        @JsonPropertyDescription("Target named element identifier")
        String targetNamedElementId,

        @JsonPropertyDescription("URL: must start with \"https://\".")
        String url,

        @JsonPropertyDescription("Comment for the hyperlink")
        String hyperlinkComment
) {
}
