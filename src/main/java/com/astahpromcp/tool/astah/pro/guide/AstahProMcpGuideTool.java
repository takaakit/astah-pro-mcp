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
public class AstahProMcpGuideTool implements ToolProvider {

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "astah_pro_mcp_guide",
                            "MCP client (you) MUST call this tool function before referencing or editing an Astah project to understand how to use this MCP server.",
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
        log.debug("Get Astah Pro MCP Guide: {}", param);
        
        String content = """
This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, you can reference and edit an Astah project.

"Astah projects consist of Models and Presentations. Presentation is visual information to notate the model elements in Astah. For example, if you want to get color information of a specific Class, you access presentation. If you want to edit attributes of a specific Class, you access model. Just remember, you use presentations for anything visual. The correspondence between the model and presentation is not necessarily 1:1. Some model elements have presentations and some don't (Astah API User Guide)."


IMPORTANT POINTS to Keep in Mind:
* When creating a presentation on a diagram that corresponds to a model, you must provide not only the diagram information but also the information of the corresponding model. In contrast, when creating a presentation that is not associated with a model (such as notes), no corresponding model information is required.  
* Deleting a presentation does not remove the corresponding model. In contrast, deleting a model will also remove its corresponding presentation.  
* Association ends are attribute elements (member ends) of the association. Therefore, the information of association ends can be obtained through the information of the association.  
* The Astah that this MCP server references and edits is also edited by users. Therefore, assume that the project itself—and the model elements and presentations it contains—may be updated, and retrieve the latest information from Astah as needed. For example, a user may switch to a different Astah project, or make changes to model elements or presentations.  
* Object diagrams and package diagrams are substituted with class diagrams. This means that, for example, instance specifications and instance specification links are drawn on class diagrams.  


Procedure for Editing Models and Diagrams:
* When editing models and diagrams, execute the loop below. End the loop when editing is complete, when no further edits are possible, or when the loop has been repeated five or more times.
  Draft an edit plan → Apply the edits → Retrieve and evaluate information about the post-edit model and diagram → If any deficiencies are found, return to drafting an edit plan.


Inheritance Structure of Element Types:
The following PlantUML code illustrates the inheritance structure of element types in Astah. Child types inherit all the characteristics of their parent types. Every element in an Astah project belongs to one of these element types. The definitions of element types in Astah generally follow those defined in the UML Specification, although some of them are slightly customized.

```plantuml
@startuml
Action --|> ActivityNode
Activity --|> NamedElement
ActivityDiagram --|> Diagram
ActivityNode --|> NamedElement
ActivityParameterNode --|> ObjectNode
Anchor --|> DataFlowNode
Artifact --|> Class
Association --|> NamedElement
AssociationClass --|> Class
AssociationClass --|> Association
Attribute --|> NamedElement
Class --|> NamedElement
ClassDiagram --|> Diagram
ClassifierTemplateParameter --|> NamedElement
CombinedFragment --|> InteractionFragment
Comment --|> NamedElement
CommunicationDiagram --|> Diagram
Component --|> Class
ComponentDiagram --|> Diagram
CompositeStructureDiagram --|> Diagram
Connector --|> NamedElement
Constraint --|> NamedElement
ControlNode --|> ActivityNode
DataFlow --|> NamedElement
DataFlowDiagram --|> Diagram
DataFlowNode --|> NamedElement
DataStore --|> DataFlowNode
Dependency --|> NamedElement
DeploymentDiagram --|> Diagram
Diagram --|> NamedElement
DurationConstraint --|> Constraint
ERAttribute --|> NamedElement
ERDatatype --|> NamedElement
ERDiagram --|> Diagram
ERDomain --|> NamedElement
EREntity --|> NamedElement
ERIndex --|> NamedElement
ERModel --|> Model
ERPackage --|> Package
ERRelationship --|> NamedElement
ERSchema --|> ERPackage
ERSubtypeRelationship --|> NamedElement
Element --|> Entity
Enumeration --|> Class
EnumerationLiteral --|> InstanceSpecification
Extend --|> NamedElement
ExtentionPoint --|> NamedElement
ExternalEntity --|> DataFlowNode
FinalState --|> State
Flow --|> NamedElement
Gate --|> NamedElement
Generalization --|> NamedElement
Include --|> NamedElement
InputPin --|> Pin
InstanceSpecification --|> NamedElement
Interaction --|> NamedElement
InteractionFragment --|> NamedElement
InteractionOperand --|> NamedElement
InteractionUse --|> InteractionFragment
Lifeline --|> NamedElement
LifelineLink --|> NamedElement
Link --|> NamedElement
LinkEnd --|> NamedElement
MatrixDiagram --|> Diagram
Message --|> NamedElement
MindMapDiagram --|> Diagram
Model --|> Package
NamedElement --|> Element
NamedElement --|> HyperlinkOwner
Node --|> Class
ObjectNode --|> ActivityNode
Operation --|> NamedElement
OutputPin --|> Pin
Package --|> NamedElement
Parameter --|> NamedElement
Partition --|> NamedElement
Pin --|> ObjectNode
Port --|> Attribute
ProcessBox --|> DataFlowNode
Pseudostate --|> Vertex
Realization --|> NamedElement
Requirement --|> Class
RequirementDiagram --|> Diagram
RequirementTable --|> Diagram
SequenceDiagram --|> Diagram
Slot --|> NamedElement
State --|> Vertex
StateInvariant --|> InteractionFragment
StateMachine --|> NamedElement
StateMachineDiagram --|> Diagram
Subsystem --|> Package
Subsystem --|> Class
TaggedValue --|> Element
TemplateBinding --|> NamedElement
Termination --|> Message
TestCase --|> Class
TimeConstraint --|> Constraint
TimingDiagram --|> Diagram
TraceabilityMap --|> MindMapDiagram
Transition --|> NamedElement
Usage --|> NamedElement
UseCase --|> Class
UseCaseDiagram --|> Diagram
Vertex --|> NamedElement
Cell --|> Presentation
HeaderCell --|> Cell
LinkPresentation --|> Presentation
NodePresentation --|> Presentation
Presentation --|> Entity
Presentation --|> HyperlinkOwner
TopicPresentation --|> Presentation
ValueCell --|> Cell
@enduml
```


Relationships between Element Types:
The following PlantUML code illustrates the relationships between the main element types in Astah. The definitions of element types in Astah generally follow those defined in the UML Specification, although some of them are slightly customized.

```plantuml
@startuml
Element ---> "owner element" Element
Element ---> "corresponding presentations" Presentation
Element ---> "tagged values" TaggedValue
Element ---> "comments" Comment
Element ---> "tagged values" TaggedValue
NamedElement ---> "client dependencies" Dependency
NamedElement ---> "supplier dependencies" Dependency
NamedElement ---> "client realizations" Realization
NamedElement ---> "supplier realizations" Realization
NamedElement ---> "client usages" Usage
NamedElement ---> "supplier usages" Usage
NamedElement ---> "drawing targets" Diagram
Package ---> "owned elements" NamedElement
Class ---> "attributes" Attribute
Class ---> "operations" Operation
Class ---> "generalizations" Generalization
Class ---> "nested classes" Class
Attribute ---> "type" Class
Operation ---> "return type" Class
Operation ---> "parameters" Parameter
Parameter ---> "type" Class
Enumeration ---> "literals" EnumerationLiteral
Association ---> "member ends" Attribute
Generalization ---> "super type" Class
Generalization ---> "sub type" Class
Realization ---> "client" NamedElement
Realization ---> "supplier" NamedElement
Dependency ---> "client" NamedElement
Dependency ---> "supplier" NamedElement
Usage ---> "client" NamedElement
Usage ---> "supplier" NamedElement
Comment ---> "annotated elements" Element
InstanceSpecification ---> "corresponding classifier" Class
InstanceSpecification ---> "link ends" LinkEnd
Link ---> "link ends" Attribute
LinkEnd ---> "type" InstanceSpecification
Diagram ---> "drawn presentations" Presentation
NodePresentation ---> "links" LinkPresentation
LinkPresentation ---> "source end" Presentation
LinkPresentation ---> "target end" Presentation
@enduml
```
""";

        return new GuideDTO(content);
    }
}
