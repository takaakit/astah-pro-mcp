package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record StateMachineDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("StateMachine diagram")
    NameIdTypeDTO stateMachineDiagram,

    @JsonPropertyDescription("Submachine states: States that reference this state machine as their submachine. This is not a list of the states in this state machine; to list those, start from 'vertexes'.")
    List<NameIdTypeDTO> states,

    @JsonPropertyDescription("Transitions in this state machine, including those between vertexes nested in composite states. Internal transitions are not included.")
    List<NameIdTypeDTO> transitions,

    @JsonPropertyDescription("Vertexes (states and pseudostates) located directly in this state machine. Vertexes nested in a composite state are not included.")
    List<NameIdTypeDTO> vertexes
) {
}
