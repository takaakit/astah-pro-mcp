package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.StructureDiagramEditor;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/ja/doc/javadoc/com/change_vision/jude/api/inf/editor/StructureDiagramEditor.html
@Slf4j
public class StructureDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final DiagramEditorSupport diagramEditorSupport;
    private final boolean includeEditTools;

    public StructureDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, DiagramEditorSupport diagramEditorSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.diagramEditorSupport = diagramEditorSupport;
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
                ToolSupport.definition(
                        "create_node_prst_on_dgm",
                        "Create a new node presentation of the specified element (specified by ID) on the specified diagram (specified by ID), and return the newly created node presentation information.",
                        this::createNodePresentation,
                        NewNodePresentationDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_link_prst_on_dgm",
                        "Create a new link presentation between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified diagram (specified by ID), and return the newly created link presentation information.",
                        this::createLinkPresentation,
                        NewLinkPresentationDTO.class,
                        LinkPresentationDTO.class)
        );
    }

    private NodePresentationDTO createNodePresentation(McpSyncServerExchange exchange, NewNodePresentationDTO param) throws Exception {
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

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = structureDiagramEditor.createNodePresentation(
                astahElement,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()));
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createLinkPresentation(McpSyncServerExchange exchange, NewLinkPresentationDTO param) throws Exception {
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

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLinkPresentation = structureDiagramEditor.createLinkPresentation(astahElement, astahSourceNode, astahTargetNode);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
