package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record SourceTargetNameIdTypeListDTO(
    @JsonPropertyDescription("Source classifiers of the inheritance arrow")
    List<NameIdTypeDTO> inheritanceSourceClassifier,

    @JsonPropertyDescription("Target classifiers of the inheritance arrow")
    List<NameIdTypeDTO> inheritanceTargetClassifier,

    @JsonPropertyDescription("Source classifiers of the realization arrow")
    List<NameIdTypeDTO> realizationSourceClassifier,

    @JsonPropertyDescription("Target classifiers of the realization arrow")
    List<NameIdTypeDTO> realizationTargetClassifier,

    @JsonPropertyDescription("Source classifiers of the association arrow")
    List<NameIdTypeDTO> associationSourceClassifier,

    @JsonPropertyDescription("Target classifiers of the association arrow")
    List<NameIdTypeDTO> associationTargetClassifier,

    @JsonPropertyDescription("Source classifiers of the dependency arrow")
    List<NameIdTypeDTO> dependencySourceClassifier,

    @JsonPropertyDescription("Target classifiers of the dependency arrow")
    List<NameIdTypeDTO> dependencyTargetClassifier,

    @JsonPropertyDescription("Source classifiers of the type usage. 'Type usage' means being used as a type—for example, as an attribute type or as the return type of an operation.")
    List<NameIdTypeDTO> typeUsageSourceClassifier,

    @JsonPropertyDescription("Target classifiers of the type usage. 'Type usage' means being used as a type—for example, as an attribute type or as the return type of an operation.")
    List<NameIdTypeDTO> typeUsageTargetClassifier
) {
}
