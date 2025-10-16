package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record LifelineWithBaseClassDTO(
    @JsonPropertyDescription("Target lifeline identifier")
    String targetLifelineId,

    @JsonPropertyDescription("Base class identifier")
    String baseClassId
) {
}
