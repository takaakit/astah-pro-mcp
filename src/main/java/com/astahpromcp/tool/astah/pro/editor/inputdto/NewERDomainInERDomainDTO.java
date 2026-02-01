package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewERDomainInERDomainDTO(
    @JsonPropertyDescription("Parent ER domain identifier")
    String parentERDomainId,

    @JsonPropertyDescription("New ER domain logical name")
    String newERDomainLogicalName,

    @JsonPropertyDescription("New ER domain physical name")
    String newERDomainPhysicalName,

    @JsonPropertyDescription("ER datatype identifier")
    String erDatatypeId
) {
}
