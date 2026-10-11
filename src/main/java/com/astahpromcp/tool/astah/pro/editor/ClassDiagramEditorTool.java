package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
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
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IAssociationClass;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/ClassDiagramEditor.html
@Slf4j
public class ClassDiagramEditorTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final ClassDiagramEditor classDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;

    public ClassDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, ClassDiagramEditor classDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.classDiagramEditor = classDiagramEditor;
        this.astahProToolSupport = astahProToolSupport;
        this.imageCaptureSupport = imageCaptureSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_class_dgm",
                "Create a new class diagram (which also serves as an object diagram and package diagram) under the specified package (specified by ID), and return the newly created model element of the class diagram.",
                this::createClassDiagram,
                NewDiagramInPackageDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_asso_class_prst",
                "Create a new association class presentation of the specified class (specified by ID) between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified class diagram (specified by ID), and return the newly created node and link presentations along with the updated diagram image in low resolution. Both node presentations must be on the specified class diagram and must draw the classes at the two ends of the association class, in either order, and the association class must not already be drawn on that class diagram.",
                this::createAssociationClassPresentation,
                NewAssociationClassPresentationDTO.class,
                PresentationListDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_instance_spec",
                "Create an instance specification of the specified class (specified by ID) at the specified point (specified by x and y coordinates) on the specified class diagram (specified by ID), and return the newly created node presentation of the instance specification along with the updated diagram image in low resolution. Set the name to an empty string when the instance specification has no name.",
                this::createInstanceSpecification,
                NewInstanceWithPointDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_link_between_instance_specs",
                "Create a link between two instance specifications (specified by presentation ID) on the specified class diagram (specified by ID), and return the newly created link presentation along with the updated diagram image in low resolution. Both instance specifications must be on the specified class diagram, and only one link can be created between the same two instance specifications. Note that the created link has no arrowheads.",
                this::createInstanceSpecificationLink,
                NewLinkSourceAndTargetDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private DiagramDTO createClassDiagram(NewDiagramInPackageDTO param) throws Exception {
        IPackage astahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        IClassDiagram createdAstahClassDiagram = txnAstah.call( () -> {
            return classDiagramEditor.createClassDiagram(astahPackage, param.newDiagramName());
        });

        return DiagramDTOAssembler.toDTO(createdAstahClassDiagram);
    }

    private Pair<PresentationListDTO, List<McpSchema.Content>> createAssociationClassPresentation(NewAssociationClassPresentationDTO param) throws Exception {
        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());
        IAssociationClass astahAssociationClass = astahProToolSupport.getAssociationClass(param.targetAssociationClassId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        if (!astahClassDiagram.equals(astahSourceNodePresentation.getDiagram())) {
            throw new IllegalArgumentException("The source node presentation is not on the target class diagram: " + param.sourceNodePresentationId());
        }
        if (!astahClassDiagram.equals(astahTargetNodePresentation.getDiagram())) {
            throw new IllegalArgumentException("The target node presentation is not on the target class diagram: " + param.targetNodePresentationId());
        }

        // Either order draws the same, so both are accepted.
        IAttribute[] astahMemberEnds = astahAssociationClass.getMemberEnds();
        IClass astahEndAClass = astahMemberEnds[0].getType();
        IClass astahEndBClass = astahMemberEnds[1].getType();
        Object astahSourceModel = astahSourceNodePresentation.getModel();
        Object astahTargetModel = astahTargetNodePresentation.getModel();
        if (!((astahEndAClass.equals(astahSourceModel) && astahEndBClass.equals(astahTargetModel))
                || (astahEndBClass.equals(astahSourceModel) && astahEndAClass.equals(astahTargetModel)))) {
            throw new IllegalArgumentException(String.format(
                "The source and target node presentations must draw the classes at the two ends of association class '%s', '%s' and '%s' (in either order), but they draw '%s' and '%s'.",
                astahAssociationClass.getName(),
                astahEndAClass.getName(),
                astahEndBClass.getName(),
                astahSourceNodePresentation.getLabel(),
                astahTargetNodePresentation.getLabel()));
        }

        for (IPresentation astahExistingPresentation : astahAssociationClass.getPresentations()) {
            if (astahClassDiagram.equals(astahExistingPresentation.getDiagram())) {
                throw new IllegalArgumentException("The association class is already drawn on the target class diagram (presentation ID: " + astahExistingPresentation.getID() + "). Only one association class presentation can be created for the same association class on a class diagram, so use the existing one.");
            }
        }

        classDiagramEditor.setDiagram(astahClassDiagram);

        IPresentation[] astahPresentations;
        try {
            astahPresentations = txnAstah.call( () -> {
                return classDiagramEditor.createAssociationClassPresentation(
                    astahAssociationClass,
                    astahSourceNodePresentation,
                    astahTargetNodePresentation);
            });

        } catch (NullPointerException e) {
            throw new IllegalStateException(String.format(
                "Astah could not draw association class '%s' on class diagram '%s' (%s). Astah fails this way on some class diagrams that were already in the project file, so calling this tool again, even in a separate run, fails the same way. Draw the association class on a class diagram created with create_class_dgm instead. Drawing its line and its class box separately does not connect them with the dashed line, so it is no substitute.",
                astahAssociationClass.getName(),
                astahClassDiagram.getName(),
                e.getMessage()),
                e);
        }

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation astahPresentation : astahPresentations) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(astahPresentation));
        }

        PresentationListDTO dto = new PresentationListDTO(presentationDTOs);

        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(param.targetDiagramId());

        return Pair.of(dto, List.of(thumbnailContent));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createInstanceSpecification(NewInstanceWithPointDTO param) throws Exception {
        IClass astahClass = astahProToolSupport.getClassOrPrimitiveType(param.targetClassId());
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

        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(param.targetDiagramId());

        return Pair.of(dto, List.of(thumbnailContent));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createInstanceSpecificationLink(NewLinkSourceAndTargetDTO param) throws Exception {
        INodePresentation astahSourceNode = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNode = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());

        if (!(astahSourceNode.getModel() instanceof IInstanceSpecification)) {
            throw new IllegalArgumentException("The source node presentation is not an instance specification: " + param.sourceNodePresentationId());
        }
        if (!(astahTargetNode.getModel() instanceof IInstanceSpecification)) {
            throw new IllegalArgumentException("The target node presentation is not an instance specification: " + param.targetNodePresentationId());
        }

        if (!astahClassDiagram.equals(astahSourceNode.getDiagram())) {
            throw new IllegalArgumentException("The source node presentation is not on the target class diagram: " + param.sourceNodePresentationId());
        }
        if (!astahClassDiagram.equals(astahTargetNode.getDiagram())) {
            throw new IllegalArgumentException("The target node presentation is not on the target class diagram: " + param.targetNodePresentationId());
        }

        // A link has no direction, so the reversed pair is the same link
        for (ILinkPresentation astahExistingLinkPresentation : astahSourceNode.getLinks()) {
            if (astahExistingLinkPresentation.getModel() instanceof ILink
                    && ((astahSourceNode.equals(astahExistingLinkPresentation.getSource()) && astahTargetNode.equals(astahExistingLinkPresentation.getTarget()))
                        || (astahTargetNode.equals(astahExistingLinkPresentation.getSource()) && astahSourceNode.equals(astahExistingLinkPresentation.getTarget())))) {
                throw new IllegalArgumentException("A link (ID: " + astahExistingLinkPresentation.getID() + ") already exists between the source and target instance specifications. Only one link can be created between the same two instance specifications, so use the existing one.");
            }
        }

        classDiagramEditor.setDiagram(astahClassDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return classDiagramEditor.createInstanceSpecificationLink(
                astahSourceNode,
                astahTargetNode);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.Content thumbnailContent = imageCaptureSupport.createThumbnailContent(param.targetDiagramId());

        return Pair.of(dto, List.of(thumbnailContent));
    }
}
