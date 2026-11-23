package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;

import java.util.List;
import java.util.stream.Collectors;

// One relationship between two elements of a relationship matrix, as plain values.
// key tells relationships apart: the ID of the model element of the relationship, or for a containment or an implied relationship, which are no model elements, the IDs of their ends.
// id is the ID of the model element of the relationship, or null for a containment or an implied relationship.
// sourceId and targetId are the elements a relationship with a direction goes from and to; for any other relationship they are its two ends, in no particular order.
// kinds is usually one kind; a dependency with several requirement stereotypes has those of them that were asked for.
// implied tells a relationship that the model implies rather than holds, such as a satisfy of a class whose part satisfies the requirement.
// detail says more about the relationship in the list of relationships, such as the parts an implied relationship comes through; it is empty for most relationships.
record MatrixRelationship(
    String key,
    String id,
    String sourceId,
    String targetId,
    List<MatrixRelationshipKind> kinds,
    Orientation orientation,
    boolean implied,
    String detail
) {

    // Which way a relationship goes
    enum Orientation {
        // From the source to the target
        DIRECTED,
        // Both ways, such as an association navigable on both ends
        BIDIRECTIONAL,
        // Neither way, such as an association navigable on neither end
        UNDIRECTED
    }

    MatrixRelationship {
        kinds = List.copyOf(kinds);
        detail = detail == null ? "" : detail;
    }

    static MatrixRelationship directed(String key, String id, String sourceId, String targetId, List<MatrixRelationshipKind> kinds) {
        return new MatrixRelationship(key, id, sourceId, targetId, kinds, Orientation.DIRECTED, false, "");
    }

    static MatrixRelationship bidirectional(String id, String endId, String otherEndId, MatrixRelationshipKind kind) {
        return new MatrixRelationship(id, id, endId, otherEndId, List.of(kind), Orientation.BIDIRECTIONAL, false, "");
    }

    static MatrixRelationship undirected(String id, String endId, String otherEndId, MatrixRelationshipKind kind) {
        return new MatrixRelationship(id, id, endId, otherEndId, List.of(kind), Orientation.UNDIRECTED, false, "");
    }

    // A relationship the model implies from the source to the target, told apart from one the model holds by its key
    static MatrixRelationship implied(String sourceId, String targetId, MatrixRelationshipKind kind, String detail) {
        String key = "implied " + kind.label() + " " + sourceId + ">" + targetId;
        return new MatrixRelationship(key, null, sourceId, targetId, List.of(kind), Orientation.DIRECTED, true, detail);
    }

    // The kinds as a cell shows them, such as satisfy+trace, or satisfy (implied) for an implied relationship
    String label() {
        String label = kinds.stream()
                .map(MatrixRelationshipKind::label)
                .collect(Collectors.joining("+"));
        return implied ? label + " (implied)" : label;
    }
}
