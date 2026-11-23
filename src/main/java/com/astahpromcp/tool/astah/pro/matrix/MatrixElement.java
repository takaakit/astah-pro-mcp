package com.astahpromcp.tool.astah.pro.matrix;

// One element listed as a row or a column of a relationship matrix
record MatrixElement(
    String id,
    String name,
    String namespace,                // The namespace with "." as the separator, empty for an element directly under the project
    String fullName,                 // The name with its namespace, which the rows and the columns are sorted by
    String type,                     // The type name of AstahElementType
    String requirementOrTestCaseId   // The requirement ID of a requirement or the test case ID of a test case, empty for any other element
) {

    static final String UNNAMED = "(unnamed)";

    // The name, or a mark that the element has none rather than nothing at all
    String displayName() {
        return name == null || name.isBlank() ? UNNAMED : name;
    }
}
