package com.astahpromcp.tool.config.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record WorkspaceDirectoryPathDTO(
    @JsonPropertyDescription("Workspace directory path")
    String workspaceDirectoryPath
) {
}
