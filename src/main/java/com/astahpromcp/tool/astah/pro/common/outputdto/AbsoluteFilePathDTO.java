package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AbsoluteFilePathDTO(
    @JsonPropertyDescription("Absolute file path")
    String absoluteFilePath
) {
}
