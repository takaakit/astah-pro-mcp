package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.common.MatrixDirection;
import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.astahpromcp.tool.astah.pro.common.MatrixShowElements;

import java.util.List;

// The criteria of a relationship matrix as its Criteria section states them, with the scopes already resolved to names.
// rows and columns describe the criteria of each axis, such as: Requirement in Package "Requirements" (nested elements included)
// impliedRelationships tells whether the satisfy relationships that composition implies are shown, and groupedByPackage whether the rows and the columns are packages.
record MatrixCriteriaSummary(
    String rows,
    String columns,
    List<MatrixRelationshipKind> relationshipKinds,
    MatrixDirection direction,
    MatrixShowElements showElements,
    boolean impliedRelationships,
    boolean groupedByPackage
) {

    MatrixCriteriaSummary {
        relationshipKinds = List.copyOf(relationshipKinds);
    }

    // Criteria that show the relationships the model holds, between the elements themselves
    MatrixCriteriaSummary(
            String rows,
            String columns,
            List<MatrixRelationshipKind> relationshipKinds,
            MatrixDirection direction,
            MatrixShowElements showElements) {
        this(rows, columns, relationshipKinds, direction, showElements, false, false);
    }
}
