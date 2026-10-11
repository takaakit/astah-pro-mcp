package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record EREntityDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Parent ER relationships: ER relationships in which this entity is the child.")
    List<NameIdTypeDTO> parentERRelationships,

    @JsonPropertyDescription("Parent ER subtype relationships: ER subtype relationships in which this entity is the child.")
    List<NameIdTypeDTO> parentERSubtypeRelationships,

    @JsonPropertyDescription("Child ER relationships: ER relationships in which this entity is the parent.")
    List<NameIdTypeDTO> childERRelationships,

    @JsonPropertyDescription("Child ER subtype relationships: ER subtype relationships in which this entity is the parent.")
    List<NameIdTypeDTO> childERSubtypeRelationships,

    @JsonPropertyDescription("ER indices")
    List<NameIdTypeDTO> erIndices,

    @JsonPropertyDescription("Primary keys")
    List<NameIdTypeDTO> primaryKeys,

    @JsonPropertyDescription("Foreign keys")
    List<NameIdTypeDTO> foreignKeys,

    @JsonPropertyDescription("Non primary keys")
    List<NameIdTypeDTO> nonPrimaryKeys,

    @JsonPropertyDescription("Logical name")
    String logicalName,

    @JsonPropertyDescription("Physical name. An empty string in case there is none.")
    String physicalName,

    @JsonPropertyDescription("Type: \"Resource\",\"Event\",\"Summary\". An empty string in case there is none")
    String type
) {
}
