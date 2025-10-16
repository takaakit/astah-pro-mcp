package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
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
