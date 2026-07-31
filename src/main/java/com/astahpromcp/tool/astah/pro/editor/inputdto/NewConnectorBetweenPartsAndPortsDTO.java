package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewConnectorBetweenPartsAndPortsDTO(
    @JsonPropertyDescription("Source part identifier. It must be an association end, not a plain attribute.")
    String sourcePartId,

    @JsonPropertyDescription("Source port identifier. It must be a port owned by the type of the source part. If the source side is connected via the part itself instead of a port, set an empty string.")
    String sourcePortId,

    @JsonPropertyDescription("Target part identifier. It must be an association end, not a plain attribute.")
    String targetPartId,

    @JsonPropertyDescription("Target port identifier. It must be a port owned by the type of the target part. If the target side is connected via the part itself instead of a port, set an empty string.")
    String targetPortId
) {
}
