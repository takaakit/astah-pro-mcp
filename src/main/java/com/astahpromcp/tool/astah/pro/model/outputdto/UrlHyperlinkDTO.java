package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record UrlHyperlinkDTO(
    @JsonPropertyDescription("URL")
    String url,

    @JsonPropertyDescription("Hyperlink comment")
    String hyperlinkComment
) {
}
