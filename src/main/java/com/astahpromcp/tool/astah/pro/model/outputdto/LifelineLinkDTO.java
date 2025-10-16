package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LifelineLinkDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Messages")
    List<NameIdTypeDTO> messages,

    @JsonPropertyDescription("Source lifeline")
    NameIdTypeDTO sourceLifeline,

    @JsonPropertyDescription("Target lifeline")
    NameIdTypeDTO targetLifeline
) {
}
