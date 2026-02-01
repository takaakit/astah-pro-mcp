package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewEREntityInERPackageDTO(
    @JsonPropertyDescription("Parent ER package identifier")
    String parentERPackageId,

    @JsonPropertyDescription("New ER entity logical name")
    String newEREntityLogicalName,

    @JsonPropertyDescription("New ER entity physical name")
    String newEREntityPhysicalName
) {
}
