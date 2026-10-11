package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record InteractionOperandDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Guard condition")
    String guard,

    @JsonPropertyDescription("Lifelines: The lifelines Astah's model records as covered by this operand. This record can differ from the diagram, so use drawnLifelines for what the diagram shows.")
    List<NameIdTypeDTO> lifelines,

    @JsonPropertyDescription("Messages: The messages Astah's model records as held by this operand. This record can differ from the diagram, so use drawnMessages for what the diagram shows.")
    List<NameIdTypeDTO> messages,

    @JsonPropertyDescription("Drawn lifelines: The lifelines the diagram draws across this operand, ordered from left to right: those whose head is centered within the width of the combined fragment and starts above the bottom of this operand.")
    List<NameIdTypeDTO> drawnLifelines,

    @JsonPropertyDescription("Drawn messages: The messages the diagram draws inside this operand, ordered from top to bottom: those with both ends within the width of the combined fragment that start within the height of this operand. A message inside nested combined fragments is listed only for the innermost operand.")
    List<NameIdTypeDTO> drawnMessages,

    @JsonPropertyDescription("Drawn height: The height of this operand on the diagram, as set_height_of_interaction_operand takes it. 0 when the operand is not drawn.")
    double drawnHeight,

    @JsonPropertyDescription("Matches diagram: Whether lifelines and messages hold the same elements as drawnLifelines and drawnMessages. When false, Astah's model does not record what the diagram shows.")
    boolean matchesDiagram
) {
}
