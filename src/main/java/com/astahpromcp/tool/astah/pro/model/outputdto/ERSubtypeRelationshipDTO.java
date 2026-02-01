package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERSubtypeRelationshipDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Parent ER entity")
    NameIdTypeDTO parentEREntity,

    @JsonPropertyDescription("Child ER entity")
    NameIdTypeDTO childEREntity,

    @JsonPropertyDescription("Identifier ER attribute. null in case there is none.")
    NameIdTypeDTO identifierERAttribute,

    @JsonPropertyDescription("Foreign keys")
    List<NameIdTypeDTO> foreignKeys,

    @JsonPropertyDescription("Logical name")
    String logicalName,

    @JsonPropertyDescription("Physical name. An empty string in case there is none.")
    String physicalName,

    @JsonPropertyDescription("Check if the ER subtype relationship is Conclusive.")
    boolean isConclusive
) {
}
