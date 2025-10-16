package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.guide.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SequenceDiagramGuideTool implements ToolProvider {

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "seq_dgm_guide",
                            "MCP client (you) MUST call this tool function before referencing or editing a sequence diagram to understand its usage and terminology definitions.",
                            this::getGuide,
                            NoInputDTO.class,
                            GuideDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get sequence diagram guide: {}", param);
        
        String content = """
IMPORTANT POINTS to Keep in Mind:
* After editing a diagram, retrieve its drawing content and verify that the edits and layout are as intended.
* When creating a lifeline, if a corresponding base class exists, set that class as the type of the lifeline.
* When adding a message name, if a corresponding operation exists in the base class associated with the target lifeline of the message, assign that operation to the message. Otherwise, specify an arbitrary message name, arguments, and return value.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* Interactions are units of behavior of an enclosing Classifier. Interactions focus on the passing of information with Messages between the ConnectableElements of the Classifier.
* An InteractionFragment may either be contained directly in an enclosing Interaction, or may be contained within an InteractionOperand of a CombinedFragment. As a CombinedFragment is itself an InteractionFragment, there may be multiple nesting levels of InteractionFragments within an Interaction.
* The notation for an Interaction in a Sequence Diagram is a solid-outline rectangle. A pentagon in the upper left corner of the rectangle contains ‘sd’ followed by the Interaction name and parameters.
* There is no general notation for an InteractionFragment. The specific subclasses of InteractionFragment define their own notation.
* In an interaction diagram a Lifeline describes the time-line for a process, where time increases down the page. The distance between two events on a time-line does not represent any literal measurement of time, only that non-zero time has passed.
* The semantics of a complete Message is simply the trace <sendEvent, receiveEvent>.
* A lost Message is a Message where the sending event occurrence is known, but there is no receiving event occurrence. We interpret this to be because the destination of the [lost]Message is outside the scope of the description. The semantics is simply the trace <sendEvent>.
* A found Message is a Message where the receiving event occurrence is known, but there is no (known) sending event occurrence. We interpret this to be because the origin of the Message is outside the scope of the description. This may for example be noise or other activity that we do not want to describe in detail. The semantics is simply the trace <receiveEvent>.
* Subclasses of MessageEnd define the specific semantics appropriate to the concept they represent.
* A Gate is a MessageEnd which is used on the boundary of an Interaction, or an InteractionUse, or a CombinedFragment to establish the concrete sender and receiver for every Message.
* A message is shown as a line from the sender MessageEnd to the receiver MessageEnd. The line must be such that every line fragment is either horizontal or downwards when traversed from send event to receive event. The send and receive events may both be on the same lifeline.
* Gates are just points on the frame, the ends of the messages.
* An InteractionOperand is a region within a CombinedFragment.
* Only InteractionOperands with true guards are included in the calculation of the semantics. If no guard is present, this is taken to mean a true guard.
* InteractionConstraints are always used in connection with CombinedFragments.
* The semantics of a CombinedFragment is dependent upon the interactionOperator, as explained below for each kind of interactionOperator.
* The Gates associated with a CombinedFragment represent the syntactic interface between the CombinedFragment and its surroundings, which means the interface towards other InteractionFragments.
* The interactionOperator alt designates that the CombinedFragment represents a choice of behavior.
* The interactionOperator opt designates that the CombinedFragment represents a choice of behavior where either the (sole) operand happens or nothing happens.
* The interactionOperator break designates that the CombinedFragment represents a breaking scenario in the sense that the operand is a scenario that is performed instead of the remainder of the enclosing InteractionFragment.
* The interactionOperator par designates that the CombinedFragment represents a parallel merge between the behaviors of the operands.
* The interactionOperator seq designates that the CombinedFragment represents a weak sequencing between the behaviors of the operands.
* The interactionOperator strict designates that the CombinedFragment represents a strict sequencing between the behaviors of the operands.
* The interactionOperator neg designates that the CombinedFragment represents traces that are defined to be invalid.
* The interactionOperator critical designates that the CombinedFragment represents a critical region.
* The interactionOperator ignore designates that there are some message types that are not shown within this combined fragment.
* The interactionOperator assert designates that the CombinedFragment represents an assertion. The sequences of the operand of the assertion are the only valid continuations.
* The interactionOperator loop designates that the CombinedFragment represents a loop. The loop operand will be repeated a number of times.
* InteractionOperands are separated by a dashed horizontal line. The InteractionOperands together make up the framed CombinedFragment. Within an InteractionOperand of a Sequence Diagram the order of the InteractionFragments are given simply by the topmost vertical position.
* The notation for a CombinedFragment in a Sequence Diagram is a solid-outline rectangle. The operator is shown in a pentagon in the upper left corner of the rectangle.
* The value of the InteractionOperandKind is given as text in a small compartment in the upper left corner of the CombinedFragment frame.
* A "coregion" is a notational shorthand for parallel combined fragments, used for the common situation where the order of event occurrences (or other nested fragments) on one Lifeline is insignificant. This means that in a given “coregion” area of a Lifeline all the directly contained fragments are considered separate operands of a parallel combined fragment.
* The semantics of the InteractionUse is the set of traces of the semantics of the referred Interaction where the gates have been resolved as well as all generic parts having been bound such as the arguments substituting the parameters.
* The InteractionUse is shown as a CombinedFragment symbol where the operator is called ref.
        """;
        
        return new GuideDTO(content);
    }
}
