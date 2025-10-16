package com.astahpromcp.tool.log.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LogDirectoryDTO(
    @JsonPropertyDescription("Log directory path")
    String logDirectoryPath
) {
}
