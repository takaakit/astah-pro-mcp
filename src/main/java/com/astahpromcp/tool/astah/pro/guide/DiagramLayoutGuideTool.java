package com.astahpromcp.tool.astah.pro.guide;

import java.util.List;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.guide.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiagramLayoutGuideTool implements ToolProvider {
    
    public DiagramLayoutGuideTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "dgm_layout_guide",
                            "MCP client (you) MUST call this tool function before aligning diagram layout, and then lay out the diagram in strict accordance with this layout guide.",
                            this::getGuide,
                            NoInputDTO.class,
                            GuideDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create diagram layout guide tools", e);
            return List.of();
        }
    }

    private GuideDTO getGuide(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get diagram layout guide: {}", param);
        
        String content = """
Common Layout Rules:
* Adjust the layout of any newly placed node/line presentations so that they always conform to the rules below. On the other hand, for node/line presentations that have already been placed, adjust the layout so that they conform to the rules below only when the user has explicitly instructed you to make layout adjustments.


Class Diagram Layout Rules:
* Align node presentations to minimize the number of intersecting line presentations. Because crossing one line presentation with another can cause confusion.
* For groups of sub classes that share the same super class, align the top edges of the sub-class node presentations horizontally whenever possible. This arrangement helps user easily recognize the sub-class groupings.
* Make each inheritance line presentation a right-angle line that starts from the top edge of the sub-class node presentation and ends at the bottom edge of the super-class node presentation.
* Adjust the positions of the node presentations so that, whenever possible, the arrow direction of inheritance and realization link presentations goes from bottom to top.
* Adjust the positions of the node presentations so that, whenever possible, the arrow direction of relationship and dependency link presentations goes from top to bottom or from left to right.
* Adjust the positions so that node presentation rectangles never overlap, except when one node presentation contains the other. For example, it is acceptable for the rectangle of a package to overlap with the rectangle of a class contained within that package, but it is not acceptable for rectangles of classes contained within the same package to overlap.
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* If there are multiple link presentations between two node presentations, adjust the paths of those link presentations so they do not overlap.
* For each class diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


Sequence Diagram Layout Rules:
* Ensure that lifeline rectangles never overlap with other lifeline rectangles. If lifeline rectangles would overlap, adjust the lifeline X coordinates to avoid the overlap.
* Ensure that messages never overlap with other messages. If messages would overlap, adjust the message Y coordinates to avoid the overlap.
* For each sequence diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


Activity Diagram Layout Rules:
* Adjust the node-presentation positions so that the axes of the initial node, action node, and finish node in the same flow are aligned in a straight line either vertically or horizontally, making it easier to recognize that those nodes belong to the same flow.
* Adjust the positions of the node presentations so that, whenever possible, the arrow direction of link presentations goes from top to bottom or from left to right.
* Adjust the positions so that node presentation rectangles never overlap, except when one node presentation contains the other. For example, it is acceptable for the rectangle of a partition to overlap with the rectangle of an action contained within that partition, but it is not acceptable for rectangles of actions contained within the same partition to overlap.
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* For each activity diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


State Machine Diagram Layout Rules:
* Adjust the node-presentation positions so that the axes of the initial node, action node, and finish node in the same flow are aligned in a straight line either vertically or horizontally, making it easier to recognize that those nodes belong to the same flow.
* Adjust the positions so that node presentation rectangles never overlap, except when one node presentation contains the other. For example, it is acceptable for the rectangle of a super state to overlap with the rectangle of a sub state contained within that super state, but it is not acceptable for rectangles of sub states contained within the same super state to overlap.
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* If there are multiple link presentations between two node presentations, adjust the paths of those link presentations so they do not overlap.
* For each state machine diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.
        """;

        return new GuideDTO(content);
    }
}
