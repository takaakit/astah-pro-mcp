package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ProjectPathDTO(
    @JsonPropertyDescription("Full path of the Astah project file (e.g., /path/to/project.asta). Empty string if the project has not been saved.")
    String projectPath
) {
}
