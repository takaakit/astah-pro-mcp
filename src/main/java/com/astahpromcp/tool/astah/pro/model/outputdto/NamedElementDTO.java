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
        List<NameIdTypeDTO> constraints
) {
    public enum Type {
        Action("Action", IAction.class, 4),
        Activity("Activity", IActivity.class, 3),
        ActivityDiagram("ActivityDiagram", IActivityDiagram.class, 4),
        ActivityNode("ActivityNode", IActivityNode.class, 3),
        ActivityParameterNode("ActivityParameterNode", IActivityParameterNode.class, 5),
        Anchor("Anchor", IAnchor.class, 4),
        Artifact("Artifact", IArtifact.class, 4),
        Association("Association", IAssociation.class, 3),
        AssociationClass("AssociationClass", IAssociationClass.class, 4),
        Attribute("Attribute", IAttribute.class, 3),
        Class("Class", IClass.class, 3),
        ClassDiagram("ClassDiagram", IClassDiagram.class, 4),
        ClassifierTemplateParameter("ClassifierTemplateParameter", IClassifierTemplateParameter.class, 3),
        CombinedFragment("CombinedFragment", ICombinedFragment.class, 4),
        Comment("Comment", IComment.class, 3),
        CommunicationDiagram("CommunicationDiagram", ICommunicationDiagram.class, 4),
        Component("Component", IComponent.class, 4),
        ComponentDiagram("ComponentDiagram", IComponentDiagram.class, 4),
        CompositeStructureDiagram("CompositeStructureDiagram", ICompositeStructureDiagram.class, 4),
        Connector("Connector", IConnector.class, 3),
        Constraint("Constraint", IConstraint.class, 3),
        ControlNode("ControlNode", IControlNode.class, 4),
        DataFlow("DataFlow", IDataFlow.class, 3),
        DataFlowDiagram("DataFlowDiagram", IDataFlowDiagram.class, 4),
        DataFlowNode("DataFlowNode", IDataFlowNode.class, 3),
        DataStore("DataStore", IDataStore.class, 4),
        Dependency("Dependency", IDependency.class, 3),
        DeploymentDiagram("DeploymentDiagram", IDeploymentDiagram.class, 4),
        Diagram("Diagram", IDiagram.class, 3),
        DurationConstraint("DurationConstraint", IDurationConstraint.class, 4),
        Element("Element", IElement.class, 1),
        Entity("Entity", IEntity.class, 0),
        Enumeration("Enumeration", IEnumeration.class, 4),
        EnumerationLiteral("EnumerationLiteral", IEnumerationLiteral.class, 4),
        ERAttribute("ERAttribute", IERAttribute.class, 3),
        ERDatatype("ERDatatype", IERDatatype.class, 3),
        ERDiagram("ERDiagram", IERDiagram.class, 4),
        ERDomain("ERDomain", IERDomain.class, 3),
        EREntity("EREntity", IEREntity.class, 3),
        ERIndex("ERIndex", IERIndex.class, 3),
        ERModel("ERModel", IERModel.class, 5),
        ERPackage("ERPackage", IERPackage.class, 4),
        ERRelationship("ERRelationship", IERRelationship.class, 3),
        ERSchema("ERSchema", IERSchema.class, 5),
        ERSubtypeRelationship("ERSubtypeRelationship", IERSubtypeRelationship.class, 3),
        Extend("Extend", IExtend.class, 3),
        ExtentionPoint("ExtentionPoint", IExtentionPoint.class, 3),
        ExternalEntity("ExternalEntity", IExternalEntity.class, 4),
        FinalState("FinalState", IFinalState.class, 5),
        Flow("Flow", IFlow.class, 3),
        Gate("Gate", IGate.class, 3),
        Generalization("Generalization", IGeneralization.class, 3),
        Hyperlink("Hyperlink", IHyperlink.class, 0),
        HyperlinkOwner("HyperlinkOwner", IHyperlinkOwner.class, 0),
        Include("Include", IInclude.class, 3),
        InputPin("InputPin", IInputPin.class, 6),
        InstanceSpecification("InstanceSpecification", IInstanceSpecification.class, 3),
        Interaction("Interaction", IInteraction.class, 3),
        InteractionFragment("InteractionFragment", IInteractionFragment.class, 3),
        InteractionOperand("InteractionOperand", IInteractionOperand.class, 3),
        InteractionUse("InteractionUse", IInteractionUse.class, 4),
        Lifeline("Lifeline", ILifeline.class, 3),
        LifelineLink("LifelineLink", ILifelineLink.class, 3),
        Link("Link", ILink.class, 3),
        LinkEnd("LinkEnd", ILinkEnd.class, 3),
        MatrixDiagram("MatrixDiagram", IMatrixDiagram.class, 4),
        Message("Message", IMessage.class, 3),
        MindMapDiagram("MindMapDiagram", IMindMapDiagram.class, 4),
        Model("Model", IModel.class, 4),
        MultiplicityRange("MultiplicityRange", IMultiplicityRange.class, 0),
        NamedElement("NamedElement", INamedElement.class, 2),
        Node("Node", INode.class, 4),
        ObjectNode("ObjectNode", IObjectNode.class, 4),
        Operation("Operation", IOperation.class, 3),
        OutputPin("OutputPin", IOutputPin.class, 6),
        Package("Package", IPackage.class, 3),
        Parameter("Parameter", IParameter.class, 3),
        Partition("Partition", IPartition.class, 3),
        Pin("Pin", IPin.class, 5),
        Port("Port", IPort.class, 4),
        ProcessBox("ProcessBox", IProcessBox.class, 4),
        Pseudostate("Pseudostate", IPseudostate.class, 4),
        Realization("Realization", IRealization.class, 3),
        Requirement("Requirement", IRequirement.class, 4),
        RequirementDiagram("RequirementDiagram", IRequirementDiagram.class, 4),
        RequirementTable("RequirementTable", IRequirementTable.class, 4),
        SequenceDiagram("SequenceDiagram", ISequenceDiagram.class, 4),
        Slot("Slot", ISlot.class, 3),
        State("State", IState.class, 4),
        StateInvariant("StateInvariant", IStateInvariant.class, 4),
        StateMachine("StateMachine", IStateMachine.class, 3),
        StateMachineDiagram("StateMachineDiagram", IStateMachineDiagram.class, 4),
        Subsystem("Subsystem", ISubsystem.class, 4),
        TaggedValue("TaggedValue", ITaggedValue.class, 2),
        TemplateBinding("TemplateBinding", ITemplateBinding.class, 3),
        Termination("Termination", ITermination.class, 4),
        TestCase("TestCase", ITestCase.class, 4),
        TimeConstraint("TimeConstraint", ITimeConstraint.class, 4),
        TimingDiagram("TimingDiagram", ITimingDiagram.class, 4),
        TraceabilityMap("TraceabilityMap", ITraceabilityMap.class, 5),
        Transition("Transition", ITransition.class, 3),
        Usage("Usage", IUsage.class, 3),
        UseCase("UseCase", IUseCase.class, 4),
        UseCaseDiagram("UseCaseDiagram", IUseCaseDiagram.class, 4),
        Vertex("Vertex", IVertex.class, 3),
        Unknown("Unknown", null, -1);

        private final String typeName;
        private final Class typeClass;
        private final int hierarchyDepth;

        private Type(String typeName, Class typeClass, int hierarchyDepth) {
            this.typeName = typeName;
            this.typeClass = typeClass;
            this.hierarchyDepth = hierarchyDepth;
        }

        public String getTypeName() {
            return typeName;
        }

        public Class getTypeClass() {
            return typeClass;
        }

        public static Type getCorrespondingType(Object object) {
            // Return the deepest classifier that matches the Type
            Type bestMatch = Type.Unknown;
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
            return Type.Unknown;
        }
    }
}
