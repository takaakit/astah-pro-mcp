package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record OperationDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Is Abstract")
    boolean isAbstract,

    @JsonPropertyDescription("Is Leaf")
    boolean isLeaf,

    @JsonPropertyDescription("Is Static")
    boolean isStatic,

    @JsonPropertyDescription("Parameters")
    List<ParameterDTO> parameters,

    @JsonPropertyDescription("Return type")
    NameIdTypeDTO returnType,

    @JsonPropertyDescription("Return type expression: The string representation of a return type, consisting of the return type name and its modifiers.")
    String returnTypeExpression
) {
}
