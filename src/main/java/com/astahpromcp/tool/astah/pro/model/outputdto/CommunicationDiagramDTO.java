package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CommunicationDiagramDTO(
    @JsonPropertyDescription("Diagram information")
    DiagramDTO diagram,

    @JsonPropertyDescription("Interaction of the communication diagram")
    NameIdTypeDTO interaction
) {
}
