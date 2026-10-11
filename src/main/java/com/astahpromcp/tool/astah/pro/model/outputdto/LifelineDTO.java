package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record LifelineDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Base class")
    NameIdTypeDTO baseClass,

    @JsonPropertyDescription("Fragments: The interaction fragments (including messages) Astah's model records on this lifeline outside any combined fragment. Astah's record of what a combined fragment holds can differ from the diagram, so a message drawn inside a combined fragment may be listed here; get_interaction_operand_info reports what the diagram draws in each operand.")
    List<NameIdTypeDTO> fragments,

    @JsonPropertyDescription("Lifeline links")
    List<NameIdTypeDTO> lifelineLinks
) {
}
