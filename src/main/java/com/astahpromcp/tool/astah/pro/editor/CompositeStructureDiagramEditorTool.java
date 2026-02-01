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
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/CompositeStructureDiagramEditor.html
@Slf4j
public class CompositeStructureDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final CompositeStructureDiagramEditor compositeStructureDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public CompositeStructureDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, CompositeStructureDiagramEditor compositeStructureDiagramEditor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.compositeStructureDiagramEditor = compositeStructureDiagramEditor;
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
            log.error("Failed to create composite structure diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_composite_structure_dgm",
                        "Create a new composite structure diagram under the specified package (specified by ID), and return the newly created composite structure diagram information.",
                        this::createCompositeStructureDiagram,
                        NewDiagramInPackageDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "create_connector_prst",
                        "Create a new connector presentation between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified composite structure diagram (specified by ID), and return the newly created connector presentation information. The specified source/target node presentations are allowed to be part presentation or port presentation.",
                        this::createConnectorPresentation,
                        NewConnectorPresentationDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_port_prst",
                        "Create a new port presentation of the specified part (specified by ID) or structured class (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created port presentation information.",
                        this::createPortPresentation,
                        NewPortPresentationDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_provided_interface_prst",
                        "Create a new provided interface presentation of the specified interface (specified by ID) for the specified port (specified by ID) or part (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created provided interface presentation information.",
                        this::createProvidedInterfacePresentation,
                        NewProvidedInterfacePresentationDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_required_interface_prst",
                        "Create a new required interface presentation of the specified interface (specified by ID) for the specified port (specified by ID) or part (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created required interface presentation information.",
                        this::createRequiredInterfacePresentation,
                        NewRequiredInterfacePresentationDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_structured_class_prst",
                        "Create a new structured class presentation of the specified structured class (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created structured class presentation information.",
                        this::createStructuredClassPresentation,
                        NewStructuredClassPresentationDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_structured_class_prst_under_parent",
                        "Create a new structured class presentation of the specified structured class (specified by ID) under the specified parent node presentation (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created structured class presentation information.",
                        this::createStructuredClassPresentationUnderParent,
                        NewStructuredClassPresentationUnderParentDTO.class,
                        NodePresentationDTO.class)
        );
    }

    private DiagramDTO createCompositeStructureDiagram(McpSyncServerExchange exchange, NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create composite structure diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        try {
            transactionManager.beginTransaction();
            ICompositeStructureDiagram createdAstahCompositeStructureDiagram = compositeStructureDiagramEditor.createCompositeStructureDiagram(astahPackage, param.newDiagramName());
            transactionManager.endTransaction();

            return DiagramDTOAssembler.toDTO(createdAstahCompositeStructureDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createConnectorPresentation(McpSyncServerExchange exchange, NewConnectorPresentationDTO param) throws Exception {
        log.debug("Create connector presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLinkPresentation = compositeStructureDiagramEditor.createConnectorPresentation(
                astahSourceNodePresentation,
                astahTargetNodePresentation,
                param.newConnectorName());
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createPortPresentation(McpSyncServerExchange exchange, NewPortPresentationDTO param) throws Exception {
        log.debug("Create port presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = compositeStructureDiagramEditor.createPortPresentation(
                astahTargetNodePresentation,
                astahPort,
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

    private NodePresentationDTO createProvidedInterfacePresentation(McpSyncServerExchange exchange, NewProvidedInterfacePresentationDTO param) throws Exception {
        log.debug("Create provided interface presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IClass astahInterface = astahProToolSupport.getClass(param.targetInterfaceId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = compositeStructureDiagramEditor.createProvidedInterfacePresentation(
                astahTargetNodePresentation,
                astahInterface,
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

    private NodePresentationDTO createRequiredInterfacePresentation(McpSyncServerExchange exchange, NewRequiredInterfacePresentationDTO param) throws Exception {
        log.debug("Create required interface presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IClass astahInterface = astahProToolSupport.getClass(param.targetInterfaceId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = compositeStructureDiagramEditor.createRequiredInterfacePresentation(
                astahTargetNodePresentation,
                astahInterface,
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
    
    private NodePresentationDTO createStructuredClassPresentation(McpSyncServerExchange exchange, NewStructuredClassPresentationDTO param) throws Exception {
        log.debug("Create structured class presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = compositeStructureDiagramEditor.createStructuredClassPresentation(
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

    private NodePresentationDTO createStructuredClassPresentationUnderParent(McpSyncServerExchange exchange, NewStructuredClassPresentationUnderParentDTO param) throws Exception {
        log.debug("Create structured class presentation under parent: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = compositeStructureDiagramEditor.createStructuredClassPresentation(
                astahElement,
                astahParentNodePresentation,
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
}
