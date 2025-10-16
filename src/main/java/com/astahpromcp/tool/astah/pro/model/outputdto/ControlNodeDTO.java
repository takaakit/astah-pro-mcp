package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ControlNodeDTO(
    @JsonPropertyDescription("Activity node information")
    ActivityNodeDTO activityNode,

    @JsonPropertyDescription("Is Connector")
    boolean isConnector,

    @JsonPropertyDescription("Is Decision merge node")
    boolean isDecisionMergeNode,

    @JsonPropertyDescription("Is Final node")
    boolean isFinalNode,

    @JsonPropertyDescription("Is Flow final node")
    boolean isFlowFinalNode,

    @JsonPropertyDescription("Is Fork node")
    boolean isForkNode,

    @JsonPropertyDescription("Is Initial node")
    boolean isInitialNode,

    @JsonPropertyDescription("Is Join node")
    boolean isJoinNode,

    @JsonPropertyDescription("Is Merge node")
    boolean isMergeNode
) {
}
