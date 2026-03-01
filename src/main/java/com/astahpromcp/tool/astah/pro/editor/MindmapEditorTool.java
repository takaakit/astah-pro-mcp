package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.ChangeToFloatingTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeleteChildTopicsDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.DeleteImageFromTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewFloatingTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.ChangeParentOfTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.MoveTopicWithinSiblingOrderDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewJpgImageIntoTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewLinkBetweenTopicsDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewPngImageIntoTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewSvgImageIntoTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewTopicDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.TopicWithBoundaryVisibilityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.MindmapEditor;
import com.change_vision.jude.api.inf.model.IMindMapDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/MindmapEditor.html
@Slf4j
public class MindmapEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final MindmapEditor mindmapEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageConvertSupport imageConvertSupport;
    private final boolean includeEditTools;

    public MindmapEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, MindmapEditor mindmapEditor, AstahProToolSupport astahProToolSupport, ImageConvertSupport imageConvertSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.mindmapEditor = mindmapEditor;
        this.astahProToolSupport = astahProToolSupport;
        this.imageConvertSupport = imageConvertSupport;
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
            log.error("Failed to create mind map editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_mind_map_dgm",
                        "Create a new mind map diagram under the specified package (specified by ID), and return the information of newly created mind map diagram. The diagram name is also used as the root topic label.",
                        this::createMindmapDiagram,
                        NewDiagramInPackageDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "change_to_floating_topic",
                        "Change the specified topic (specified by ID) to a floating topic on the specified mind map diagram (specified by ID), and return the changed topic information. A floating topic is a separate topic from the root topic.",
                        this::changeToFloatingTopic,
                        ChangeToFloatingTopicDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_floating_topic",
                        "Create a new floating topic at the specified point (specified by x and y coordinates) on the specified mind map diagram (specified by ID), and return the newly created floating topic information. A floating topic is a separate topic from the root topic.",
                        this::createFloatingTopic,
                        NewFloatingTopicDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_topic",
                        "Create a new topic under the specified parent topic (specified by ID) on the specified mind map diagram (specified by ID), and return the newly created topic information.",
                        this::createTopic,
                        NewTopicDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_link_between_topics",
                        "Create a new link between the specified source topic (specified by ID) and the specified target topic (specified by ID) on the specified mind map diagram (specified by ID), and return the newly created link presentation information.",
                        this::createTopicLink,
                        NewLinkBetweenTopicsDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "change_parent_of_topic",
                        "Change the parent topic (specified by ID) on the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the changed topic information.",
                        this::changeParentOfTopic,
                        ChangeParentOfTopicDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "move_topic_within_sibling_order",
                        "Move the specified topic (specified by ID) within the order of its sibling topics on the specified mind map diagram (specified by ID), and return the moved topic information.",
                        this::moveTopicWithinSiblingOrder,
                        MoveTopicWithinSiblingOrderDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "insert_svg_img_into_topic",
                        "Insert an SVG image (specified by SVG code) into the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-inserted topic information.",
                        this::insertSvgImageIntoTopic,
                        NewSvgImageIntoTopicDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "insert_png_img_into_topic",
                        "Insert a PNG image (specified by image URL) into the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-inserted topic information.",
                        this::insertPngImageIntoTopic,
                        NewPngImageIntoTopicDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "insert_jpg_img_into_topic",
                        "Insert a JPG image (specified by image URL) into the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-inserted topic information.",
                        this::insertJpgImageIntoTopic,
                        NewJpgImageIntoTopicDTO.class,
                        NodePresentationDTO.class),
    
                ToolSupport.definition(
                        "delete_child_topics",
                        "Delete the child topics of the specified topic (specified by ID) from the specified mind map diagram (specified by ID), and return the parent topic information of the deleted child topics.",
                        this::deleteChildTopics,
                        DeleteChildTopicsDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "delete_img_from_topic",
                        "Delete the image from the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-deleted topic information.",
                        this::deleteImageFromTopic,
                        DeleteImageFromTopicDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "set_boundary_of_topic",
                        "Set the boundary visibility of the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the root topic information within the set boundary. A boundary is an outline that collectively encloses a specific topic and its subordinate topics. It is used to make a particular group of topics stand out or to emphasize it.",
                        this::setBoundaryOfTopic,
                        TopicWithBoundaryVisibilityDTO.class,
                        NodePresentationDTO.class)
        );
    }

    private DiagramDTO createMindmapDiagram(McpSyncServerExchange exchange, NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create mind map diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        try {
            transactionManager.beginTransaction();
            IMindMapDiagram createdAstahMindmapDiagram = mindmapEditor.createMindmapDiagram(astahPackage, param.newDiagramName());
            transactionManager.endTransaction();

            return DiagramDTOAssembler.toDTO(createdAstahMindmapDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO changeToFloatingTopic(McpSyncServerExchange exchange, ChangeToFloatingTopicDTO param) throws Exception {
        log.debug("Change to floating topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.changeToFloatingTopic(astahTopic);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createFloatingTopic(McpSyncServerExchange exchange, NewFloatingTopicDTO param) throws Exception {
        log.debug("Create floating topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahFloatingTopic = mindmapEditor.createFloatingTopic(
                param.newFloatingTopicLabel(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahFloatingTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createTopic(McpSyncServerExchange exchange, NewTopicDTO param) throws Exception {
        log.debug("Create topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahParentTopic = astahProToolSupport.getNodePresentation(param.parentTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahTopic = mindmapEditor.createTopic(astahParentTopic, param.newTopicLabel());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createTopicLink(McpSyncServerExchange exchange, NewLinkBetweenTopicsDTO param) throws Exception {
        log.debug("Create topic link: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahSourceTopic = astahProToolSupport.getNodePresentation(param.sourceTopicId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLink = mindmapEditor.createMMLinkPresentation(astahSourceTopic, astahTargetTopic);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(astahLink);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO changeParentOfTopic(McpSyncServerExchange exchange, ChangeParentOfTopicDTO param) throws Exception {
        log.debug("Change parent of topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());
        INodePresentation astahNewParentTopic = astahProToolSupport.getNodePresentation(param.newParentTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.moveTo(astahTargetTopic, astahNewParentTopic);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTargetTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO moveTopicWithinSiblingOrder(McpSyncServerExchange exchange, MoveTopicWithinSiblingOrderDTO param) throws Exception {
        log.debug("Move topic within sibling order: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());
        INodePresentation astahParentTopic = astahTargetTopic.getParent();

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.moveTo(
                astahTargetTopic,
                astahParentTopic,
                param.newSiblingIndex());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTargetTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO insertSvgImageIntoTopic(McpSyncServerExchange exchange, NewSvgImageIntoTopicDTO param) throws Exception {
        log.debug("Insert SVG image into topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        Image image = imageConvertSupport.svgToImage(param.imageSvgCode());

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setImage(astahTopic, image);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO insertPngImageIntoTopic(McpSyncServerExchange exchange, NewPngImageIntoTopicDTO param) throws Exception {
        log.debug("Insert PNG image into topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setImage(astahTopic, image);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO insertJpgImageIntoTopic(McpSyncServerExchange exchange, NewJpgImageIntoTopicDTO param) throws Exception {
        log.debug("Insert JPG image into topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setImage(astahTopic, image);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO deleteChildTopics(McpSyncServerExchange exchange, DeleteChildTopicsDTO param) throws Exception {
        log.debug("Delete child topics: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetParentTopic = astahProToolSupport.getNodePresentation(param.targetParentTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.deleteChildren(astahTargetParentTopic);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTargetParentTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO deleteImageFromTopic(McpSyncServerExchange exchange, DeleteImageFromTopicDTO param) throws Exception {
        log.debug("Delete image from topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.deleteImage(astahTargetTopic);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTargetTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO setBoundaryOfTopic(McpSyncServerExchange exchange, TopicWithBoundaryVisibilityDTO param) throws Exception {
        log.debug("Set boundary of topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setBoundaryVisibility(astahTargetTopic, param.boundaryVisibility());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahTargetTopic);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
