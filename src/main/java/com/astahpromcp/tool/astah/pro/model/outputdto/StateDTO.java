package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record StateDTO(
    @JsonPropertyDescription("Vertex info")
    VertexDTO vertex,

    @JsonPropertyDescription("Entry")
    String entry,

    @JsonPropertyDescription("DoActivity")
    String doActivity,

    @JsonPropertyDescription("Exit")
    String exit,

    @JsonPropertyDescription("Is SubmachineState")
    boolean isSubmachineState,

    @JsonPropertyDescription("Submachine: The state machine that this state references as a submachine state. Empty if this state is not a submachine state or references no state machine.")
    NameIdTypeDTO submachineState,

    @JsonPropertyDescription("Number of regions")
    int numberOfRegions,

    @JsonPropertyDescription("States of region")
    List<NameIdTypeDTO> regionStates,

    @JsonPropertyDescription("Rectangles of region")
    List<RectangleDTO> regionRectangles,

    @JsonPropertyDescription("Internal Transitions")
    List<NameIdTypeDTO> internalTransitions,

    @JsonPropertyDescription("SubVertexes")
    List<NameIdTypeDTO> subVertexes
) {
}
