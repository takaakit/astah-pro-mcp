package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CombinedFragmentDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Combined fragment kind")
    String combinedFragmentKind,

    @JsonPropertyDescription("Interaction operands")
    List<NameIdTypeDTO> interactionOperands
) {
}
