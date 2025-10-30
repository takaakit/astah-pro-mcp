package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record InteractionUseDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Argument")
    String argument,

    @JsonPropertyDescription("Sequence diagram refers to")
    NameIdTypeDTO sequenceDiagram,

    @JsonPropertyDescription("Gates")
    List<NameIdTypeDTO> gates
) {
}
