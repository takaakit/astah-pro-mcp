package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.projectdiff.outputdto.DiffStatus;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiffStartDTO(
    @JsonPropertyDescription("Comparison ID to pass when you get the result")
    String diffId,

    @JsonPropertyDescription("running, done, or failed")
    DiffStatus status,

    @JsonPropertyDescription("Project compared from")
    ProjectSideDTO base,

    @JsonPropertyDescription("Project compared to")
    ProjectSideDTO target
) {
}
