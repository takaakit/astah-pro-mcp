package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
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
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
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
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/MindmapEditor.html
@Slf4j
public class MindmapEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final MindmapEditor mindmapEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageConvertSupport imageConvertSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public MindmapEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, MindmapEditor mindmapEditor, AstahProToolSupport astahProToolSupport, ImageConvertSupport imageConvertSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.mindmapEditor = mindmapEditor;
        this.astahProToolSupport = astahProToolSupport;
        this.imageConvertSupport = imageConvertSupport;
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
            log.error("Failed to create mind map editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_mind_map_dgm",
                "Create a new mind map diagram under the specified package (specified by ID), and return the information of newly created mind map diagram. The diagram name is also used as the root topic label.",
                this::createMindmapDiagram,
                NewDiagramInPackageDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_to_floating_topic",
                "Change the specified topic (specified by ID) to a floating topic on the specified mind map diagram (specified by ID), and return the changed topic information along with the updated diagram image. A floating topic is a separate topic from the root topic.",
                this::changeToFloatingTopic,
                ChangeToFloatingTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_floating_topic",
                "Create a new floating topic at the specified point (specified by x and y coordinates) on the specified mind map diagram (specified by ID), and return the newly created floating topic information along with the updated diagram image. A floating topic is a separate topic from the root topic.",
                this::createFloatingTopic,
                NewFloatingTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_topic",
                "Create a new topic under the specified parent topic (specified by ID) on the specified mind map diagram (specified by ID), and return the newly created topic information along with the updated diagram image.",
                this::createTopic,
                NewTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_link_between_topics",
                "Create a new link between the specified source topic (specified by ID) and the specified target topic (specified by ID) on the specified mind map diagram (specified by ID), and return the newly created link presentation information along with the updated diagram image.",
                this::createTopicLink,
                NewLinkBetweenTopicsDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_parent_of_topic",
                "Change the parent topic (specified by ID) on the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the changed topic information along with the updated diagram image.",
                this::changeParentOfTopic,
                ChangeParentOfTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "move_topic_within_sibling_order",
                "Move the specified topic (specified by ID) within the order of its sibling topics on the specified mind map diagram (specified by ID), and return the moved topic information along with the updated diagram image.",
                this::moveTopicWithinSiblingOrder,
                MoveTopicWithinSiblingOrderDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_svg_img_into_topic",
                "Insert an SVG image (specified by SVG code) into the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-inserted topic information along with the updated diagram image.",
                this::insertSvgImageIntoTopic,
                NewSvgImageIntoTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_png_img_into_topic",
                "Insert a PNG image (specified by image URL) into the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-inserted topic information along with the updated diagram image.",
                this::insertPngImageIntoTopic,
                NewPngImageIntoTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "insert_jpg_img_into_topic",
                "Insert a JPG image (specified by image URL) into the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-inserted topic information along with the updated diagram image.",
                this::insertJpgImageIntoTopic,
                NewJpgImageIntoTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "delete_child_topics",
                "Delete the child topics of the specified topic (specified by ID) from the specified mind map diagram (specified by ID), and return the parent topic information of the deleted child topics along with the updated diagram image.",
                this::deleteChildTopics,
                DeleteChildTopicsDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "delete_img_from_topic",
                "Delete the image from the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the image-deleted topic information along with the updated diagram image.",
                this::deleteImageFromTopic,
                DeleteImageFromTopicDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_boundary_of_topic",
                "Set the boundary visibility of the specified topic (specified by ID) on the specified mind map diagram (specified by ID), and return the root topic information within the set boundary along with the updated diagram image. A boundary is an outline that collectively encloses a specific topic and its subordinate topics. It is used to make a particular group of topics stand out or to emphasize it.",
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

    private Pair<NodePresentationDTO, List<McpSchema.Content>> changeToFloatingTopic(McpSyncServerExchange exchange, ChangeToFloatingTopicDTO param) throws Exception {
        log.debug("Change to floating topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.changeToFloatingTopic(astahTopic);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createFloatingTopic(McpSyncServerExchange exchange, NewFloatingTopicDTO param) throws Exception {
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

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahFloatingTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createTopic(McpSyncServerExchange exchange, NewTopicDTO param) throws Exception {
        log.debug("Create topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahParentTopic = astahProToolSupport.getNodePresentation(param.parentTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahTopic = mindmapEditor.createTopic(astahParentTopic, param.newTopicLabel());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createTopicLink(McpSyncServerExchange exchange, NewLinkBetweenTopicsDTO param) throws Exception {
        log.debug("Create topic link: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahSourceTopic = astahProToolSupport.getNodePresentation(param.sourceTopicId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLink = mindmapEditor.createMMLinkPresentation(astahSourceTopic, astahTargetTopic);
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLink);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> changeParentOfTopic(McpSyncServerExchange exchange, ChangeParentOfTopicDTO param) throws Exception {
        log.debug("Change parent of topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());
        INodePresentation astahNewParentTopic = astahProToolSupport.getNodePresentation(param.newParentTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.moveTo(astahTargetTopic, astahNewParentTopic);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTargetTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> moveTopicWithinSiblingOrder(McpSyncServerExchange exchange, MoveTopicWithinSiblingOrderDTO param) throws Exception {
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

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTargetTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> insertSvgImageIntoTopic(McpSyncServerExchange exchange, NewSvgImageIntoTopicDTO param) throws Exception {
        log.debug("Insert SVG image into topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        Image image = imageConvertSupport.svgToImage(param.imageSvgCode());

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setImage(astahTopic, image);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTopic);

            McpSchema.ImageContent diagramImage = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(diagramImage));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> insertPngImageIntoTopic(McpSyncServerExchange exchange, NewPngImageIntoTopicDTO param) throws Exception {
        log.debug("Insert PNG image into topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setImage(astahTopic, image);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTopic);

            McpSchema.ImageContent diagramImage = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(diagramImage));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> insertJpgImageIntoTopic(McpSyncServerExchange exchange, NewJpgImageIntoTopicDTO param) throws Exception {
        log.debug("Insert JPG image into topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        Image image = imageConvertSupport.urlToImage(param.imageUrl());

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setImage(astahTopic, image);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTopic);

            McpSchema.ImageContent diagramImage = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(diagramImage));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> deleteChildTopics(McpSyncServerExchange exchange, DeleteChildTopicsDTO param) throws Exception {
        log.debug("Delete child topics: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetParentTopic = astahProToolSupport.getNodePresentation(param.targetParentTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.deleteChildren(astahTargetParentTopic);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTargetParentTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> deleteImageFromTopic(McpSyncServerExchange exchange, DeleteImageFromTopicDTO param) throws Exception {
        log.debug("Delete image from topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.deleteImage(astahTargetTopic);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTargetTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> setBoundaryOfTopic(McpSyncServerExchange exchange, TopicWithBoundaryVisibilityDTO param) throws Exception {
        log.debug("Set boundary of topic: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.targetDiagramId());
        INodePresentation astahTargetTopic = astahProToolSupport.getNodePresentation(param.targetTopicId());

        mindmapEditor.setDiagram(astahMindMapDiagram);

        try {
            transactionManager.beginTransaction();
            mindmapEditor.setBoundaryVisibility(astahTargetTopic, param.boundaryVisibility());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTargetTopic);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
