package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.astahpromcp.tool.astah.pro.common.NavigabilityKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LinkEndWithNavigabilityDTO(
    @JsonPropertyDescription("Target link end identifier")
    String targetLinkEndId,

    @JsonPropertyDescription("Navigability kind")
    NavigabilityKind navigabilityKind
) {
}
