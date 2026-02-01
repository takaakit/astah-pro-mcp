package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewERPackageInERPackageDTO(
    @JsonPropertyDescription("Parent ER package identifier")
    String parentERPackageId,

    @JsonPropertyDescription("New ER package name")
    String newERPackageName
) {
}
