package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record StateDTO(
    @JsonPropertyDescription("Vertex information")
    VertexDTO vertex,

    @JsonPropertyDescription("Entry")
    String entry,

    @JsonPropertyDescription("DoActivity")
    String doActivity,

    @JsonPropertyDescription("Exit")
    String exit,

    @JsonPropertyDescription("Is SubmachineState")
    boolean isSubmachineState,

    @JsonPropertyDescription("SubmachineState")
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
