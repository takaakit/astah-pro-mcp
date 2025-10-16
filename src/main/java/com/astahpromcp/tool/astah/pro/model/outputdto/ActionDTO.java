package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ActionDTO(
    @JsonPropertyDescription("Activity node information")
    ActivityNodeDTO activityNode,

    @JsonPropertyDescription("Calling activity")
    NameIdTypeDTO callingActivity,

    @JsonPropertyDescription("Input pins")
    List<NameIdTypeDTO> inputPins,

    @JsonPropertyDescription("Output pins")
    List<NameIdTypeDTO> outputPins,

    @JsonPropertyDescription("Is Accept event action")
    boolean isAcceptEventAction,

    @JsonPropertyDescription("Is Accept time event action")
    boolean isAcceptTimeEventAction,

    @JsonPropertyDescription("Is Call behavior action")
    boolean isCallBehaviorAction,

    @JsonPropertyDescription("Is Connector")
    boolean isConnector,

    @JsonPropertyDescription("Is Process")
    boolean isProcess,

    @JsonPropertyDescription("Is Send signal action")
    boolean isSendSignalAction
) {
}
