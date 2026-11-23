package com.astahpromcp.tool.astah.pro.matrix.inputdto;

import com.astahpromcp.tool.astah.pro.common.MatrixDirection;
import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.astahpromcp.tool.astah.pro.common.MatrixShowElements;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record RelationshipMatrixDTO(
    @JsonPropertyDescription("Criteria for the elements listed as rows")
    MatrixRowCriteriaDTO rows,

    @JsonPropertyDescription("Criteria for the elements listed as columns")
    MatrixColumnCriteriaDTO columns,

    @JsonPropertyDescription("Kinds of relationship to show. Specify at least one. satisfy, verify, refine, trace, derive_reqt and copy are the dependencies with that stereotype, and dependency is every other dependency. transition joins states and pseudostates, flow activity nodes, data_flow the processes, data stores and external entities of a data flow diagram, er_relationship ER entities, connector parts and ports, and link instance specifications: those ends are rows and columns only when elementTypes names their types.")
    List<MatrixRelationshipKind> relationshipKinds,

    @JsonPropertyDescription("Direction of the relationships to show: from a row element to a column element, from a column element to a row element, or both. A relationship with no direction, such as an association navigable on neither end, which is what an association has by default, and one with both directions are shown whatever this is.")
    MatrixDirection direction,

    @JsonPropertyDescription("Which row and column elements to list, by whether they have a relationship that matches the other criteria. To find the row elements that have none, use rows_without_relationships: every column element is listed as well, with its ID, so that you can relate a row element to one of them.")
    MatrixShowElements showElements,

    @JsonPropertyDescription("Whether to also show the satisfy relationships that composition implies: a class whose part, an element it holds by composition however deeply, satisfies a requirement is taken to satisfy that requirement too. Such a relationship shows as satisfy (implied) and has no ID, being no model element; the part it comes through needs to be neither a row nor a column. Has an effect only when relationshipKinds holds satisfy.")
    boolean includeImpliedRelationships,

    @JsonPropertyDescription("Whether to show how the packages that hold the chosen elements relate, instead of the elements themselves, as a dependency structure matrix does: each row and column is the nearest package, subsystem or the project itself that holds a chosen element, and a cell counts the relationships between the elements of its two packages. A relationship within one package is left out. An element without an owner, such as a state or an action, belongs to the package of the diagram it is drawn on.")
    boolean groupByPackage,

    @JsonPropertyDescription("Whether to append a table of the relationships between the listed elements, each with its ID, for example to delete one.")
    boolean listRelationships
) {
}
