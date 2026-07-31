package com.astahpromcp.tool.astah.pro.presentation.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record PresentationDTO(
        @JsonPropertyDescription("Presentation identifier")
        String id,

        @JsonPropertyDescription("Label text")
        String label,

        @JsonPropertyDescription("Diagram where this presentation is rendered")
        NameIdTypeDTO renderedInDiagram,

        @JsonPropertyDescription("Corresponding model element (named element)")
        NameIdTypeDTO correspondingModelElement,
        
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
        FRAME("Frame"),
        CLASS("Class"),
        STRUCTURED_CLASS("StructuredClass"),
        NEST("Containment"),
        NEST_SHARED_STYLE("ContainmentGroup"),
        ASSOCIATION("Association"),
        GENERALIZATION("Generalization"),
        GENERALIZATION_SHARED_STYLE("GeneralizationGroup"),
        REALIZATION("Realization"),
        DEPENDENCY("Dependency"),
        USAGE("Usage"),
        TEMPLATE_BINDING("TemplateBinding"),
        ASSOCIATION_CLASS("AssociationClass"),
        MODEL("Model"),
        PACKAGE("Package"),
        SUBSYSTEM("Subsystem"),
        PART("Part"),
        PORT("Port"),
        CONNECTOR("Connector"),
        INSTANCE_SPECIFICATION("InstanceSpecification"),
        LINK("Link"),
        NOTE("Note"),
        NOTE_ANCHOR("NoteAnchor"),
        TEXT("Text"),
        RECTANGLE("Rectangle"),
        OVAL("Oval"),
        IMAGE("Image"),
        LINE("Line"),
        FREE_HAND("FreeHand"),
        HIGHLIGHTER("Highlighter"),
        USE_CASE("UseCase"),
        EXTEND("Extend"),
        INCLUDE("Include"),
        INITIAL_PSEUDOSTATE("InitialPseudostate"),
        STATE("State"),
        FINAL_STATE("FinalState"),
        TRANSITION("Transition"),
        SHALLOW_HISTORY_PSEUDOSTATE("ShallowHistoryPseudostate"),
        DEEP_HISTORY_PSEUDOSTATE("DeepHistoryPseudostate"),
        JUNCTION_PSEUDOSTATE("JunctionPseudostate"),
        CHOICE_PSEUDOSTATE("ChoicePseudostate"),
        FORK_PSEUDOSTATE("ForkPseudostate"),
        JOIN_PSEUDOSTATE("JoinPseudostate"),
        STUB_STATE_IN_SUBMACHINE_STATE("StubState in SubmachineState"),
        SUBMACHINE_STATE("SubmachineState"),
        ENTRY_POINT_PSEUDOSTATE("EntryPointPseudostate"),
        EXIT_POINT_PSEUDOSTATE("ExitPointPseudostate"),
        PARTITION("Partition"),
        INITIAL_NODE("InitialNode"),
        ACTION("Action"),
        FLOW_FINAL_NODE("Flow Final Node"),
        ACCEPT_TIME_EVENT_ACTION("AcceptTimeEventAction"),
        INPUT_PIN("InputPin"),
        OUTPUT_PIN("OutputPin"),
        SEND_SIGNAL_ACTION("SendSignalAction"),
        ACCEPT_EVENT_ACTION("AcceptEventAction"),
        PROCESS("Process"),
        CALL_BEHAVIOR_ACTION("CallBehaviorAction"),
        ACTIVITY_FINAL("ActivityFinal"),
        CONTROL_FLOW_OR_OBJECT_FLOW("ControlFlow/ObjectFlow"),
        DECISION_NODE_AND_MERGE_NODE("Decision Node & Merge Node"),
        FORK_NODE("ForkNode"),
        JOIN_NODE("JoinNode"),
        OBJECT_NODE("ObjectNode"),
        ACTIVITY_PARAMETER_NODE("ActivityParameterNode"),
        LIFELINE("Lifeline"),
        ACTIVATION("Activation"),
        MESSAGE("Message"),
        TERMINATION("Termination"),
        INTERACTION_USE("InteractionUse"),
        COMBINED_FRAGMENT("CombinedFragment"),
        STATE_INVARIANT("StateInvariant"),
        DURATION_CONSTRAINT("DurationConstraint"),
        TIME_CONSTRAINT("TimeConstraint"),
        LANE("Lane"),
        FINAL_NODE("FinalNode"),
        CONDITION_JUDGEMENT("ConditionJudgement"),
        FORK("Fork"),
        JOIN("Join"),
        EXTERNAL_ENTITY("ExternalEntity"),
        PROCESS_BOX("ProcessBox"),
        DATA_STORE("DataStore"),
        ANCHOR("Anchor"),
        DATA_FLOW("DataFlow"),
        REQUIREMENT("Requirement"),
        TEST_CASE("TestCase"),
        DERIVE_REQT("DeriveReqt"),
        COPY("Copy"),
        SATISFY("Satisfy"),
        VERIFY("Verify"),
        REFINE("Refine"),
        TRACE("Trace"),
        ER_ENTITY("EREntity"),
        IDENTIFYING_RELATIONSHIP("Indentyfying-Relationship"),
        NON_IDENTIFYING_RELATIONSHIP("Non-Indentyfying-Relationship"),
        MANY_TO_MANY_RELATIONSHIP("Many-to-many-Relationship"),
        SUBTYPE("Subtype"),
        SUBTYPE_GROUP("SubtypeGroup"),
        TOPIC("Topic"),
        EDGE("Edge"),
        LINK_BETWEEN_TOPICS("MMLink"),
        BOUNDARY_IN_MINDMAP("MMBoundary"),
        VALUE_CELL_IN_CRUD("ValueCell"),
        HEADER_CELL_IN_CRUD("HeaderCell"),
        TOTAL_HEADER_CELL_IN_CRUD("TotalHeaderCell"),
        UNKNOWN("Unknown");
        
        public final String typeName;

        private Type(String typeName) {
            this.typeName = typeName;
        }

        public boolean matches(String typeName) {
            return this.typeName.equals(typeName);
        }

        public static Type getCorrespondingType(String typeName) {
            for (Type type : Type.values()) {
                if (type.matches(typeName)) {
                    return type;
                }
            }
            return Type.UNKNOWN;
        }
    }
}
