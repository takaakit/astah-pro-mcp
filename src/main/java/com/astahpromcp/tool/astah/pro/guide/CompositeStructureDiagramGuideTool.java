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
public class CompositeStructureDiagramGuideTool implements ToolProvider {

    public CompositeStructureDiagramGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
	        return List.of(
	                ToolSupport.definition(
	                        "composite_structure_dgm_guide",
	                        "MCP client (you) MUST call this tool function before referencing or editing a composite structure diagram to understand its usage and terminology definitions.",
	                        this::getGuide,
	                        NoInputDTO.class,
	                        GuideDTO.class)
	        );

        } catch (Exception e) {
            log.error("Failed to create composite structure diagram guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get composite structure diagram guide: {}", param);
        
        String content = """
IMPORTANT POINTS to Keep in Mind:
* After editing a diagram, retrieve its drawing content and verify that the edits and layout are as intended.
* In the definition below, StructuredClassifier and EncapsulatedClassifier correspond to the Class element in Astah. Therefore, understand StructuredClassifier as Class.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* All of the ownedAttributes of a StructuredClassifier are roles and can be connected using Connectors. Those ownedAttributes of a StructuredClassifier that have isComposite = true are called its parts. Hence parts constitute a subset of roles.
* A Connector specifies links between two or more instances playing owned or inherited roles within a StructuredClassifier.
* A Connector may be typed by an Association, in which case the links specified by the Connector are instances of the typing Association.
* A part may be shown by graphical nesting of a box symbol with a solid outline representing the part within the internal structure compartment.
* A role that is not a composition may be shown by graphical nesting of a box symbol with a dashed outline.
* In either case the box may be called a part box, even though strictly-speaking only the compositions are parts.
* Lollipop and socket symbols may optionally be shown to indicate the provided and required interfaces of the part.
* When a role is typed by an EncapsulatedClassifier, any Ports of the type may also be shown as small square symbols overlapping the boundary of the part box denoting the role.
* Lollipop and socket symbols may optionally be shown to indicate the provided and required interfaces of the Port.
* EncapsulatedClassifier extends StructuredClassifier with the ability to own Ports, a mechanism for isolating an EncapsulatedClassifier from its environment.
* Ports represent interaction points through which an EncapsulatedClassifier communicates with its environment. Multiple Ports can be defined for an EncapsulatedClassifier, enabling different communications to be distinguished based on the Port through which they occur.
* Ports are connected by Connectors through which requests can be made to invoke the BehavioralFeatures of an EncapsulatedClassifier. A Port may specify the services an EncapsulatedClassifier provides (offers) to its environment as well as the services that an EncapsulatedClassifier expects (requires) of its environment.
* The property isService, when true, indicates that this Port is used to provide the published functionality of an EncapsulatedClassifier. If false, this Port is used to implement the EncapsulatedClassifier but is not part of the essential externally-visible functionality of the EncapsulatedClassifier.
* The phrase Port on Part or more generally Port on Property signifies the situation where a Property playing a role in a StructuredClassifier is typed by an EncapsulatedClassifier that has Ports.
* The Interfaces associated with a Port specify the nature of the interactions that may occur over it. The required Interfaces of a Port characterize the requests that may be made from the EncapsulatedClassifier to its environment through this Port. Instances of this EncapsulatedClassifier expect that the Features owned by its required Interfaces will be offered by one or more instances in its environment. The provided Interfaces of a Port characterize requests to the EncapsulatedClassifier that its environment may make through this Port. The owning EncapsulatedClassifier must offer the Features owned by the provided Interfaces.
* As a kind of Property, a Port has a type.
* A Port has the ability, by setting the property isBehavior to true, to specify that any requests arriving at this Port are handled by the Behavior of the instance of the owning EncapsulatedClassifier, rather than being forwarded to any contained instances, if any. Such a Port is called a behavior Port.
* A Component can always be considered an autonomous unit within a system or subsystem. It has one or more provided and/or required Interfaces (potentially exposed via Ports), and its internals are hidden and inaccessible other than as provided by its Interfaces.
* The Components package supports the specification of both logical Components (e.g., business components, process components) and physical Components (e.g., EJB components, CORBA components, COM+ and .NET components, WSDL components, etc.), along with the artifacts that implement them and the nodes on which they are deployed and executed.
* A Component is a self-contained unit that encapsulates the state and behavior of a number of Classifiers. A Component specifies a formal contract of the services that it provides to its clients and those that it requires from other Components or services in the system in terms of its provided and required Interfaces.
* A Component may be manifested by one or more Artifacts, and in turn, that Artifact may be deployed to its execution environment.
* A Component may implement a provided Interface directly, or its realizing Classifiers may do so, or they may be inherited. The required and provided Interfaces may optionally be organized through Ports; these enable the definition of named sets of provided and required Interfaces that are typically (but not always) addressed at run-time.
* A Component may be realized (or implemented) by a number of Classifiers. In that case, a Component owns a set of ComponentRealizations to these Classifiers.
        """;

        return new GuideDTO(content);
    }
}
