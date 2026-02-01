package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERDomainDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Child ER domains")
    List<NameIdTypeDTO> childERDomains,

    @JsonPropertyDescription("ER datatype name")
    String erDatatypeName,

    @JsonPropertyDescription("Default value. An empty string in case there is none.")
    String defaultValue,

    @JsonPropertyDescription("Length/Precision. An empty string in case there is none.")
    String lengthPrecision,

    @JsonPropertyDescription("Logical name")
    String logicalName,

    @JsonPropertyDescription("Physical name. An empty string in case there is none.")
    String physicalName,

    @JsonPropertyDescription("Check if the ER domain is NOT NULL.")
    boolean isNotNull
) {
}
