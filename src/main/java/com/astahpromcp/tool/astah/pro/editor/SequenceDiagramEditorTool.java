package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.SequenceDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.SequenceDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO.Type;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
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

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/SequenceDiagramEditor.html
@Slf4j
public class SequenceDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final SequenceDiagramEditor sequenceDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public SequenceDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, SequenceDiagramEditor sequenceDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.sequenceDiagramEditor = sequenceDiagramEditor;
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
            log.error("Failed to create sequence diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_seq_dgm",
                "Create a new sequence diagram on the specified package (specified by ID), and return the newly created model element of the sequence diagram.",
                this::createSequenceDiagram,
                NewSequenceDiagramInPackageDTO.class,
                SequenceDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_combined_fragment",
                "Create a new combined fragment on the specified sequence diagram (specified by ID), and return the newly created node presentation of the combined fragment along with the updated diagram image.",
                this::createCombinedFragment,
                NewCombinedFragmentDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_msg",
                "Create a new message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the message along with the updated diagram image. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createMessage,
                NewMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_create_msg",
                "Create a new create message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the create message along with the updated diagram image. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createCreateMessage,
                NewCreateMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_destroy_msg",
                "Create a new destroy message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the destroy message along with the updated diagram image. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createDestroyMessage,
                NewDestroyMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_return_msg",
                "Create a new return message to the specified message (specified by ID) on the specified sequence diagram (specified by ID), and return the newly created link presentation of the return message along with the updated diagram image.",
                this::createReturnMessage,
                NewReturnMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_lost_msg",
                "Create a new lost message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the lost message along with the updated diagram image. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createLostMessage,
                NewLostMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_found_msg",
                "Create a new found message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the found message along with the updated diagram image. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message.",
                this::createFoundMessage,
                NewFoundMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_interaction_use",
                "Create a new interaction use on the specified sequence diagram (specified by ID), and return the newly created node presentation of the interaction use along with the updated diagram image. Note that the InteractionUse to be created must cover at least one lifeline. In other words, attempting to create an InteractionUse in an area where no lifelines exist will result in failure.",
                this::createInteractionUse,
                NewInteractionUseDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_lifeline",
                "Create a new lifeline on the specified sequence diagram (specified by ID), and return the newly created node presentation of the lifeline along with the updated diagram image.",
                this::createLifeline,
                NewLifelineDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_termination",
                "Create a new termination on the specified sequence diagram (specified by ID), and return the newly created node presentation of the termination along with the updated diagram image.",
                this::createTermination,
                NewTerminationDTO.class,
                NodePresentationDTO.class)
        );
    }

    private SequenceDiagramDTO createSequenceDiagram(McpSyncServerExchange exchange, NewSequenceDiagramInPackageDTO param) throws Exception {
        log.debug("Create sequence diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            ISequenceDiagram createdAstahSequenceDiagram = sequenceDiagramEditor.createSequenceDiagram(
                astahPackage,
                param.newSequenceDiagramName());
            transactionManager.endTransaction();

            return SequenceDiagramDTOAssembler.toDTO(createdAstahSequenceDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createCombinedFragment(McpSyncServerExchange exchange, NewCombinedFragmentDTO param) throws Exception {
        log.debug("Create combined fragment: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation combinedFragment = sequenceDiagramEditor.createCombinedFragment(
                param.newCombinedFragmentName(),
                param.combinedFragmentKind().astahValue,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()),
                param.width(),
                param.height());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(combinedFragment);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createMessage(McpSyncServerExchange exchange, NewMessageDTO param) throws Exception {
        log.debug("Create message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        if (senderNode.getType() != Type.ACTIVATION.typeName
            && senderNode.getType() != Type.LIFELINE.typeName
            && senderNode.getType() != Type.INTERACTION_USE.typeName
            && senderNode.getType() != Type.FRAME.typeName) {
            throw new IllegalArgumentException("Message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        if (receiverNode.getType() != Type.LIFELINE.typeName
            && receiverNode.getType() != Type.INTERACTION_USE.typeName
            && receiverNode.getType() != Type.FRAME.typeName) {
            throw new IllegalArgumentException("Message receiver node must be one of the following node presentation types: Lifeline, InteractionUse, or Frame.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation message = sequenceDiagramEditor.createMessage(
                param.newMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(message);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createCreateMessage(McpSyncServerExchange exchange, NewCreateMessageDTO param) throws Exception {
        log.debug("Create create message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        if (senderNode.getType() != Type.ACTIVATION.typeName
            && senderNode.getType() != Type.LIFELINE.typeName
            && senderNode.getType() != Type.INTERACTION_USE.typeName
            && senderNode.getType() != Type.FRAME.typeName) {
            throw new IllegalArgumentException("Create message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        if (receiverNode.getType() != Type.LIFELINE.typeName) {
            throw new IllegalArgumentException("Create message receiver node must be one of the following node presentation types: Lifeline.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation createMessage = sequenceDiagramEditor.createCreateMessage(
                param.newCreateMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(createMessage);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createDestroyMessage(McpSyncServerExchange exchange, NewDestroyMessageDTO param) throws Exception {
        log.debug("Create destroy message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        if (senderNode.getType() != Type.ACTIVATION.typeName
            && senderNode.getType() != Type.LIFELINE.typeName
            && senderNode.getType() != Type.INTERACTION_USE.typeName
            && senderNode.getType() != Type.FRAME.typeName) {
            throw new IllegalArgumentException("Destroy message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        if (receiverNode.getType() != Type.LIFELINE.typeName) {
            throw new IllegalArgumentException("Destroy message receiver node must be one of the following node presentation types: Lifeline.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation destroyMessage = sequenceDiagramEditor.createDestroyMessage(
                param.newDestroyMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(destroyMessage);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createReturnMessage(McpSyncServerExchange exchange, NewReturnMessageDTO param) throws Exception {
        log.debug("Create return message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        ILinkPresentation targetMessage = astahProToolSupport.getLinkPresentation(param.targetMessageId());
        if (targetMessage.getType() != Type.MESSAGE.typeName) {
            throw new IllegalArgumentException("Target message for return message must be one of the following link presentation types: Message.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation returnMessage = sequenceDiagramEditor.createReturnMessage(
                param.newReturnMessageName(),
                targetMessage);
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(returnMessage);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createLostMessage(McpSyncServerExchange exchange, NewLostMessageDTO param) throws Exception {
        log.debug("Create lost message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        if (senderNode.getType() != Type.ACTIVATION.typeName
            && senderNode.getType() != Type.LIFELINE.typeName
            && senderNode.getType() != Type.INTERACTION_USE.typeName
            && senderNode.getType() != Type.FRAME.typeName) {
            throw new IllegalArgumentException("Lost message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation lostMessage = sequenceDiagramEditor.createLostMessage(
                param.newLostMessageName(),
                senderNode,
                new Point2D.Double(param.endPointX(), param.endPointY()));
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(lostMessage);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createFoundMessage(McpSyncServerExchange exchange, NewFoundMessageDTO param) throws Exception {
        log.debug("Create found message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        if (receiverNode.getType() != Type.LIFELINE.typeName
            && receiverNode.getType() != Type.INTERACTION_USE.typeName
            && receiverNode.getType() != Type.FRAME.typeName) {
            throw new IllegalArgumentException("Found message receiver node must be one of the following node presentation types: Lifeline, InteractionUse, or Frame.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation foundMessage = sequenceDiagramEditor.createFoundMessage(
                param.newFoundMessageName(),
                new Point2D.Double(param.startPointX(), param.startPointY()),
                receiverNode);
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(foundMessage);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createInteractionUse(McpSyncServerExchange exchange, NewInteractionUseDTO param) throws Exception {
        log.debug("Create interaction use: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation interactionUse = sequenceDiagramEditor.createInteractionUse(
                param.newInteractionUseName(),
                "",
                null,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()),
                param.width(),
                param.height());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(interactionUse);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createLifeline(McpSyncServerExchange exchange, NewLifelineDTO param) throws Exception {
        log.debug("Create lifeline: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation lifeline = sequenceDiagramEditor.createLifeline(
                param.newLifelineName(),
                param.locationX());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(lifeline);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createTermination(McpSyncServerExchange exchange, NewTerminationDTO param) throws Exception {
        log.debug("Create termination: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation targetNode = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation termination = sequenceDiagramEditor.createTermination(
                targetNode);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(termination);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetSequenceDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
