package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record InteractionDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Lifelines")
    List<NameIdTypeDTO> lifelines,

    @JsonPropertyDescription("Messages")
    List<NameIdTypeDTO> messages,

    @JsonPropertyDescription("Gates")
    List<NameIdTypeDTO> gates
) {
}
