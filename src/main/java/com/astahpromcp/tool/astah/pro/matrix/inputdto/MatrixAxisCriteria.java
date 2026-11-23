package com.astahpromcp.tool.astah.pro.matrix.inputdto;

import java.util.List;

// The criteria of one axis of a relationship matrix, shared by the row and column records.
// The rows and the columns get a record each rather than sharing one, so that the input schema has no $defs.
public interface MatrixAxisCriteria {

    List<String> scopeIds();

    boolean includeNestedElements();

    List<String> elementTypes();

    List<String> stereotypes();
}
