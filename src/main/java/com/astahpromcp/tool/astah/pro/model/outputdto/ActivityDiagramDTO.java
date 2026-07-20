package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ActivityDiagramDTO(
    @JsonPropertyDescription("Diagram info")
    DiagramDTO diagram,

    @JsonPropertyDescription("Activity")
    NameIdTypeDTO activity
) {
}
