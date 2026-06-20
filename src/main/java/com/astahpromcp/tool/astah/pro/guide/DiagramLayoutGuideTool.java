package com.astahpromcp.tool.astah.pro.guide;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiagramLayoutGuideTool implements ToolProvider {

    private static final String CLASS_IMAGE_RESOURCE = "/diagram-layout-rules/class-diagram-layout-rules.png";
    private static final String SEQUENCE_IMAGE_RESOURCE = "/diagram-layout-rules/sequence-diagram-layout-rules.png";
    private static final String ACTIVITY_IMAGE_RESOURCE = "/diagram-layout-rules/activity-diagram-layout-rules.png";
    private static final String STATE_MACHINE_IMAGE_RESOURCE = "/diagram-layout-rules/state-machine-diagram-layout-rules.png";
    private static final String REQUIREMENT_IMAGE_RESOURCE = "/diagram-layout-rules/requirement-diagram-layout-rules.png";

    private static final String CLASS_ANTI_PATTERNS_IMAGE_RESOURCE = "/diagram-layout-anti-patterns/class-diagram-layout-anti-patterns.png";
    private static final String SEQUENCE_ANTI_PATTERNS_IMAGE_RESOURCE = "/diagram-layout-anti-patterns/sequence-diagram-layout-anti-patterns.png";
    private static final String ACTIVITY_ANTI_PATTERNS_IMAGE_RESOURCE = "/diagram-layout-anti-patterns/activity-diagram-layout-anti-patterns.png";
    private static final String STATE_MACHINE_ANTI_PATTERNS_IMAGE_RESOURCE = "/diagram-layout-anti-patterns/state-machine-diagram-layout-anti-patterns.png";

    public DiagramLayoutGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.toolDefinitionReturningContents(
                "dgm_layout_guide",
                "MCP client (you) MUST call this tool function before aligning diagram layout, and then lay out the diagram in strict accordance with this layout guide.",
                this::getGuide,
                NoInputDTO.class),
                
            ToolSupport.toolDefinitionReturningContents(
                "dgm_layout_anti_patterns",
                "MCP client (you) MUST call this tool function before aligning diagram layout to learn the layout anti-patterns, and then lay out the diagram so as to avoid these anti-patterns.",
                this::getAntiPatterns,
                NoInputDTO.class)
        );
    }

    private List<McpSchema.Content> getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get diagram layout guide: {}", param);

        String content = """
Common Layout Rules:
* Adjust the layout of any newly placed node/link presentations so that they always conform to the rules below. On the other hand, for node/link presentations that have already been placed, adjust the layout so that they conform to the rules below only when the user has explicitly instructed you to make layout adjustments.
* After creating or editing presentations on a diagram, the tool output data includes an image of the edited diagram. Therefore, you must review the contents of that diagram image each time and self-check whether the presentations you created or edited comply with this diagram layout guide. If they do not comply, immediately revise them. You must also feed the findings back into the subsequent drawing plan and revise that plan accordingly.
* When node/link presentations are created on a diagram, presentations created later are displayed in front of earlier ones. Depending on the creation order, one presentation may be hidden behind another. Therefore, after creating presentations on a diagram, check the diagram image for any hidden presentations. If any are found, move them to the front so they are not hidden.


Class Diagram Layout Rules:
* Node presentations are aligned to minimize the number of intersecting link presentations. Because crossing one link presentation with another can cause confusion.
* For groups of sub classes that share the same super class, the top edges of the sub-class node presentations are aligned horizontally whenever possible. This arrangement helps user easily recognize the sub-class groupings.
* Each inheritance link presentation uses a right-angle line that starts from the top edge of the sub-class node presentation and ends at the bottom edge of the super-class node presentation.
* The positions of the node presentations are adjusted so that, whenever possible, the arrow direction of inheritance and realization link presentations goes from bottom to top.
* The positions of the node presentations are adjusted so that, whenever possible, the arrow direction of relationship and dependency link presentations goes from top to bottom or from left to right.
* The positions are adjusted so that node presentation rectangles never overlap, except when one node presentation contains the other. For example, it is acceptable for the rectangle of a package to overlap with the rectangle of a class contained within that package, but it is not acceptable for rectangles of classes contained within the same package to overlap.
* If there are multiple link presentations between two node presentations, the paths of those link presentations do not overlap.
* For each class diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


Sequence Diagram Layout Rules:
* If an Activation (ExecutionSpecification) is specified as the source when creating the message, the message is created with the existing Activation (ExecutionSpecification) as its source. On the other hand, if a lifeline is specified as the source when creating the message, the message is created with a new Activation (ExecutionSpecification) as its source.
* Lifeline rectangles never overlap with other lifeline rectangles. If lifeline rectangles would overlap, the lifeline X coordinates are adjusted to avoid the overlap.
* Messages never overlap with other messages. If messages would overlap, the message Y coordinates are adjusted to avoid the overlap.
* For each sequence diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.
* The position and size of each operand rectangle are adjusted so that it is large enough to contain the presentations that should be placed inside it.
* Tip: When the position or size of an operand is changed, the positions of the presentations placed inside it also move accordingly. Therefore, it is usually easier to achieve the expected layout by first adjusting the positions and sizes of the operands, and then placing the presentations inside them.


Activity Diagram Layout Rules:
* The node-presentation positions are adjusted so that the axes of the initial node, action node, and finish node in the same flow are aligned in a straight line either vertically or horizontally, making it easier to recognize that those nodes belong to the same flow.
* The positions of the node presentations are adjusted so that, whenever possible, the arrow direction of link presentations goes from top to bottom or from left to right.
* The positions are adjusted so that node presentations inside a partition do not touch the partition edges and have some margin around them.
* The positions are adjusted so that node presentation rectangles never overlap, except when one node presentation contains the other. For example, it is acceptable for the rectangle of a partition to overlap with the rectangle of an action contained within that partition, but it is not acceptable for rectangles of actions contained within the same partition to overlap.
* For each activity diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.
* The size of each partition rectangle are adjusted so that it is large enough to contain the presentations that should be placed inside it.
* Tip: When the size of a partition is changed, the positions of the presentations placed inside it also move accordingly. Therefore, it is usually easier to achieve the expected layout by first adjusting the sizes of the partitions, and then placing the presentations inside them.


State Machine Diagram Layout Rules:
* The line style for each transition link presentation is a curved style.
* The positions are adjusted so that node presentation rectangles never overlap, except when one node presentation contains the other. For example, it is acceptable for the rectangle of a super state to overlap with the rectangle of a sub state contained within that super state, but it is not acceptable for rectangles of sub states contained within the same super state to overlap.
* If there are multiple link presentations between two node presentations, the paths of those link presentations do not overlap.
* For each state machine diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.
* The size of each region rectangle are adjusted so that it is large enough to contain the presentations that should be placed inside it.
* Tip: When the size of a region is changed, the positions of the presentations placed inside it also move accordingly. Therefore, it is usually easier to achieve the expected layout by first adjusting the sizes of the regions, and then placing the presentations inside them.


Requirement Diagram Layout Rules:
* When requirements have a parent-child relationship, the positions of the requirement node presentations are adjusted so that the parent requirement is placed above the child requirement.
* The positions of element node presentations, such as requirements, classes, and use cases, are adjusted so that the arrow direction of the relationship link presentations listed below goes from bottom to top.
  * <<deriveReqt>>
  * <<satisfy>>
  * <<verify>>
  * <<refine>>


Required adjustments after layout:
* Detect overlaps involving any newly placed node or link presentation using the tool, and adjust either the positions of the node presentations or the paths of the link presentations to eliminate the detected overlaps. However, if a particular overlap issue is not resolved after three consecutive layout adjustment attempts, give up on resolving that overlap and move on to resolving a different overlap issue.
        """;

        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(McpSchema.TextContent.builder(content).build());
        contents.add(loadImageContent(CLASS_IMAGE_RESOURCE));
        contents.add(loadImageContent(SEQUENCE_IMAGE_RESOURCE));
        contents.add(loadImageContent(ACTIVITY_IMAGE_RESOURCE));
        contents.add(loadImageContent(STATE_MACHINE_IMAGE_RESOURCE));
        contents.add(loadImageContent(REQUIREMENT_IMAGE_RESOURCE));

        return contents;
    }

    private List<McpSchema.Content> getAntiPatterns(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get diagram layout anti-patterns: {}", param);

        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(loadImageContent(CLASS_ANTI_PATTERNS_IMAGE_RESOURCE));
        contents.add(loadImageContent(SEQUENCE_ANTI_PATTERNS_IMAGE_RESOURCE));
        contents.add(loadImageContent(ACTIVITY_ANTI_PATTERNS_IMAGE_RESOURCE));
        contents.add(loadImageContent(STATE_MACHINE_ANTI_PATTERNS_IMAGE_RESOURCE));

        return contents;
    }

    private McpSchema.ImageContent loadImageContent(String resourcePath) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new Exception("Resource not found on classpath: " + resourcePath);
            }
            byte[] bytes = stream.readAllBytes();
            String encoded = Base64.getEncoder().encodeToString(bytes);
            return McpSchema.ImageContent.builder(encoded, "image/png").build();
        }
    }
}
