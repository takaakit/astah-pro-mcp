package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNestingLinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNodePresentationDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.StructureDiagramEditor;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Value;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/StructureDiagramEditor.html
@Slf4j
public class StructureDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public StructureDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.diagramEditorSupport = diagramEditorSupport;
        this.imageCaptureSupport = imageCaptureSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create structure diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_node_prst_on_dgm",
                "Create a new node presentation of the specified element (specified by ID) on the specified diagram (specified by ID), and return the newly created node presentation along with the updated diagram image.",
                this::createNodePresentation,
                NewNodePresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_link_prst_on_dgm",
                "Create a new link presentation between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified diagram (specified by ID), and return the newly created link presentation along with the updated diagram image.",
                this::createLinkPresentation,
                NewLinkPresentationDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_nesting_link_prst_on_dgm",
                "Create a new link presentation that indicates nesting (\"circle-plus\" notation) between the specified parent node presentation (specified by ID) and the specified child node presentation (specified by ID) on the specified diagram (specified by ID), and return the newly created link presentation along with the updated diagram image. For example, use this tool to draw nested relationships between requirements in a requirements diagram.",
                this::createNestingLinkPresentation,
                NewNestingLinkPresentationDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createNodePresentation(McpSyncServerExchange exchange, NewNodePresentationDTO param) throws Exception {
        log.debug("Create node presentation on diagram: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());
        IDiagram astahStructureDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());

        StructureDiagramEditor structureDiagramEditor;
        try {
            structureDiagramEditor = (StructureDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahStructureDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get structure diagram editor.");
        }

        structureDiagramEditor.setDiagram(astahStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            INodePresentation nodePresentation = structureDiagramEditor.createNodePresentation(
                astahElement,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));

            // Use normal notation (e.g., interfaces as rectangles), except for actors on Use Case or Sequence diagrams.
            if (astahElement.hasStereotype("actor")
                && (astahStructureDiagram instanceof IUseCaseDiagram || astahStructureDiagram instanceof ISequenceDiagram)) {
                nodePresentation.setProperty(
                    Key.NOTATION_TYPE,
                    Value.NOTATION_TYPE_ICON);
            } else {
                nodePresentation.setProperty(
                    Key.NOTATION_TYPE,
                    Value.NOTATION_TYPE_NORMAL);
            }
            return nodePresentation;
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createLinkPresentation(McpSyncServerExchange exchange, NewLinkPresentationDTO param) throws Exception {
        log.debug("Create link presentation on diagram: {}", param);

        IDiagram astahStructureDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());
        INodePresentation astahSourceNode = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNode = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        StructureDiagramEditor structureDiagramEditor;
        try {
            structureDiagramEditor = (StructureDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahStructureDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get structure diagram editor.");
        }

        structureDiagramEditor.setDiagram(astahStructureDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return structureDiagramEditor.createLinkPresentation(astahElement, astahSourceNode, astahTargetNode);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createNestingLinkPresentation(McpSyncServerExchange exchange, NewNestingLinkPresentationDTO param) throws Exception {
        log.debug("Create nesting link presentation on diagram: {}", param);

        IDiagram astahStructureDiagram = astahProToolSupport.getDiagram(param.targetDiagramId());
        INodePresentation astahParentNode = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        INodePresentation astahChildNode = astahProToolSupport.getNodePresentation(param.childNodePresentationId());

        StructureDiagramEditor structureDiagramEditor;
        try {
            structureDiagramEditor = (StructureDiagramEditor) diagramEditorSupport.getCorrespondingDiagramEditor(astahStructureDiagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get structure diagram editor.");
        }

        structureDiagramEditor.setDiagram(astahStructureDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return structureDiagramEditor.createContainmentLinkPresentation(astahParentNode, astahChildNode);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

        return Pair.of(dto, List.of(image));
    }
}
