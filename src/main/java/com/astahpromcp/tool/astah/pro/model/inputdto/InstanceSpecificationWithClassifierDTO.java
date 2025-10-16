package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record InstanceSpecificationWithClassifierDTO(
    @JsonPropertyDescription("Target classifier identifier")
    String targetClassifierId,

    @JsonPropertyDescription("Target instance specification identifier")
    String targetInstanceSpecificationId
) {
}
