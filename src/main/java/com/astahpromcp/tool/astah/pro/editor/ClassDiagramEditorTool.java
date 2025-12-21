package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewAssociationClassPresentationDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewInstanceWithPointDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkSourceAndTargetDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.*;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.*;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAssociationClass;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/ja/doc/javadoc/com/change_vision/jude/api/inf/editor/ClassDiagramEditor.html
@Slf4j
public class ClassDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final ClassDiagramEditor classDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ClassDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, ClassDiagramEditor classDiagramEditor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.classDiagramEditor = classDiagramEditor;
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
            log.error("Failed to create class diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_class_dgm",
                        "Create a new class diagram (which also serves as an object diagram and package diagram) under the specified package (specified by ID), and return the newly created class diagram information.",
                        this::createClassDiagram,
                        NewDiagramInPackageDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "create_asso_class_prst",
                        "Create a new association class presentation of the specified class (specified by ID) between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified class diagram (specified by ID), and return the information of newly created node and linkpresentations.",
                        this::createAssociationClassPresentation,
                        NewAssociationClassPresentationDTO.class,
                        PresentationListDTO.class),

                ToolSupport.definition(
                        "create_instance_spec",
                        "Create an instance specification of the specified class (specified by ID) at the specified point (specified by x and y coordinates) on the specified class diagram (specified by ID), and return the newly created instance specification information (node presentation information).",
                        this::createInstanceSpecification,
                        NewInstanceWithPointDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_link_between_instance_specs",
                        "Create a link between two instance specifications (specified by ID) on the specified class diagram (specified by ID), and return the newly created link presentation information.",
                        this::createInstanceSpecificationLink,
                        NewLinkSourceAndTargetDTO.class,
                        LinkPresentationDTO.class)
        );
    }

    private DiagramDTO createClassDiagram(McpSyncServerExchange exchange, NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create class diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        try {
            transactionManager.beginTransaction();
            IClassDiagram createdAstahClassDiagram = classDiagramEditor.createClassDiagram(astahPackage, param.newDiagramName());
            transactionManager.endTransaction();

            return DiagramDTOAssembler.toDTO(createdAstahClassDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private PresentationListDTO createAssociationClassPresentation(McpSyncServerExchange exchange, NewAssociationClassPresentationDTO param) throws Exception {
        log.debug("Create association class presentation: {}", param);

        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());
        IAssociationClass astahAssociationClass = astahProToolSupport.getAssociationClass(param.targetAssociationClassId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        classDiagramEditor.setDiagram(astahClassDiagram);

        try {
            transactionManager.beginTransaction();
            IPresentation[] astahPresentations = classDiagramEditor.createAssociationClassPresentation(
                astahAssociationClass,
                astahSourceNodePresentation,
                astahTargetNodePresentation);
            transactionManager.endTransaction();

            List<PresentationDTO> presentationDTOs = new ArrayList<>();
            for (IPresentation astahPresentation : astahPresentations) {
                presentationDTOs.add(PresentationDTOAssembler.toDTO(astahPresentation));
            }

            return new PresentationListDTO(presentationDTOs);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createInstanceSpecification(McpSyncServerExchange exchange, NewInstanceWithPointDTO param) throws Exception {
        log.debug("Create instance specification: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());
        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());

        classDiagramEditor.setDiagram(astahClassDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = classDiagramEditor.createInstanceSpecification(
                param.newInstanceName(),
                astahClass.getFullName("."),
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

    private LinkPresentationDTO createInstanceSpecificationLink(McpSyncServerExchange exchange, NewLinkSourceAndTargetDTO param) throws Exception {
        log.debug("Create instance specification link: {}", param);

        INodePresentation astahSourceNode = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNode = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());
        IClassDiagram astahClassDiagram = astahProToolSupport.getClassDiagram(param.targetDiagramId());

        classDiagramEditor.setDiagram(astahClassDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLinkPresentation = classDiagramEditor.createInstanceSpecificationLink(
                astahSourceNode,
                astahTargetNode);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
