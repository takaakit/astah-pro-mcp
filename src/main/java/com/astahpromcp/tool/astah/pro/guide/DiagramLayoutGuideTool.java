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
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* If there are multiple link presentations between two node presentations, adjust the paths of those link presentations so they do not overlap.
* For each class diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


Activity Diagram Layout Rules:
* Adjust the node-presentation positions so that the axes of the initial node, action node, and finish node in the same flow are aligned in a straight line either vertically or horizontally, making it easier to recognize that those nodes belong to the same flow.
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* For each activity diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


State Machine Diagram Layout Rules:
* Adjust the node-presentation positions so that the axes of the initial node, action node, and finish node in the same flow are aligned in a straight line either vertically or horizontally, making it easier to recognize that those nodes belong to the same flow.
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* If there are multiple link presentations between two node presentations, adjust the paths of those link presentations so they do not overlap.
* For each activity diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


Composite Structure Diagram Layout Rules:
* Adjust the node-presentation positions so that the axes of the initial node, action node, and finish node in the same flow are aligned in a straight line either vertically or horizontally, making it easier to recognize that those nodes belong to the same flow.
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* If there are multiple link presentations between two node presentations, adjust the paths of those link presentations so they do not overlap.
* For each activity diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.


ER Diagram Layout Rules:
* Align node presentations to minimize the number of intersecting line presentations. Because crossing one line presentation with another can cause confusion.
* Ensure that line-presentation paths never overlap with node-presentation rectangles. If a line-presentation path would overlap a node-presentation rectangle, reroute the line presentation to avoid the overlap.
* If there are multiple link presentations between two node presentations, adjust the paths of those link presentations so they do not overlap.
* For each ER diagram, repeat the cycle of "checking compliance with the above rules -> adjusting the layout" up to three times so that the above rules are satisfied simultaneously.
        """;

        return new GuideDTO(content);
    }
}
