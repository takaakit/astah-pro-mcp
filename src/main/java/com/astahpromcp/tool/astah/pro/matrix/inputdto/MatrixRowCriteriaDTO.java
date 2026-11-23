package com.astahpromcp.tool.astah.pro.matrix.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record MatrixRowCriteriaDTO(
    @JsonPropertyDescription("IDs of the packages, diagrams or elements that bound the rows. A package contributes the elements it owns, a diagram the elements drawn on it, and any other element itself. A diagram does not contribute the class of a lifeline, a part or an instance specification drawn on it, so for the classes of a sequence or composite structure diagram, specify their package or the classes themselves. An element without an owner, such as a state, an action or a process of a data flow diagram, is taken to belong where the diagram it is drawn on belongs. Specify an empty list to search the whole project.")
    List<String> scopeIds,

    @JsonPropertyDescription("Whether to also take the elements nested more deeply under a package or an element in scopeIds: those in its subpackages and those nested in its elements, such as requirements nested under a requirement. Ignored for a diagram, and when scopeIds is empty, where the whole project is always searched.")
    boolean includeNestedElements,

    @JsonPropertyDescription("Element types to take, each compared exactly. Specify an empty list to take the default types Class, Requirement, TestCase, UseCase, Component, Node, Artifact and Enumeration. The other types are taken only when named: Package and Subsystem; State, FinalState and Pseudostate for transitions; Action, ControlNode, ObjectNode, ActivityParameterNode, InputPin and OutputPin for flows; Attribute (a part) and Port for connectors; InstanceSpecification for links; ProcessBox, DataStore and ExternalEntity for data flows; EREntity for ER relationships. An interface, an actor and a block are classes with a stereotype, so specify Class and put the stereotype in stereotypes.")
    List<String> elementTypes,

    @JsonPropertyDescription("Take only the elements that have at least one of these stereotypes, compared case-sensitively, such as block, interface or actor. Specify an empty list not to filter by stereotype.")
    List<String> stereotypes
) implements MatrixAxisCriteria {
}
