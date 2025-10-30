package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record InteractionOperandDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Guard condition")
    String guard,

    @JsonPropertyDescription("Lifelines")
    List<NameIdTypeDTO> lifelines,

    @JsonPropertyDescription("Messages")
    List<NameIdTypeDTO> messages
) {
}
