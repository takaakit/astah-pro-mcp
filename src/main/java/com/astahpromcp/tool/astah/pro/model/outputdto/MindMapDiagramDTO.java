package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record MindMapDiagramDTO(
    @JsonPropertyDescription("Diagram info")
    DiagramDTO diagram,

    @JsonPropertyDescription("Root topic")
    LabelIdTypeDTO rootTopic,

    @JsonPropertyDescription("Floating topics")
    List<LabelIdTypeDTO> floatingTopics
) {    
}
