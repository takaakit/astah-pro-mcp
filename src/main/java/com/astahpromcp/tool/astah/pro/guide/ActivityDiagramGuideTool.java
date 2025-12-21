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
public class ActivityDiagramGuideTool implements ToolProvider {

    public ActivityDiagramGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
	        return List.of(
	                ToolSupport.definition(
	                        "activity_dgm_guide",
	                        "MCP client (you) MUST call this tool function before referencing or editing a activity diagram to understand its usage and terminology definitions.",
	                        this::getGuide,
	                        NoInputDTO.class,
	                        GuideDTO.class)
	        );

        } catch (Exception e) {
            log.error("Failed to create activity diagram guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get activity diagram guide: {}", param);
        
        String content = """
IMPORTANT POINTS to Keep in Mind:
* After editing a diagram, retrieve its drawing content and verify that the edits and layout are as intended.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* ActivityNodes are used to model the individual steps in the behavior specified by an Activity.
* ControlNodes act as "traffic switches" managing the flow of tokens across ActivityEdges. Tokens cannot "rest" at ControlNodes.
* ObjectNodes hold object tokens accepted from incoming ObjectFlows and may subsequently offer them to outgoing ObjectFlows.
* A ControlFlow is an ActivityEdge that only passes control tokens. ControlFlows are used to explicitly sequence execution of ActivityNodes, as the target ActivityNode cannot receive a control token and start execution until the source ActivityNode completes execution and produces the token.
* An ObjectFlow is an ActivityEdge that can have object tokens passing along it. ObjectFlows model the flow of values between ObjectNodes. Tokens are offered to the target ActivityNode in the same order as they are offered from the source.
* Object tokens pass over ObjectFlows, carrying data through an Activity via their values, or carrying no data (null tokens).
* An ObjectFlow may have a transformation Behavior that has a single input Parameter and a single output Parameter. If a transformation Behavior is specified, then the Behavior is invoked for each object token offered to the ObjectFlow, with the value in the token passed to the Behavior as input (for a null token, the behavior is invoked but no value is passed). The output of the Behavior is put in an object token that is offered to the target ActivityNode instead of the original object token.
* When an Activity is invoked, any values passed to its input Parameters are put in object tokens and placed on the corresponding input ActivityParameterNodes for the Activity (if an input parameter has no value, a null token is placed on the corresponding ActivityParameterNode). These ActivityParameterNodes then offer their tokens to outgoing ActivityEdges.
* An InitialNode is a ControlNode that acts as a starting point for executing an Activity. An Activity may have more than one InitialNode. If an Activity has more than one InitialNode, then invoking the Activity starts multiple concurrent control flows, one for each InitialNode.
* An InitialNode shall not have any incoming ActivityEdges, which means the InitialNodes owned by an Activity will always be enabled when the Activity begins execution and a single control token is placed on each such InitialNode when Activity execution starts. The outgoing ActivityEdges of an InitialNode must all be ControlFlows. The control token placed on an InitialNode is offered concurrently on all outgoing ControlFlows.
* A FinalNode is a ControlNode at which a flow in an Activity stops. A FinalNode shall not have outgoing ActivityEdges.
* A FlowFinalNode is a FinalNode that terminates a flow.
* A ForkNode is a ControlNode that splits a flow into multiple concurrent flows. A ForkNode shall have exactly one incoming ActivityEdge, though it may have multiple outgoing ActivityEdges.
* A JoinNode is a ControlNode that synchronizes multiple flows. A JoinNode shall have exactly one outgoing ActivityEdge but may have multiple incoming ActivityEdges.
* A MergeNode is a control node that brings together multiple flows without synchronization. A MergeNode shall have exactly one outgoing ActivityEdge but may have multiple incoming ActivityEdges.
* An ObjectNode holds object tokens during the course of the execution of an Activity. Except in the case of an input 
ActivityParameterNode (as discussed further below), the tokens held by an ObjectNode arrive from incoming ActivityEdges. Except in the case of an output ActivityParameterNode, tokens held by an ObjectNode may leave the node on outgoing ActivityEdges.
* An ObjectNode may not contain more tokens than specified by its upperBound, if any. If an ObjectNode has an upperBound, then this ValueSpecification shall evaluate to an UnlimitedNatural value.
* An ActivityParameterNode shall have either all incoming or all outgoing ActivityEdges. An ActivityParameterNode with outgoing edges is an input ActivityParameterNode, while an ActivityParameterNode with incoming edges is an output ActivityParameterNode.
* Actions are contained in Behaviors, specifically Activities and Interactions.
* An Action may accept inputs and produce outputs, as specified by InputPins and OutputPins of the Action, respectively. Each Pin on an Action specifies the type and multiplicity for a specific input or output of that Action.
* A Pin represents an input to an Action or an output from an Action. An InputPin represents an input, while an OutputPin represents an output. Each of the sets of inputs and outputs owned by an Action are ordered.
* A SendSignalAction is notated as a convex pentagon with the name of the Signal placed inside it.
        """;
        
        return new GuideDTO(content);
    }
}
