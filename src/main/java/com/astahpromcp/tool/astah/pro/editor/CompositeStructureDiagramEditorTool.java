package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewConnectorPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewPortPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewProvidedInterfacePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewRequiredInterfacePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewStructuredClassPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewStructuredClassPresentationUnderParentDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
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
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/CompositeStructureDiagramEditor.html
@Slf4j
public class CompositeStructureDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final CompositeStructureDiagramEditor compositeStructureDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public CompositeStructureDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, CompositeStructureDiagramEditor compositeStructureDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.compositeStructureDiagramEditor = compositeStructureDiagramEditor;
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
            log.error("Failed to create composite structure diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_composite_structure_dgm",
                "Create a new composite structure diagram under the specified package (specified by ID), and return the newly created model element of the composite structure diagram.",
                this::createCompositeStructureDiagram,
                NewDiagramInPackageDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_connector_prst",
                "Create a new connector presentation between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified composite structure diagram (specified by ID), and return the newly created link presentation of the connector along with the updated diagram image in low resolution. The specified source/target node presentations are allowed to be part presentation or port presentation.",
                this::createConnectorPresentation,
                NewConnectorPresentationDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_port_prst",
                "Create a new port presentation of the specified part (specified by ID) or structured class (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the port along with the updated diagram image in low resolution.",
                this::createPortPresentation,
                NewPortPresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_provided_interface_prst",
                "Create a new provided interface presentation of the specified interface (specified by ID) for the specified port (specified by ID) or part (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the provided interface along with the updated diagram image in low resolution.",
                this::createProvidedInterfacePresentation,
                NewProvidedInterfacePresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_required_interface_prst",
                "Create a new required interface presentation of the specified interface (specified by ID) for the specified port (specified by ID) or part (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the required interface along with the updated diagram image in low resolution.",
                this::createRequiredInterfacePresentation,
                NewRequiredInterfacePresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_structured_class_prst",
                "Create a new structured class presentation of the specified structured class (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the structured class along with the updated diagram image in low resolution.",
                this::createStructuredClassPresentation,
                NewStructuredClassPresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_structured_class_prst_under_parent",
                "Create a new structured class presentation of the specified structured class (specified by ID) under the specified parent node presentation (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the structured class along with the updated diagram image in low resolution.",
                this::createStructuredClassPresentationUnderParent,
                NewStructuredClassPresentationUnderParentDTO.class,
                NodePresentationDTO.class)
        );
    }

    private DiagramDTO createCompositeStructureDiagram(McpSyncServerExchange exchange, NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create composite structure diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        ICompositeStructureDiagram createdAstahCompositeStructureDiagram = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createCompositeStructureDiagram(astahPackage, param.newDiagramName());
        });

        return DiagramDTOAssembler.toDTO(createdAstahCompositeStructureDiagram);
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createConnectorPresentation(McpSyncServerExchange exchange, NewConnectorPresentationDTO param) throws Exception {
        log.debug("Create connector presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createConnectorPresentation(
                astahSourceNodePresentation,
                astahTargetNodePresentation,
                param.newConnectorName());
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createPortPresentation(McpSyncServerExchange exchange, NewPortPresentationDTO param) throws Exception {
        log.debug("Create port presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createPortPresentation(
                astahTargetNodePresentation,
                astahPort,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createProvidedInterfacePresentation(McpSyncServerExchange exchange, NewProvidedInterfacePresentationDTO param) throws Exception {
        log.debug("Create provided interface presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IClass astahInterface = astahProToolSupport.getClass(param.targetInterfaceId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createProvidedInterfacePresentation(
                astahTargetNodePresentation,
                astahInterface,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createRequiredInterfacePresentation(McpSyncServerExchange exchange, NewRequiredInterfacePresentationDTO param) throws Exception {
        log.debug("Create required interface presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IClass astahInterface = astahProToolSupport.getClass(param.targetInterfaceId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createRequiredInterfacePresentation(
                astahTargetNodePresentation,
                astahInterface,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createStructuredClassPresentation(McpSyncServerExchange exchange, NewStructuredClassPresentationDTO param) throws Exception {
        log.debug("Create structured class presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createStructuredClassPresentation(
                astahElement,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createStructuredClassPresentationUnderParent(McpSyncServerExchange exchange, NewStructuredClassPresentationUnderParentDTO param) throws Exception {
        log.debug("Create structured class presentation under parent: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createStructuredClassPresentation(
                astahElement,
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }
}
