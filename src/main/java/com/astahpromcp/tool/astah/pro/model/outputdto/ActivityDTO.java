package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ActivityDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Activity diagram")
    NameIdTypeDTO activityDiagram,

    @JsonPropertyDescription("Activity nodes")
    List<NameIdTypeDTO> activityNodes,

    @JsonPropertyDescription("Call behavior actions")
    List<NameIdTypeDTO> callBehaviorActions,

    @JsonPropertyDescription("Control flows and object flows")
    List<NameIdTypeDTO> flows,

    @JsonPropertyDescription("Partitions")
    List<NameIdTypeDTO> partitions
) {
}
