package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERSchemaDTO(
    @JsonPropertyDescription("ER package information")
    ERPackageDTO erPackage,

    @JsonPropertyDescription("ER domains")
    List<NameIdTypeDTO> erDomains,

    @JsonPropertyDescription("ER datatypes")
    List<NameIdTypeDTO> erDatatypes
) {
}
