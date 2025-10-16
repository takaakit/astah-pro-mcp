package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ObjectNodeWithBaseDTO(
    @JsonPropertyDescription("Target object node identifier")
    String targetObjectNodeId,

    @JsonPropertyDescription("Base class identifier")
    String baseClassId
) {
}
