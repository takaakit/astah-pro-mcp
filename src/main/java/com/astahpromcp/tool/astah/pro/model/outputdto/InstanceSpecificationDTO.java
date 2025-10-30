package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record InstanceSpecificationDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Classifier")
    NameIdTypeDTO classifier,

    @JsonPropertyDescription("Parent instance specification")
    NameIdTypeDTO parentInstanceSpecification,

    @JsonPropertyDescription("Link ends")
    List<NameIdTypeDTO> linkEnds,

    @JsonPropertyDescription("Slots")
    List<NameIdTypeDTO> slots
) {
}
