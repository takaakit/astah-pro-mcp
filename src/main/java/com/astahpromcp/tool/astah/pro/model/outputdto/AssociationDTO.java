package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record AssociationDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Association end A")
    NameIdTypeDTO associationEndA,

    @JsonPropertyDescription("Association end B")
    NameIdTypeDTO associationEndB
) {
}
