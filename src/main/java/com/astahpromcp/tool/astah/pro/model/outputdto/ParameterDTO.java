package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ParameterDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Type")
    NameIdTypeDTO type,

    @JsonPropertyDescription("Type expression: The string representation of a type, consisting of the type name and its modifiers.")
    String typeExpression
) {
}
