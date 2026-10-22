package com.astahpromcp.tool.astah.pro.project.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiffProjectsDTO(
    @JsonPropertyDescription("Absolute path of the Astah project file (.asta) to compare from")
    String baseProjectPath,

    @JsonPropertyDescription("Absolute path of the Astah project file (.asta) to compare to; empty for the project open in Astah")
    String targetProjectPath,

    @JsonPropertyDescription("Unique ID of this request, such as a UUID; calling again with the same ID returns the comparison already started")
    String requestId,

    @JsonPropertyDescription("True if you have called the project diff guide tool and still have its contents")
    boolean isProjectDiffGuideCalled
) {
}
