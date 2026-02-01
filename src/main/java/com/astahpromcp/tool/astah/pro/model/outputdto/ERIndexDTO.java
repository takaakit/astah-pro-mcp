package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERIndexDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("ER attributes")
    List<NameIdTypeDTO> erAttributes,

    @JsonPropertyDescription("ER relationships with this er Index")
    List<NameIdTypeDTO> erRelationships,

    @JsonPropertyDescription("Parent ER entity")
    NameIdTypeDTO parentEREntity,

    @JsonPropertyDescription("A kind of ER index: AK1...,IE1..., etc.")
    String kind,

    @JsonPropertyDescription("Check if the Key(AK,IE) is shown.")
    boolean isKey,

    @JsonPropertyDescription("Check if the ER index is unique.")
    boolean isUnique
) {
}
