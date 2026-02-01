package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERSubtypeRelationshipWithDiscriminatorAttrDTO(
    @JsonPropertyDescription("Target ER subtype relationship identifier")
    String targetERSubtypeRelationshipId,

    @JsonPropertyDescription("ER discriminator attribute identifier")
    String erDiscriminatorAttributeId
) {
}
