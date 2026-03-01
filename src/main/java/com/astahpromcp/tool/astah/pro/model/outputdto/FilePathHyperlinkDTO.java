package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record FilePathHyperlinkDTO(
    @JsonPropertyDescription("File path: absolute path or relative path.")
    String filePath,

    @JsonPropertyDescription("Hyperlink comment")
    String hyperlinkComment
) {
}
