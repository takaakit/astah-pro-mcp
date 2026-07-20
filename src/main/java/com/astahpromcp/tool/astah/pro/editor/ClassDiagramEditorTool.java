package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewAssociationClassPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewInstanceWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkSourceAndTargetDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.*;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.*;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IAssociationClass;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
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
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/ClassDiagramEditor.html
@Slf4j
public class ClassDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final ClassDiagramEditor classDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public ClassDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, ClassDiagramEditor classDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.classDiagramEditor = classDiagramEditor;
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
            log.error("Failed to create class diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_class_dgm",
                "Create a new class diagram (which also serves as an object diagram and package diagram) under the specified package (specified by ID), and return the newly created model element of the class diagram.",
                this::createClassDiagram,
                NewDiagramInPackageDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_asso_class_prst",
                "Create a new association class presentation of the specified class (specified by ID) between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified class diagram (specified by ID), and return the newly created node and link presentations along with the updated diagram image in low resolution.",
                this::createAssociationClassPresentation,
                NewAssociationClassPresentationDTO.class,
                PresentationListDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_instance_spec",
                "Create an instance specification of the specified class (specified by ID) at the specified point (specified by x and y coordinates) on the specified class diagram (specified by ID), and return the newly created node presentation of the instance specification along with the updated diagram image in low resolution.",
                this::createInstanceSpecification,
                NewInstanceWithPointDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_link_between_instance_specs",
                "Create a link between two instance specifications (specified by ID) on the specified class diagram (specified by ID), and return the newly created link presentation along with the updated diagram image in low resolution. Note that the created link has no arrowheads.",
                this::createInstanceSpecificationLink,
                NewLinkSourceAndTargetDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private DiagramDTO createClassDiagram(McpSyncServerExchange exchange, NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create class diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        IClassDiagram createdAstahClassDiagram = txnAstah.call( () -> {
            return classDiagramEditor.createClassDiagram(astahPackage, param.newDiagramName());
        });

        return DiagramDTOAssembler.toDTO(createdAstahClassDiagram);
    }

    private Pair<PresentationListDTO, List<McpSchema.Content>> createAssociationClassPresentation(McpSyncServerExchange exchange, NewAssociationClassPresentationDTO param) throws Exception {
        log.debug("Create association class presentation: {}", param);

        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());
        IAssociationClass astahAssociationClass = astahProToolSupport.getAssociationClass(param.targetAssociationClassId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        classDiagramEditor.setDiagram(astahClassDiagram);

        IPresentation[] astahPresentations = txnAstah.call( () -> {
            return classDiagramEditor.createAssociationClassPresentation(
                astahAssociationClass,
                astahSourceNodePresentation,
                astahTargetNodePresentation);
        });

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation astahPresentation : astahPresentations) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(astahPresentation));
        }

        PresentationListDTO dto = new PresentationListDTO(presentationDTOs);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createInstanceSpecification(McpSyncServerExchange exchange, NewInstanceWithPointDTO param) throws Exception {
        log.debug("Create instance specification: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());
        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());

        classDiagramEditor.setDiagram(astahClassDiagram);

        INodePresentation astahNodePresentation = txnAstah.call( () -> {
            INodePresentation createdNodePresentation = classDiagramEditor.createInstanceSpecification(
                param.newInstanceName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            
            ((IInstanceSpecification) createdNodePresentation.getModel()).setClassifier(astahClass);
            
            return createdNodePresentation;
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createInstanceSpecificationLink(McpSyncServerExchange exchange, NewLinkSourceAndTargetDTO param) throws Exception {
        log.debug("Create instance specification link: {}", param);

        INodePresentation astahSourceNode = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNode = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());

        classDiagramEditor.setDiagram(astahClassDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return classDiagramEditor.createInstanceSpecificationLink(
                astahSourceNode,
                astahTargetNode);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }
}
