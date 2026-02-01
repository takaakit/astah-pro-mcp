package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDiagramWithAlignAttributeItemsDTO(
    @JsonPropertyDescription("Target ER diagram identifier")
    String targetERDiagramId,

    @JsonPropertyDescription("Align attribute items. if true, it is Align Attribute Items, otherwise, it is not Align Attribute Items.")
    boolean isAlignAttributeItems
) {
}
