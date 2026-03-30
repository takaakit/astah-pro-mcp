package com.astahpromcp.tool.astah.pro.common.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record FilePathDTO(
    @JsonPropertyDescription("File path: absolute path or relative path.")
    String filePath
) {
}
