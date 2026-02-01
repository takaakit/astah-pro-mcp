package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkPresentationOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNodePresentationOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewSubtypeRelationshipGroupOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ERDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IERDiagram;
import com.change_vision.jude.api.inf.model.IERPackage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/ERDiagramEditor.html
@Slf4j
public class ERDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final ERDiagramEditor erDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, ERDiagramEditor erDiagramEditor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.erDiagramEditor = erDiagramEditor;
        this.astahProToolSupport = astahProToolSupport;
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
            log.error("Failed to create ER diagram editor tools", e);
            return List.of();
        }
    }
    
    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_er_dgm",
                        "Create a new ER diagram under the specified ER package (specified by ID), and return the newly created ER diagram information.",
                        this::createERDiagram,
                        NewERDiagramDTO.class,
                        ERDiagramDTO.class),

                ToolSupport.definition(
                        "create_node_prst_on_er_dgm",
                        "Create a new node presentation of the specified element (specified by ID) on the specified ER diagram (specified by ID), and return the newly created node presentation information.",
                        this::createNodePresentation,
                        NewNodePresentationOnERDiagramDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_link_prst_on_er_dgm",
                        "Create a new link presentation between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified ER diagram (specified by ID), and return the newly created link presentation information.",
                        this::createLinkPresentation,
                        NewLinkPresentationOnERDiagramDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_subtype_relationship_group_on_er_dgm",
                        "Create a new node presentation for the group of the shared subtype relationships on the specified ER diagram (specified by ID), and return the newly created node presentation information for the group of the subtype relationships.",
                        this::createSubtypeRelationshipGroup,
                        NewSubtypeRelationshipGroupOnERDiagramDTO.class,
                        NodePresentationDTO.class)
        );
    }

    private ERDiagramDTO createERDiagram(McpSyncServerExchange exchange, NewERDiagramDTO param) throws Exception {
        log.debug("Create ER diagram: {}", param);

        IERPackage astahERPackage = astahProToolSupport.getERPackage(param.targetERPackageId());

        try {
            transactionManager.beginTransaction();
            IERDiagram createdAstahERDiagram = erDiagramEditor.createERDiagram(
                astahERPackage,
                param.newERDiagramName());
            transactionManager.endTransaction();

            return ERDiagramDTOAssembler.toDTO(createdAstahERDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createNodePresentation(McpSyncServerExchange exchange, NewNodePresentationOnERDiagramDTO param) throws Exception {
        log.debug("Create node presentation on ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        erDiagramEditor.setDiagram(astahERDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = erDiagramEditor.createNodePresentation(
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

    private LinkPresentationDTO createLinkPresentation(McpSyncServerExchange exchange, NewLinkPresentationOnERDiagramDTO param) throws Exception {
        log.debug("Create link presentation on ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        erDiagramEditor.setDiagram(astahERDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLinkPresentation = erDiagramEditor.createLinkPresentation(
                astahElement,
                astahSourceNodePresentation,
                astahTargetNodePresentation);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createSubtypeRelationshipGroup(McpSyncServerExchange exchange, NewSubtypeRelationshipGroupOnERDiagramDTO param) throws Exception {
        log.debug("Create subtype relationship group on ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        List<ILinkPresentation> subtypeRelationshipPresentations = new ArrayList<>();
        for (String subtypeRelationshipId : param.subtypeRelationshipLinkPresentationIds()) {
            subtypeRelationshipPresentations.add(astahProToolSupport.getLinkPresentation(subtypeRelationshipId));
        }

        erDiagramEditor.setDiagram(astahERDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = erDiagramEditor.createSubtypeRelationshipGroup(
                subtypeRelationshipPresentations.toArray(new ILinkPresentation[0]),
                param.direction());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
