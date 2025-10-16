package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record MessageDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Argument")
    String argument,

    @JsonPropertyDescription("Guard condition")
    String guard,

    @JsonPropertyDescription("Return value")
    String returnValue,

    @JsonPropertyDescription("Return value variable")
    String returnValueVariable,

    @JsonPropertyDescription("Is Asynchronous")
    boolean isAsynchronous,

    @JsonPropertyDescription("Is ReturnMessage")
    boolean isReturnMessage,

    @JsonPropertyDescription("Is Synchronous")
    boolean isSynchronous,

    @JsonPropertyDescription("Index")
    String index,

    @JsonPropertyDescription("Activator")
    NameIdTypeDTO activator,

    @JsonPropertyDescription("Successor")
    NameIdTypeDTO successor,

    @JsonPropertyDescription("Source")
    NameIdTypeDTO source,

    @JsonPropertyDescription("Target")
    NameIdTypeDTO target,

    @JsonPropertyDescription("Operation")
    NameIdTypeDTO operation
) {
}
