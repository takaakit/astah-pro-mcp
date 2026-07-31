package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewPartPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewPortPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewProvidedInterfacePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewRequiredInterfacePresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewStructuredClassPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.ShowInterfacePresentationsOfPortDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO.Type;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                "create_port_prst",
                "Create a new port presentation of the specified part (specified by ID) or structured class (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the port along with the updated diagram image in low resolution.",
                this::createPortPresentation,
                NewPortPresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_provided_interface_prst",
                "Create a new provided interface presentation of the specified interface (specified by ID) for the specified port (specified by ID) or part (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the provided interface along with the updated diagram image in low resolution. Note that if the specified interface is already a provided interface of the specified port or part, only the interface symbol is placed and no line connecting it to the port or part is drawn.",
                this::createProvidedInterfacePresentation,
                NewProvidedInterfacePresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_required_interface_prst",
                "Create a new required interface presentation of the specified interface (specified by ID) for the specified port (specified by ID) or part (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the required interface along with the updated diagram image in low resolution. Note that if the specified interface is already a required interface of the specified port or part, only the interface symbol is placed and no line connecting it to the port or part is drawn.",
                this::createRequiredInterfacePresentation,
                NewRequiredInterfacePresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_structured_class_prst",
                "Create a new structured class presentation of the specified class (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the structured class along with the updated diagram image in low resolution. Since a structured class presentation cannot be shrunk once it has part presentations inside, use other tool function to set its width and height before creating part presentations.",
                this::createStructuredClassPresentation,
                NewStructuredClassPresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_part_prst",
                "Create a new part presentation of the specified attribute (specified by ID) inside the specified parent structured class presentation (specified by ID) at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created node presentation of the part along with the updated diagram image in low resolution. Since the specified point is an absolute coordinate on the diagram, specify a point inside the parent structured class presentation; otherwise the parent is automatically enlarged to enclose the part.",
                this::createPartPresentation,
                NewPartPresentationDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "show_interface_prsts_of_port",
                "Show the provided and required interfaces that the specified port (specified by ID) already has, as interface presentations connected to the port at the specified point (specified by x and y coordinates) on the specified composite structure diagram (specified by ID), and return the newly created presentations along with the updated diagram image in low resolution. Since all the interface symbols are placed at the same point, use other tool function to move each of them afterwards. Note that nothing is created when the port has no interfaces or when its interfaces are already shown on the diagram.",
                this::showInterfacePresentationsOfPort,
                ShowInterfacePresentationsOfPortDTO.class,
                PresentationListDTO.class)
        );
    }

    private DiagramDTO createCompositeStructureDiagram(NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create composite structure diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        ICompositeStructureDiagram createdAstahCompositeStructureDiagram = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createCompositeStructureDiagram(astahPackage, param.newDiagramName());
        });

        return DiagramDTOAssembler.toDTO(createdAstahCompositeStructureDiagram);
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createPortPresentation(NewPortPresentationDTO param) throws Exception {
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

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createProvidedInterfacePresentation(NewProvidedInterfacePresentationDTO param) throws Exception {
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

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createRequiredInterfacePresentation(NewRequiredInterfacePresentationDTO param) throws Exception {
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

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createStructuredClassPresentation(NewStructuredClassPresentationDTO param) throws Exception {
        log.debug("Create structured class presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createStructuredClassPresentation(
                astahClass,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createPartPresentation(NewPartPresentationDTO param) throws Exception {
        log.debug("Create part presentation: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());

        // IPort extends IAttribute, so a port is accepted here and drawn as a part box instead of a port.
        IAttribute astahAttribute = astahProToolSupport.getAttribute(param.targetAttributeId());
        if (astahAttribute instanceof IPort) {
            throw new IllegalArgumentException("Target attribute for part presentation must not be a port.");
        }

        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        if (!Type.STRUCTURED_CLASS.matches(astahParentNodePresentation.getType())) {
            throw new IllegalArgumentException("Parent node for part presentation must be one of the following node presentation types: StructuredClass.");
        }

        // Nothing is added to the diagram and no error is raised when the parent structured class does not own the attribute.
        IElement astahParentModel = astahParentNodePresentation.getModel();
        if (astahParentModel == null || !astahParentModel.equals(astahAttribute.getOwner())) {
            throw new IllegalArgumentException("Target attribute for part presentation must be owned by the class that the parent node presentation represents.");
        }

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            return compositeStructureDiagramEditor.createStructuredClassPresentation(
                astahAttribute,
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<PresentationListDTO, List<McpSchema.Content>> showInterfacePresentationsOfPort(ShowInterfacePresentationsOfPortDTO param) throws Exception {
        log.debug("Show interface presentations of port: {}", param);

        ICompositeStructureDiagram astahCompositeStructureDiagram = astahProToolSupport.getCompositeStructureDiagram(param.targetCompositeStructureDiagramId());

        INodePresentation astahNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        if (!Type.PORT.matches(astahNodePresentation.getType())) {
            throw new IllegalArgumentException("Target node for showing interface presentations must be one of the following node presentation types: Port.");
        }

        compositeStructureDiagramEditor.setDiagram(astahCompositeStructureDiagram);

        Set<String> beforePresentationIds = new HashSet<>();
        for (IPresentation astahPresentation : astahCompositeStructureDiagram.getPresentations()) {
            beforePresentationIds.add(astahPresentation.getID());
        }

        txnAstah.run( () -> {
            compositeStructureDiagramEditor.showInterfacePresentations(
                astahNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        // The newly created presentations
        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation astahPresentation : astahCompositeStructureDiagram.getPresentations()) {
            if (!beforePresentationIds.contains(astahPresentation.getID())) {
                presentationDTOs.add(PresentationDTOAssembler.toDTO(astahPresentation));
            }
        }

        PresentationListDTO dto = new PresentationListDTO(presentationDTOs);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetCompositeStructureDiagramId());

        return Pair.of(dto, List.of(image));
    }
}
