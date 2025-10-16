package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PresentationDTO(
        @JsonPropertyDescription("Presentation identifier")
        String id,

        @JsonPropertyDescription("Label text")
        String label,

        @JsonPropertyDescription("Diagram identifier where this presentation is rendered")
        String renderedInDiagramId,

        @JsonPropertyDescription("Corresponding model element identifier")
        String correspondingElementId,
        
        @JsonPropertyDescription("Presentation type name")
        String type,

        @JsonPropertyDescription("Presentation fill color")
        String fillColor,

        @JsonPropertyDescription("Presentation line color")
        String lineColor,

        @JsonPropertyDescription("Presentation font color")
        String fontColor
) {
    public enum Type {
        Frame("Frame"),
        Class("Class"),
        StructuredClass("StructuredClass"),
        Nest("Containment"),
        Nest_Shared_Style("ContainmentGroup"),
        Association("Association"),
        Generalization("Generalization"),
        Generalization_Shared_Style("GeneralizationGroup"),
        Realization("Realization"),
        Dependency("Dependency"),
        Usage("Usage"),
        TemplateBinding("TemplateBinding"),
        AssociationClass("AssociationClass"),
        Model("Model"),
        Package("Package"),
        Subsystem("Subsystem"),
        Part("Part"),
        Port("Port"),
        Connector("Connector"),
        InstanceSpecification("InstanceSpecification"),
        Link("Link"),
        Note("Note"),
        NoteAnchor("NoteAnchor"),
        Text("Text"),
        Rectangle("Rectangle"),
        Oval("Oval"),
        Image("Image"),
        Line("Line"),
        FreeHand("FreeHand"),
        Highlighter("Highlighter"),
        UseCase("UseCase"),
        Extend("Extend"),
        Include("Include"),
        InitialPseudostate("InitialPseudostate"),
        State("State"),
        FinalState("FinalState"),
        Transition("Transition"),
        ShallowHistoryPseudostate("ShallowHistoryPseudostate"),
        DeepHistoryPseudostate("DeepHistoryPseudostate"),
        JunctionPseudostate("JunctionPseudostate"),
        ChoicePseudostate("ChoicePseudostate"),
        ForkPseudostate("ForkPseudostate"),
        JoinPseudostate("JoinPseudostate"),
        StubState_in_SubmachineState("StubState in SubmachineState"),
        SubmachineState("SubmachineState"),
        EntryPointPseudostate("EntryPointPseudostate"),
        ExitPointPseudostate("ExitPointPseudostate"),
        Partition("Partition"),
        InitialNode("InitialNode"),
        Action("Action"),
        Flow_Final_Node("Flow Final Node"),
        AcceptTimeEventAction("AcceptTimeEventAction"),
        InputPin("InputPin"),
        OutputPin("OutputPin"),
        SendSignalAction("SendSignalAction"),
        AcceptEventAction("AcceptEventAction"),
        Process("Process"),
        CallBehaviorAction("CallBehaviorAction"),
        ActivityFinal("ActivityFinal"),
        ControlFlow_or_ObjectFlow("ControlFlow/ObjectFlow"),
        Decision_Node_and_Merge_Node("Decision Node & Merge Node"),
        ForkNode("ForkNode"),
        JoinNode("JoinNode"),
        ObjectNode("ObjectNode"),
        ActivityParameterNode("ActivityParameterNode"),
        Lifeline("Lifeline"),
        Activation("Activation"),
        Message("Message"),
        Termination("Termination"),
        InteractionUse("InteractionUse"),
        CombinedFragment("CombinedFragment"),
        StateInvariant("StateInvariant"),
        DurationConstraint("DurationConstraint"),
        TimeConstraint("TimeConstraint"),
        Lane("Lane"),
        FinalNode("FinalNode"),
        ConditionJudgement("ConditionJudgement"),
        Fork("Fork"),
        Join("Join"),
        ExternalEntity("ExternalEntity"),
        ProcessBox("ProcessBox"),
        DataStore("DataStore"),
        Anchor("Anchor"),
        DataFlow("DataFlow"),
        Requirement("Requirement"),
        TestCase("TestCase"),
        DeriveReqt("DeriveReqt"),
        Copy("Copy"),
        Satisfy("Satisfy"),
        Verify("Verify"),
        Refine("Refine"),
        Trace("Trace"),
        EREntity("EREntity"),
        Identifying_Relationship("Indentyfying-Relationship"),
        Non_Identifying_Relationship("Non-Indentyfying-Relationship"),
        Many_to_many_Relationship("Many-to-many-Relationship"),
        Subtype("Subtype"),
        SubtypeGroup("SubtypeGroup"),
        Topic("Topic"),
        Edge("Edge"),
        Link_between_Topics("MMLink"),
        Boundary_in_Mindmap("MMBoundary"),
        Value_Cell_in_CRUD("ValueCell"),
        Header_Cell_in_CRUD("HeaderCell"),
        Total_Header_Cell_in_CRUD("TotalHeaderCell"),
        Unknown("Unknown");
        
        public final String typeName;

        private Type(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        public static Type getCorrespondingType(String typeName) {
            for (Type type : Type.values()) {
                if (typeName.equals(type.getTypeName())) {
                    return type;
                }
            }
            return Type.Unknown;
        }
    }
}
