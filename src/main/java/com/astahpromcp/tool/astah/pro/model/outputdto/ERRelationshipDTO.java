package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERRelationshipDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Parent ER entity")
    NameIdTypeDTO parentEREntity,

    @JsonPropertyDescription("Child ER entity")
    NameIdTypeDTO childEREntity,

    @JsonPropertyDescription("ER index")
    NameIdTypeDTO erIndex,

    @JsonPropertyDescription("Foreign keys")
    List<NameIdTypeDTO> foreignKeys,

    @JsonPropertyDescription("Cardinality")
    String cardinality,

    @JsonPropertyDescription("Logical name")
    String logicalName,

    @JsonPropertyDescription("Physical name. An empty string in case there is none.")
    String physicalName,

    @JsonPropertyDescription("Verb Phrase(Parent to Child). null in case there is none.")
    String verbPhraseParentToChild,

    @JsonPropertyDescription("Verb Phrase(Child to Parent). null in case there is none.")
    String verbPhraseChildToParent,

    @JsonPropertyDescription("Check if the ER relationship is Identifying.")
    boolean isIdentifying,

    @JsonPropertyDescription("Check if the ER relationship is Non-Identifying.")
    boolean isNonIdentifying,

    @JsonPropertyDescription("Check if the ER relationship is Many-to-Many.")
    boolean isManyToMany,

    @JsonPropertyDescription("Check if the ER relationship is Parent required.")
    boolean isParentRequired
) {
}
