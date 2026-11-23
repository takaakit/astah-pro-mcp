package com.astahpromcp.tool.scenario.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SetAsideDirectoryDTO(
    @JsonPropertyDescription("Absolute path of the working directory")
    String directoryPath,

    @JsonPropertyDescription("Whether the working directory was set aside; false when it did not exist")
    boolean setAside,

    @JsonPropertyDescription("Absolute path the working directory was renamed to; empty when it did not exist")
    String keptDirectoryPath
) {
}
