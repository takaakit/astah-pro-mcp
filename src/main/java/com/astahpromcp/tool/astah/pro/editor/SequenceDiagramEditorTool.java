package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.SequenceDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.SequenceDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO.Type;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
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
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/SequenceDiagramEditor.html
@Slf4j
public class SequenceDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final SequenceDiagramEditor sequenceDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public SequenceDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, SequenceDiagramEditor sequenceDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
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
                "Create a new combined fragment on the specified sequence diagram (specified by ID), and return the newly created node presentation of the combined fragment along with the updated diagram image in low resolution.",
                this::createCombinedFragment,
                NewCombinedFragmentDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_msg",
                "Create a new message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the message along with the updated diagram image in low resolution. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createMessage,
                NewMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_create_msg",
                "Create a new create message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the create message along with the updated diagram image in low resolution. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createCreateMessage,
                NewCreateMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_destroy_msg",
                "Create a new destroy message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the destroy message along with the updated diagram image in low resolution. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createDestroyMessage,
                NewDestroyMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_return_msg",
                "Create a new return message to the specified message (specified by ID) on the specified sequence diagram (specified by ID), and return the newly created link presentation of the return message along with the updated diagram image in low resolution.",
                this::createReturnMessage,
                NewReturnMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_lost_msg",
                "Create a new lost message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the lost message along with the updated diagram image in low resolution. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender.",
                this::createLostMessage,
                NewLostMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_found_msg",
                "Create a new found message on the specified sequence diagram (specified by ID), and return the newly created link presentation of the found message along with the updated diagram image in low resolution. It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message.",
                this::createFoundMessage,
                NewFoundMessageDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_interaction_use",
                "Create a new interaction use on the specified sequence diagram (specified by ID), and return the newly created node presentation of the interaction use along with the updated diagram image in low resolution. Note that the InteractionUse to be created must cover at least one lifeline. In other words, attempting to create an InteractionUse in an area where no lifelines exist will result in failure.",
                this::createInteractionUse,
                NewInteractionUseDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_lifeline",
                "Create a new lifeline on the specified sequence diagram (specified by ID), and return the newly created node presentation of the lifeline along with the updated diagram image in low resolution.",
                this::createLifeline,
                NewLifelineDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_termination",
                "Create a new termination on the specified sequence diagram (specified by ID), and return the newly created node presentation of the termination along with the updated diagram image in low resolution.",
                this::createTermination,
                NewTerminationDTO.class,
                NodePresentationDTO.class)
        );
    }

    private SequenceDiagramDTO createSequenceDiagram(NewSequenceDiagramInPackageDTO param) throws Exception {
        log.debug("Create sequence diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        ISequenceDiagram createdAstahSequenceDiagram = txnAstah.call( () -> {
            return sequenceDiagramEditor.createSequenceDiagram(
                astahPackage,
                param.newSequenceDiagramName());
        });

        return SequenceDiagramDTOAssembler.toDTO(createdAstahSequenceDiagram);
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createCombinedFragment(NewCombinedFragmentDTO param) throws Exception {
        log.debug("Create combined fragment: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        INodePresentation combinedFragment = txnAstah.call( () -> {
            return sequenceDiagramEditor.createCombinedFragment(
                param.newCombinedFragmentName(),
                param.combinedFragmentKind().astahValue,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()),
                param.width(),
                param.height());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(combinedFragment);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createMessage(NewMessageDTO param) throws Exception {
        log.debug("Create message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        String senderType = senderNode.getType();
        if (!Type.ACTIVATION.matches(senderType)
            && !Type.LIFELINE.matches(senderType)
            && !Type.INTERACTION_USE.matches(senderType)
            && !Type.FRAME.matches(senderType)) {
            throw new IllegalArgumentException("Message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        String receiverType = receiverNode.getType();
        if (!Type.LIFELINE.matches(receiverType)
            && !Type.INTERACTION_USE.matches(receiverType)
            && !Type.FRAME.matches(receiverType)) {
            throw new IllegalArgumentException("Message receiver node must be one of the following node presentation types: Lifeline, InteractionUse, or Frame.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        ILinkPresentation message = txnAstah.call( () -> {
            return sequenceDiagramEditor.createMessage(
                param.newMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(message);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createCreateMessage(NewCreateMessageDTO param) throws Exception {
        log.debug("Create create message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        String senderType = senderNode.getType();
        if (!Type.ACTIVATION.matches(senderType)
            && !Type.LIFELINE.matches(senderType)
            && !Type.INTERACTION_USE.matches(senderType)
            && !Type.FRAME.matches(senderType)) {
            throw new IllegalArgumentException("Create message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        if (!Type.LIFELINE.matches(receiverNode.getType())) {
            throw new IllegalArgumentException("Create message receiver node must be one of the following node presentation types: Lifeline.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        ILinkPresentation createMessage = txnAstah.call( () -> {
            return sequenceDiagramEditor.createCreateMessage(
                param.newCreateMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(createMessage);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createDestroyMessage(NewDestroyMessageDTO param) throws Exception {
        log.debug("Create destroy message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        String senderType = senderNode.getType();
        if (!Type.ACTIVATION.matches(senderType)
            && !Type.LIFELINE.matches(senderType)
            && !Type.INTERACTION_USE.matches(senderType)
            && !Type.FRAME.matches(senderType)) {
            throw new IllegalArgumentException("Destroy message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        if (!Type.LIFELINE.matches(receiverNode.getType())) {
            throw new IllegalArgumentException("Destroy message receiver node must be one of the following node presentation types: Lifeline.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        ILinkPresentation destroyMessage = txnAstah.call( () -> {
            return sequenceDiagramEditor.createDestroyMessage(
                param.newDestroyMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(destroyMessage);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createReturnMessage(NewReturnMessageDTO param) throws Exception {
        log.debug("Create return message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        ILinkPresentation targetMessage = astahProToolSupport.getLinkPresentation(param.targetMessageId());
        if (!Type.MESSAGE.matches(targetMessage.getType())) {
            throw new IllegalArgumentException("Target message for return message must be one of the following link presentation types: Message.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        ILinkPresentation returnMessage = txnAstah.call( () -> {
            return sequenceDiagramEditor.createReturnMessage(
                param.newReturnMessageName(),
                targetMessage);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(returnMessage);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createLostMessage(NewLostMessageDTO param) throws Exception {
        log.debug("Create lost message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        String senderType = senderNode.getType();
        if (!Type.ACTIVATION.matches(senderType)
            && !Type.LIFELINE.matches(senderType)
            && !Type.INTERACTION_USE.matches(senderType)
            && !Type.FRAME.matches(senderType)) {
            throw new IllegalArgumentException("Lost message sender node must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        ILinkPresentation lostMessage = txnAstah.call( () -> {
            return sequenceDiagramEditor.createLostMessage(
                param.newLostMessageName(),
                senderNode,
                new Point2D.Double(param.endPointX(), param.endPointY()));
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(lostMessage);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createFoundMessage(NewFoundMessageDTO param) throws Exception {
        log.debug("Create found message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());
        String receiverType = receiverNode.getType();
        if (!Type.LIFELINE.matches(receiverType)
            && !Type.INTERACTION_USE.matches(receiverType)
            && !Type.FRAME.matches(receiverType)) {
            throw new IllegalArgumentException("Found message receiver node must be one of the following node presentation types: Lifeline, InteractionUse, or Frame.");
        }

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        ILinkPresentation foundMessage = txnAstah.call( () -> {
            return sequenceDiagramEditor.createFoundMessage(
                param.newFoundMessageName(),
                new Point2D.Double(param.startPointX(), param.startPointY()),
                receiverNode);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(foundMessage);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createInteractionUse(NewInteractionUseDTO param) throws Exception {
        log.debug("Create interaction use: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        INodePresentation interactionUse = txnAstah.call( () -> {
            return sequenceDiagramEditor.createInteractionUse(
                param.newInteractionUseName(),
                "",
                null,
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()),
                param.width(),
                param.height());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(interactionUse);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createLifeline(NewLifelineDTO param) throws Exception {
        log.debug("Create lifeline: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        INodePresentation lifeline = txnAstah.call( () -> {
            return sequenceDiagramEditor.createLifeline(
                param.newLifelineName(),
                param.locationX());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(lifeline);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createTermination(NewTerminationDTO param) throws Exception {
        log.debug("Create termination: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation targetNode = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        INodePresentation termination = txnAstah.call( () -> {
            return sequenceDiagramEditor.createTermination(
                targetNode);
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(termination);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetSequenceDiagramId());

        return Pair.of(dto, List.of(image));
    }
}
