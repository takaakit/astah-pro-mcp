package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERAttributeDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("ER datatype")
    ERDatatypeDTO erDatatype,

    @JsonPropertyDescription("ER domain")
    NameIdTypeDTO erDomain,

    @JsonPropertyDescription("ER indices")
    List<NameIdTypeDTO> erIndices,

    @JsonPropertyDescription("Referenced primary key")
    NameIdTypeDTO referencedPrimaryKey,

    @JsonPropertyDescription("Referenced foreign keys")
    List<NameIdTypeDTO> referencedForeignKeys,

    @JsonPropertyDescription("Referenced ER relationship")
    NameIdTypeDTO referencedERRelationship,

    @JsonPropertyDescription("Referenced ER subtype relationships.")
    List<NameIdTypeDTO> referencedERSubtypeRelationships,

    @JsonPropertyDescription("Associated ER subtype relationship as discriminator attribute")
    NameIdTypeDTO associatedERSubtypeRelationshipAsDiscriminatorAttribute,

    @JsonPropertyDescription("Default value. An empty string in case there is none.")
    String defaultValue,

    @JsonPropertyDescription("Length/Precision. An empty string in case there is none.")
    String lengthPrecision,

    @JsonPropertyDescription("Logical name")
    String logicalName,

    @JsonPropertyDescription("Physical name. An empty string in case there is none.")
    String physicalName,

    @JsonPropertyDescription("Check if the ER attribute is primary key.")
    boolean isPrimaryKey,

    @JsonPropertyDescription("Check if the ER attribute is foreign key.")
    boolean isForeignKey,

    @JsonPropertyDescription("Check if the ER attribute is NOT NULL.")
    boolean isNotNull
) {
}
