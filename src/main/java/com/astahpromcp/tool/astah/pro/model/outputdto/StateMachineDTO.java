package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record StateMachineDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("StateMachine diagram")
    NameIdTypeDTO stateMachineDiagram,

    @JsonPropertyDescription("States")
    List<NameIdTypeDTO> states,

    @JsonPropertyDescription("Transitions")
    List<NameIdTypeDTO> transitions,

    @JsonPropertyDescription("Vertexes")
    List<NameIdTypeDTO> vertexes
) {
}
