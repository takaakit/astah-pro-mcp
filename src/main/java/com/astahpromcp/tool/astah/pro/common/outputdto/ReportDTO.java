package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ReportDTO(
    @JsonPropertyDescription("Content of report")
    String content
) {
}
