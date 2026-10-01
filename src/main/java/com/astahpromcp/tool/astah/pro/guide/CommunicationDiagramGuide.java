package com.astahpromcp.tool.astah.pro.guide;

final class CommunicationDiagramGuide {

    private CommunicationDiagramGuide() {
    }

    static String contents() {
        String contents = """
IMPORTANT POINTS to Keep in Mind:
* For communication-diagram-related tool functions, only read-only (viewing) operations are provided; editing operations are not available.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* Communication Diagrams focus on the interaction between Lifelines where the architecture of the internal structure and how this corresponds with the message passing is central.
* Communication Diagrams correspond to simple Sequence Diagrams that use none of the structuring mechanisms such as InteractionUses and CombinedFragments.
* The sequencing of Messages is given through a sequence numbering scheme.
        """;
        
        return contents;
    }
}
