package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SequenceDiagramDTO(
    @JsonPropertyDescription("Diagram information")
    DiagramDTO diagram,

    @JsonPropertyDescription("Interaction")
    NameIdTypeDTO interaction
) {
}
