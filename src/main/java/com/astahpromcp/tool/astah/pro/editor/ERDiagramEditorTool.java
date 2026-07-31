package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkPresentationOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewNodePresentationOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewSubtypeRelationshipGroupOnERDiagramDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ERDiagramEditor;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IERDiagram;
import com.change_vision.jude.api.inf.model.IERPackage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/ERDiagramEditor.html
@Slf4j
public class ERDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final ERDiagramEditor erDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public ERDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, ERDiagramEditor erDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.erDiagramEditor = erDiagramEditor;
        this.astahProToolSupport = astahProToolSupport;
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
            log.error("Failed to create ER diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_er_dgm",
                "Create a new ER diagram under the specified ER package (specified by ID), and return the newly created model element of the ER diagram.",
                this::createERDiagram,
                NewERDiagramDTO.class,
                ERDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_node_prst_on_er_dgm",
                "Create a new node presentation of the specified element (specified by ID) on the specified ER diagram (specified by ID), and return the newly created node presentation along with the updated diagram image in low resolution.",
                this::createNodePresentation,
                NewNodePresentationOnERDiagramDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_link_prst_on_er_dgm",
                "Create a new link presentation between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified ER diagram (specified by ID), and return the newly created link presentation along with the updated diagram image in low resolution.",
                this::createLinkPresentation,
                NewLinkPresentationOnERDiagramDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_subtype_relationship_group_on_er_dgm",
                "Create a new node presentation for the group of the shared subtype relationships on the specified ER diagram (specified by ID), and return the newly created node presentation for the group of the subtype relationships along with the updated diagram image in low resolution.",
                this::createSubtypeRelationshipGroup,
                NewSubtypeRelationshipGroupOnERDiagramDTO.class,
                NodePresentationDTO.class)
        );
    }

    private ERDiagramDTO createERDiagram(NewERDiagramDTO param) throws Exception {
        log.debug("Create ER diagram: {}", param);

        IERPackage astahERPackage = astahProToolSupport.getERPackage(param.targetERPackageId());

        IERDiagram createdAstahERDiagram = txnAstah.call( () -> {
            return erDiagramEditor.createERDiagram(
                astahERPackage,
                param.newERDiagramName());
        });

        return ERDiagramDTOAssembler.toDTO(createdAstahERDiagram);
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createNodePresentation(NewNodePresentationOnERDiagramDTO param) throws Exception {
        log.debug("Create node presentation on ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        erDiagramEditor.setDiagram(astahERDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return erDiagramEditor.createNodePresentation(
                astahElement,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetERDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createLinkPresentation(NewLinkPresentationOnERDiagramDTO param) throws Exception {
        log.debug("Create link presentation on ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        erDiagramEditor.setDiagram(astahERDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return erDiagramEditor.createLinkPresentation(
                astahElement,
                astahSourceNodePresentation,
                astahTargetNodePresentation);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetERDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createSubtypeRelationshipGroup(NewSubtypeRelationshipGroupOnERDiagramDTO param) throws Exception {
        log.debug("Create subtype relationship group on ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        List<ILinkPresentation> subtypeRelationshipPresentations = new ArrayList<>();
        for (String subtypeRelationshipId : param.subtypeRelationshipLinkPresentationIds()) {
            subtypeRelationshipPresentations.add(astahProToolSupport.getLinkPresentation(subtypeRelationshipId));
        }

        erDiagramEditor.setDiagram(astahERDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return erDiagramEditor.createSubtypeRelationshipGroup(
                subtypeRelationshipPresentations.toArray(new ILinkPresentation[0]),
                param.direction());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetERDiagramId());

        return Pair.of(dto, List.of(image));
    }
}
