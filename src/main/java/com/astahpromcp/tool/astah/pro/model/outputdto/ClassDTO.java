package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ClassDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Attributes")
    List<AttributeDTO> attributes,

    @JsonPropertyDescription("Operations")
    List<OperationDTO> operations,

    @JsonPropertyDescription("Associations")
    List<AssociationDTO> associations,

    @JsonPropertyDescription("Generalizations")
    List<GeneralizationDTO> generalizations,

    @JsonPropertyDescription("Specializations")
    List<GeneralizationDTO> specializations,

    @JsonPropertyDescription("Nested classes")
    List<NameIdTypeDTO> nestedClasses,

    @JsonPropertyDescription("Template parameters")
    List<NameIdTypeDTO> templateParameters,

    @JsonPropertyDescription("Is Abstract")
    boolean isAbstract,

    @JsonPropertyDescription("Is Active")
    boolean isActive,

    @JsonPropertyDescription("Is Leaf")
    boolean isLeaf
) {
}
