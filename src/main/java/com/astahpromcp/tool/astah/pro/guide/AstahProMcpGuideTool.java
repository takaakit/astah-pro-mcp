package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AstahProMcpGuideTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;

    public AstahProMcpGuideTool(ProjectAccessor projectAccessor) {
        this.projectAccessor = projectAccessor;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
	        return List.of(
	            ToolSupport.toolDefinitionReturningDto(
	                "astah_pro_mcp_guide",
	                "MCP client (you) MUST call this tool function before referencing or editing an Astah project to understand how to use this MCP server.",
	                this::getGuide,
	                NoInputDTO.class,
	                GuideDTO.class)
	        );

        } catch (Exception e) {
            log.error("Failed to create astah pro mcp guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(NoInputDTO param) throws Exception {
        log.debug("Get Astah Pro MCP Guide: {}", param);

        // If the Astah project is not open, create a new one.
        if (!projectAccessor.hasProject()) {
            try {
                projectAccessor.create();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create project (root package).");
            }
        }

        String primitiveTypes = "";
        for (INamedElement primitiveType : projectAccessor.getPrimitiveTypes()) {
            primitiveTypes += primitiveType.getName() + System.lineSeparator();
        }
        
        String contents = """
This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, you can reference and edit an Astah project.

"Astah projects consist of Models and Presentations. Presentation is visual information to notate the model elements in Astah. For example, if you want to get color information of a specific Class, you access presentation. If you want to edit attributes of a specific Class, you access model. Just remember, you use presentations for anything visual. The correspondence between the model and presentation is not necessarily 1:1. Some model elements have presentations and some don't (Astah API User Guide)."


IMPORTANT POINTS to Keep in Mind:
* Astah has only one project open at a time, and that project is shared by all agents and subagents. You may create new Astah projects freely, but even when work is split across multiple working directories (for example, git worktrees), NEVER duplicate an existing Astah project file into any of them.
* When you create a new diagram, you MUST create and refine a preliminary layout of that diagram first, by calling the preliminary layout steps tool and performing the work in strict accordance with the steps it returns. Do this after you have decided what the diagram is to show - the elements to appear on it and the relationships between them - and immediately before you begin working out where on the diagram its presentations are to be placed. Do not do it while what the diagram is to show is still undecided, and do not do it once you have begun deciding where a presentation goes. Whether the diagram itself has been created by then makes no difference to this timing. Lay the diagram out in accordance with the preliminary layout once that layout has been finalized.
* As part of drawing a new diagram, create a note in the upper-left corner of the diagram that describes what the diagram is meant to illustrate. This note describes the diagram as a whole rather than any particular element, so NEVER connect it to an element with a note anchor.
* As part of drawing a new diagram, insert an SVG illustrative graphic in the upper-right corner of the diagram that represents the content of the diagram.
* Immediately after creating a new class, interface, or enumeration element, describe the responsibility of that element in the element's definition field.
* When you place new elements on the diagram, as a final check, be sure to re-verify that the placement coordinates of the elements you placed are appropriate and properly aligned (e.g., centered alignment, top-edge alignment, etc.), and always make any necessary fine adjustments to the coordinates of the newly placed elements.  
* When creating a presentation on a diagram that corresponds to a model, you must provide not only the diagram information but also the information of the corresponding model. In contrast, when creating a presentation that is not associated with a model (such as notes), no corresponding model information is required.  
* Deleting a presentation does not remove the corresponding model. In contrast, deleting a model will also remove its corresponding presentation.  
* Association ends are attribute elements (member ends) of the association. Therefore, the information of association ends can be obtained through the information of the association.  
* The Astah that this MCP server references and edits is also edited by users. Therefore, assume that the project itself—and the model elements and presentations it contains—may be updated, and retrieve the latest information from Astah as needed. For example, a user may switch to a different Astah project, or make changes to model elements or presentations.  
* Object diagrams and package diagrams are substituted with class diagrams. This means that, for example, instance specifications and instance specification links are drawn on class diagrams.  
* Save the Astah project using tools only when the user explicitly instructs you to do so, or when explicitly instructed in Agent Skills.
  DO NOT save the Astah project on your own initiative.
* When new node/link presentations are placed on diagrams, adjust the layout of those presentations in accordance with the diagram layout guide and to avoid diagram layout anti-patterns.
* After you have finished editing the model elements and diagrams, and immediately before performing the final diagram layout check and adjustment, you MUST maintain terminology consistency across the names, labels, and definitions of model elements and diagrams.
* After you have finished editing the model elements and diagrams, and immediately before performing the final diagram layout check and adjustment, you MUST maintain the semantic consistency of the diagrams, eliminating semantic inconsistencies both within each diagram and across diagrams.
* Make sure to detect any overlaps caused by newly added node/link presentations using the tool and resolve them. However, NEVER take the incorrect approach of resolving overlaps by simply hiding the node/link presentations.
* Draw all node/link presentations related to the content intended to be represented in the diagram, without omission. In particular, be careful not to forget to draw any link presentations that should be included.
* You can place presentations at negative X or Y coordinates on the diagram. For example, a node/link presentation can be placed at (-100, -100).
* After you have finished editing the model elements and diagrams, you MUST review all definitions of the model elements and diagrams, and revise any definition text that is inconsistent with the corresponding model element or diagram.


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


Primitive Types:
The following types are defined by default in the Astah project, so you can use them without adding model elements for those types. Note that names are case-sensitive when using them.
%s
""".formatted(primitiveTypes);

        return new GuideDTO(contents);
    }
}
