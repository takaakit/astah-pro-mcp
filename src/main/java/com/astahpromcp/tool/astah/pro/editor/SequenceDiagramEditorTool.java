package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.SequenceDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.SequenceDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/SequenceDiagramEditor.html
@Slf4j
public class SequenceDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final SequenceDiagramEditor sequenceDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public SequenceDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, SequenceDiagramEditor sequenceDiagramEditor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.sequenceDiagramEditor = sequenceDiagramEditor;
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
            log.error("Failed to create sequence diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_seq_dgm",
                        "Create a new sequence diagram on the specified package (specified by ID), and return the newly created sequence diagram information.",
                        this::createSequenceDiagram,
                        NewSequenceDiagramInPackageDTO.class,
                        SequenceDiagramDTO.class),

                ToolSupport.definition(
                        "create_combined_fragment",
                        "Create a new combined fragment on the specified sequence diagram (specified by ID), and return the newly created combined fragment information.",
                        this::createCombinedFragment,
                        NewCombinedFragmentDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_msg",
                        "Create a new message on the specified sequence diagram (specified by ID), and return the newly created message information.",
                        this::createMessage,
                        NewMessageDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_create_msg",
                        "Create a new create message on the specified sequence diagram (specified by ID), and return the newly created message information.",
                        this::createCreateMessage,
                        NewCreateMessageDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_destroy_msg",
                        "Create a new destroy message on the specified sequence diagram (specified by ID), and return the newly created message information.",
                        this::createDestroyMessage,
                        NewDestroyMessageDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_return_msg",
                        "Create a new return message to the specified message (specified by ID) on the specified sequence diagram (specified by ID), and return the newly created message information.",
                        this::createReturnMessage,
                        NewReturnMessageDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_lost_msg",
                        "Create a new lost message on the specified sequence diagram (specified by ID), and return the newly created message information.",
                        this::createLostMessage,
                        NewLostMessageDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_found_msg",
                        "Create a new found message on the specified sequence diagram (specified by ID), and return the newly created message information.",
                        this::createFoundMessage,
                        NewFoundMessageDTO.class,
                        LinkPresentationDTO.class),

                ToolSupport.definition(
                        "create_interaction_use",
                        "Create a new interaction use on the specified sequence diagram (specified by ID), and return the newly created interaction use information. Note that the InteractionUse to be created must cover at least one lifeline. In other words, attempting to create an InteractionUse in an area where no lifelines exist will result in failure.",
                        this::createInteractionUse,
                        NewInteractionUseDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_lifeline",
                        "Create a new lifeline on the specified sequence diagram (specified by ID), and return the newly created lifeline information.",
                        this::createLifeline,
                        NewLifelineDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_termination",
                        "Create a new termination on the specified sequence diagram (specified by ID), and return the newly created termination information.",
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

    private NodePresentationDTO createCombinedFragment(McpSyncServerExchange exchange, NewCombinedFragmentDTO param) throws Exception {
        log.debug("Create combined fragment: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation combinedFragment = sequenceDiagramEditor.createCombinedFragment(
                param.newCombinedFragmentName(),
                param.combinedFragmentKind().toAstahValue(),
                new Point2D.Double(
                        param.locationX(),
                        param.locationY()),
                param.width(),
                param.height());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(combinedFragment);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createMessage(McpSyncServerExchange exchange, NewMessageDTO param) throws Exception {
        log.debug("Create message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation message = sequenceDiagramEditor.createMessage(
                param.newMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(message);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createCreateMessage(McpSyncServerExchange exchange, NewCreateMessageDTO param) throws Exception {
        log.debug("Create create message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation createMessage = sequenceDiagramEditor.createCreateMessage(
                param.newCreateMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(createMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createDestroyMessage(McpSyncServerExchange exchange, NewDestroyMessageDTO param) throws Exception {
        log.debug("Create destroy message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation senderNode = astahProToolSupport.getNodePresentation(param.senderNodePresentationId());
        INodePresentation receiverNode = astahProToolSupport.getNodePresentation(param.receiverNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation destroyMessage = sequenceDiagramEditor.createDestroyMessage(
                param.newDestroyMessageName(),
                senderNode,
                receiverNode,
                param.locationY());
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(destroyMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createReturnMessage(McpSyncServerExchange exchange, NewReturnMessageDTO param) throws Exception {
        log.debug("Create return message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        ILinkPresentation targetMessage = astahProToolSupport.getLinkPresentation(param.targetMessageId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation returnMessage = sequenceDiagramEditor.createReturnMessage(
                param.newReturnMessageName(),
                targetMessage);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(returnMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createLostMessage(McpSyncServerExchange exchange, NewLostMessageDTO param) throws Exception {
        log.debug("Create lost message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation targetNode = astahProToolSupport.getNodePresentation(param.targetSenderNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation lostMessage = sequenceDiagramEditor.createLostMessage(
                param.newLostMessageName(),
                targetNode,
                new Point2D.Double(param.endPointX(), param.endPointY()));
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(lostMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkPresentationDTO createFoundMessage(McpSyncServerExchange exchange, NewFoundMessageDTO param) throws Exception {
        log.debug("Create found message: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation targetNode = astahProToolSupport.getNodePresentation(param.targetReceiverNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation foundMessage = sequenceDiagramEditor.createFoundMessage(
                param.newFoundMessageName(),
                new Point2D.Double(param.startPointX(), param.startPointY()),
                targetNode);
            transactionManager.endTransaction();

            return LinkPresentationDTOAssembler.toDTO(foundMessage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createInteractionUse(McpSyncServerExchange exchange, NewInteractionUseDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(interactionUse);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createLifeline(McpSyncServerExchange exchange, NewLifelineDTO param) throws Exception {
        log.debug("Create lifeline: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation lifeline = sequenceDiagramEditor.createLifeline(
                param.newLifelineName(),
                param.locationX());
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(lifeline);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createTermination(McpSyncServerExchange exchange, NewTerminationDTO param) throws Exception {
        log.debug("Create termination: {}", param);

        ISequenceDiagram astahSequenceDiagram = (ISequenceDiagram) astahProToolSupport.getDiagram(param.targetSequenceDiagramId());
        INodePresentation targetNode = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        sequenceDiagramEditor.setDiagram(astahSequenceDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation termination = sequenceDiagramEditor.createTermination(
                targetNode);
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(termination);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
