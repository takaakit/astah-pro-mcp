package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LifelineDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Base class")
    NameIdTypeDTO baseClass,

    @JsonPropertyDescription("Fragments")
    List<NameIdTypeDTO> fragments,

    @JsonPropertyDescription("Lifeline links")
    List<NameIdTypeDTO> lifelineLinks
) {
}
