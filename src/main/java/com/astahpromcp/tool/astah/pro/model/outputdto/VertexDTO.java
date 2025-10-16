package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record VertexDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Incoming transitions")
    List<NameIdTypeDTO> incomingTransitions,

    @JsonPropertyDescription("Outgoing transitions")
    List<NameIdTypeDTO> outgoingTransitions
) {
}
