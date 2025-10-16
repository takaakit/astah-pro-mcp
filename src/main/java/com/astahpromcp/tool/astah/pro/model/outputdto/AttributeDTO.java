package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AttributeDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Is Aggregate")
    boolean isAggregate,

    @JsonPropertyDescription("Is Changeable")
    boolean isChangeable,

    @JsonPropertyDescription("Is Composite")
    boolean isComposite,

    @JsonPropertyDescription("Is Derived")
    boolean isDerived,

    @JsonPropertyDescription("Is Enable")
    boolean isEnable,

    @JsonPropertyDescription("Is Static")
    boolean isStatic,

    @JsonPropertyDescription("Is initial value")
    String initialValue,

    @JsonPropertyDescription("Upper Multiplicity. The value '-1' represents '*', and the value '-100' represents undefined.")
    int upperMultiplicity,

    @JsonPropertyDescription("Lower Multiplicity. The value '-1' represents '*', and the value '-100' represents undefined.")
    int lowerMultiplicity,

    @JsonPropertyDescription("Navigability")
    String navigability,

    @JsonPropertyDescription("Type")
    NameIdTypeDTO type,

    @JsonPropertyDescription("Type Expression: The string representation of a type, consisting of the type name and its modifiers.")
    String typeExpression
) {
}
