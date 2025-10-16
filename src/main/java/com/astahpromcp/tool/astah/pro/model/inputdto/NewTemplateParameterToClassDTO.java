package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTemplateParameterToClassDTO(
        @JsonPropertyDescription("Target class identifier")
        String targetClassId,

        @JsonPropertyDescription("Template parameter type identifier")
        String templateParameterTypeId,

        @JsonPropertyDescription("New template parameter name")
        String newTemplateParameterName
) {
}
