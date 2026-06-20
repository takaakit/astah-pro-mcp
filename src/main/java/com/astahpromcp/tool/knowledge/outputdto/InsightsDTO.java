package com.astahpromcp.tool.knowledge.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record InsightsDTO(
    @JsonPropertyDescription("Insight contents on UML, modeling, and architecture")
    String contents,

    @JsonPropertyDescription("Absolute path of the stored insight file")
    String insightFilePath
) {
}
