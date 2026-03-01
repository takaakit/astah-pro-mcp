package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.VisibilityKind;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.change_vision.jude.api.inf.model.*;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NamedElementDTO(
        @JsonPropertyDescription("Element information")
        ElementDTO element,

        @JsonPropertyDescription("Element type")
        String type,

        @JsonPropertyDescription("Element name")
        String name,

        @JsonPropertyDescription("Full namespace")
        String nameSpace,

        @JsonPropertyDescription("Visibility")
        VisibilityKind visibility,

        @JsonPropertyDescription("Alias 1")
        String alias1,

        @JsonPropertyDescription("Alias 2")
        String alias2,

        @JsonPropertyDescription("Definition")
        String definition,

        @JsonPropertyDescription("Client dependencies: Elements that this element depends on.")
        List<DependencyDTO> clientDependencies,

        @JsonPropertyDescription("Supplier dependencies: Elements that this element is depended on.")
        List<DependencyDTO> supplierDependencies,

        @JsonPropertyDescription("Client realizations: Elements that this element realizes.")
        List<RealizationDTO> clientRealizations,

        @JsonPropertyDescription("Supplier realizations: Elements that this element is realized by.")
        List<RealizationDTO> supplierRealizations,

        @JsonPropertyDescription("Client usages: Elements that this element is used by.")
        List<UsageDTO> clientUsages,
        
        @JsonPropertyDescription("Supplier usages: Elements that use this element.")
        List<UsageDTO> supplierUsages,

        @JsonPropertyDescription("Diagrams where this element is rendered")
        List<NameIdTypeDTO> renderedInDiagrams,

        @JsonPropertyDescription("Constraints")
        List<NameIdTypeDTO> constraints,

        @JsonPropertyDescription("Hyperlinks to URL")
        List<UrlHyperlinkDTO> urlHyperlinks,

        @JsonPropertyDescription("Hyperlinks to file path")
        List<FilePathHyperlinkDTO> filePathHyperlinks,

        @JsonPropertyDescription("Hyperlinks to named element")
        List<NamedElementHyperlinkDTO> namedElementHyperlinks
) {
    public enum Type {
        ACTION("Action", IAction.class, 4),
        ACTIVITY("Activity", IActivity.class, 3),
        ACTIVITY_DIAGRAM("ActivityDiagram", IActivityDiagram.class, 4),
        ACTIVITY_NODE("ActivityNode", IActivityNode.class, 3),
        ACTIVITY_PARAMETER_NODE("ActivityParameterNode", IActivityParameterNode.class, 5),
        ANCHOR("Anchor", IAnchor.class, 4),
        ARTIFACT("Artifact", IArtifact.class, 4),
        ASSOCIATION("Association", IAssociation.class, 3),
        ASSOCIATION_CLASS("AssociationClass", IAssociationClass.class, 4),
        ATTRIBUTE("Attribute", IAttribute.class, 3),
        CLASS("Class", IClass.class, 3),
        CLASS_DIAGRAM("ClassDiagram", IClassDiagram.class, 4),
        CLASSIFIER_TEMPLATE_PARAMETER("ClassifierTemplateParameter", IClassifierTemplateParameter.class, 3),
        COMBINED_FRAGMENT("CombinedFragment", ICombinedFragment.class, 4),
        COMMENT("Comment", IComment.class, 3),
        COMMUNICATION_DIAGRAM("CommunicationDiagram", ICommunicationDiagram.class, 4),
        COMPONENT("Component", IComponent.class, 4),
        COMPONENT_DIAGRAM("ComponentDiagram", IComponentDiagram.class, 4),
        COMPOSITE_STRUCTURE_DIAGRAM("CompositeStructureDiagram", ICompositeStructureDiagram.class, 4),
        CONNECTOR("Connector", IConnector.class, 3),
        CONSTRAINT("Constraint", IConstraint.class, 3),
        CONTROL_NODE("ControlNode", IControlNode.class, 4),
        DATA_FLOW("DataFlow", IDataFlow.class, 3),
        DATA_FLOW_DIAGRAM("DataFlowDiagram", IDataFlowDiagram.class, 4),
        DATA_FLOW_NODE("DataFlowNode", IDataFlowNode.class, 3),
        DATA_STORE("DataStore", IDataStore.class, 4),
        DEPENDENCY("Dependency", IDependency.class, 3),
        DEPLOYMENT_DIAGRAM("DeploymentDiagram", IDeploymentDiagram.class, 4),
        DIAGRAM("Diagram", IDiagram.class, 3),
        DURATION_CONSTRAINT("DurationConstraint", IDurationConstraint.class, 4),
        ELEMENT("Element", IElement.class, 1),
        ENTITY("Entity", IEntity.class, 0),
        ENUMERATION("Enumeration", IEnumeration.class, 4),
        ENUMERATION_LITERAL("EnumerationLiteral", IEnumerationLiteral.class, 4),
        ER_ATTRIBUTE("ERAttribute", IERAttribute.class, 3),
        ER_DATATYPE("ERDatatype", IERDatatype.class, 3),
        ER_DIAGRAM("ERDiagram", IERDiagram.class, 4),
        ER_DOMAIN("ERDomain", IERDomain.class, 3),
        ER_ENTITY("EREntity", IEREntity.class, 3),
        ER_INDEX("ERIndex", IERIndex.class, 3),
        ER_MODEL("ERModel", IERModel.class, 5),
        ER_PACKAGE("ERPackage", IERPackage.class, 4),
        ER_RELATIONSHIP("ERRelationship", IERRelationship.class, 3),
        ER_SCHEMA("ERSchema", IERSchema.class, 5),
        ER_SUBTYPE_RELATIONSHIP("ERSubtypeRelationship", IERSubtypeRelationship.class, 3),
        EXTEND("Extend", IExtend.class, 3),
        EXTENTION_POINT("ExtentionPoint", IExtentionPoint.class, 3),
        EXTERNAL_ENTITY("ExternalEntity", IExternalEntity.class, 4),
        FINAL_STATE("FinalState", IFinalState.class, 5),
        FLOW("Flow", IFlow.class, 3),
        GATE("Gate", IGate.class, 3),
        GENERALIZATION("Generalization", IGeneralization.class, 3),
        INCLUDE("Include", IInclude.class, 3),
        INPUT_PIN("InputPin", IInputPin.class, 6),
        INSTANCE_SPECIFICATION("InstanceSpecification", IInstanceSpecification.class, 3),
        INTERACTION("Interaction", IInteraction.class, 3),
        INTERACTION_FRAGMENT("InteractionFragment", IInteractionFragment.class, 3),
        INTERACTION_OPERAND("InteractionOperand", IInteractionOperand.class, 3),
        INTERACTION_USE("InteractionUse", IInteractionUse.class, 4),
        LIFELINE("Lifeline", ILifeline.class, 3),
        LIFELINE_LINK("LifelineLink", ILifelineLink.class, 3),
        LINK("Link", ILink.class, 3),
        LINK_END("LinkEnd", ILinkEnd.class, 3),
        MATRIX_DIAGRAM("MatrixDiagram", IMatrixDiagram.class, 4),
        MESSAGE("Message", IMessage.class, 3),
        MIND_MAP_DIAGRAM("MindMapDiagram", IMindMapDiagram.class, 4),
        MODEL("Model", IModel.class, 4),
        MULTIPLICITY_RANGE("MultiplicityRange", IMultiplicityRange.class, 0),
        NAMED_ELEMENT("NamedElement", INamedElement.class, 2),
        NODE("Node", INode.class, 4),
        OBJECT_NODE("ObjectNode", IObjectNode.class, 4),
        OPERATION("Operation", IOperation.class, 3),
        OUTPUT_PIN("OutputPin", IOutputPin.class, 6),
        PACKAGE("Package", IPackage.class, 3),
        PARAMETER("Parameter", IParameter.class, 3),
        PARTITION("Partition", IPartition.class, 3),
        PIN("Pin", IPin.class, 5),
        PORT("Port", IPort.class, 4),
        PROCESS_BOX("ProcessBox", IProcessBox.class, 4),
        PSEUDOSTATE("Pseudostate", IPseudostate.class, 4),
        REALIZATION("Realization", IRealization.class, 3),
        REQUIREMENT("Requirement", IRequirement.class, 4),
        REQUIREMENT_DIAGRAM("RequirementDiagram", IRequirementDiagram.class, 4),
        REQUIREMENT_TABLE("RequirementTable", IRequirementTable.class, 4),
        SEQUENCE_DIAGRAM("SequenceDiagram", ISequenceDiagram.class, 4),
        SLOT("Slot", ISlot.class, 3),
        STATE("State", IState.class, 4),
        STATE_INVARIANT("StateInvariant", IStateInvariant.class, 4),
        STATE_MACHINE("StateMachine", IStateMachine.class, 3),
        STATE_MACHINE_DIAGRAM("StateMachineDiagram", IStateMachineDiagram.class, 4),
        SUBSYSTEM("Subsystem", ISubsystem.class, 4),
        TAGGED_VALUE("TaggedValue", ITaggedValue.class, 2),
        TEMPLATE_BINDING("TemplateBinding", ITemplateBinding.class, 3),
        TERMINATION("Termination", ITermination.class, 4),
        TEST_CASE("TestCase", ITestCase.class, 4),
        TIME_CONSTRAINT("TimeConstraint", ITimeConstraint.class, 4),
        TIMING_DIAGRAM("TimingDiagram", ITimingDiagram.class, 4),
        TRACEABILITY_MAP("TraceabilityMap", ITraceabilityMap.class, 5),
        TRANSITION("Transition", ITransition.class, 3),
        USAGE("Usage", IUsage.class, 3),
        USE_CASE("UseCase", IUseCase.class, 4),
        USE_CASE_DIAGRAM("UseCaseDiagram", IUseCaseDiagram.class, 4),
        VERTEX("Vertex", IVertex.class, 3),
        UNKNOWN("Unknown", null, -1);

        public final String typeName;
        public final Class typeClass;
        private final int hierarchyDepth;

        private Type(String typeName, Class typeClass, int hierarchyDepth) {
            this.typeName = typeName;
            this.typeClass = typeClass;
            this.hierarchyDepth = hierarchyDepth;
        }

        public static Type getCorrespondingType(Object object) {
            // Return the deepest classifier that matches the Type
            Type bestMatch = Type.UNKNOWN;
            for (Type type : Type.values()) {
                if (type.typeClass != null && type.typeClass.isInstance(object)) {
                    if (type.hierarchyDepth > bestMatch.hierarchyDepth) {
                        bestMatch = type;
                    }
                }
            }
            return bestMatch;
        }

        public static Type getCorrespondingType(String typeName) {
            for (Type type : Type.values()) {
                if (typeName.equals(type.typeName)) {
                    return type;
                }
            }
            return Type.UNKNOWN;
        }
    }
}
