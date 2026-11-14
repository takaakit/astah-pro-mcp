package com.astahpromcp.tool.knowledge.document;

import java.util.Arrays;
import java.util.stream.Collectors;

// The texts that tell the MCP client which reference documents it can read and when
public final class ReferenceDocuments {
    
    private ReferenceDocuments() {
    }
    
    // One line per document: the value to name in 'documentType', the name of the document and when to read it
    public static String bulletListOfAll() {
        return Arrays.stream(DocumentType.values())
                .map(type -> "* '" + type.value() + "': " + type.title() + ". " + type.usage())
                .collect(Collectors.joining("\n"));
    }
}
