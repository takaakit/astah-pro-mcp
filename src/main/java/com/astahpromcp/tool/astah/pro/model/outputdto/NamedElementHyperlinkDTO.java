package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementHyperlinkDTO(
    @JsonPropertyDescription("Named element identifier")
    String namedElementId,

    @JsonPropertyDescription("Hyperlink comment")
    String hyperlinkComment
) {
}
