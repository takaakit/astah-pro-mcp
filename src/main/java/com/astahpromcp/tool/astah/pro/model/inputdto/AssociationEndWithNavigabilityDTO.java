package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.astahpromcp.tool.astah.pro.common.NavigabilityKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AssociationEndWithNavigabilityDTO(
    @JsonPropertyDescription("Target association end identifier")
    String targetAssociationEndId,

    @JsonPropertyDescription("Navigability kind")
    NavigabilityKind navigabilityKind
) {
}
