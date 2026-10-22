package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ReferenceProjectDTO(
    @JsonPropertyDescription("Path of the referenced project file")
    String path,

    @JsonPropertyDescription("latest, need_update, not_found, or unknown")
    ReferenceProjectStatus status
) {
}
