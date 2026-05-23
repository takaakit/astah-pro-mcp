package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.StateMachineDiagramEditor;
import com.change_vision.jude.api.inf.model.*;
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
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/StateMachineDiagramEditor.html
@Slf4j
public class StateMachineDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final StateMachineDiagramEditor stateMachineDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public StateMachineDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, StateMachineDiagramEditor stateMachineDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.stateMachineDiagramEditor = stateMachineDiagramEditor;
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
            log.error("Failed to create state machine diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "add_region",
                "Add a new region in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the parent node presentation information along with the updated diagram image.",
                this::addRegion,
                NewRegionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "delete_region",
                "Delete the specified region (specified by index) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the parent node presentation information along with the updated diagram image.",
                this::deleteRegion,
                DeleteRegionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_parent_state",
                "Change the parent state (specified by ID) of the specified state (specified by ID) on the specified state machine diagram (specified by ID), and return the parent-changed state information along with the updated diagram image. If there is no parent state (i.e., when rendering at the top level), set the parent state ID to an empty string.",
                this::changeParentOfState,
                ChangeParentStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_choice_pseudostate",
                "Create a new choice pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created choice pseudostate information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createChoicePseudostate,
                NewChoicePseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_deep_history_pseudostate",
                "Create a new deep history pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created deep history pseudostate information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createDeepHistoryPseudostate,
                NewDeepHistoryPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_shallow_history_pseudostate",
                "Create a new shallow history pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created shallow history pseudostate information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createShallowHistoryPseudostate,
                NewShallowHistoryPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_final_state",
                "Create a new final state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created final state information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createFinalState,
                NewFinalStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_fork_pseudostate",
                "Create a new fork pseudostate of the specified size (specified by width and height) at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created fork pseudostate information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createForkPseudostate,
                NewForkPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_init_pseudostate",
                "Create a new initial pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created initial pseudostate information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createInitialPseudostate,
                NewInitialPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_join_pseudostate",
                "Create a new join pseudostate of the specified size (specified by width and height) at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created join pseudostate information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createJoinPseudostate,
                NewJoinPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_junction_pseudostate",
                "Create a new junction pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created junction pseudostate information along with the updated diagram image. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createJunctionPseudostate,
                NewJunctionPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_state",
                "Create a new state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created state information along with the updated diagram image. If there is no parent state (i.e., when rendering at the top level), set the parent state ID to an empty string.",
                this::createState,
                NewStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_state_machine_dgm",
                "Create a new state machine diagram under the specified package (specified by ID), and return the newly created state machine diagram information.",
                this::createStateMachineDiagram,
                NewStateMachineDiagramDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_sub_machine_state",
                "Create a new sub machine state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created sub machine state information along with the updated diagram image.",
                this::createSubMachineState,
                NewSubMachineStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_transition",
                "Create a new transition between the specified source state (specified by ID) and the specified target state (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created transition information along with the updated diagram image.",
                this::createTransition,
                NewTransitionDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> addRegion(McpSyncServerExchange exchange, NewRegionDTO param) throws Exception {
        log.debug("Add region: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            stateMachineDiagramEditor.addRegion(
                astahParentNodePresentation,
                param.isHorizontal());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> deleteRegion(McpSyncServerExchange exchange, DeleteRegionDTO param) throws Exception {
        log.debug("Delete region: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            stateMachineDiagramEditor.deleteRegion(
                astahParentNodePresentation,
                param.index());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> changeParentOfState(McpSyncServerExchange exchange, ChangeParentStateDTO param) throws Exception {
        log.debug("Change parent of state: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetStateId());

        INodePresentation astahParentNodePresentation;
        if (param.parentStateId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentStateId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            stateMachineDiagramEditor.changeParentOfState(
                astahTargetNodePresentation,
                astahParentNodePresentation);
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTargetNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createChoicePseudostate(McpSyncServerExchange exchange, NewChoicePseudostateDTO param) throws Exception {
        log.debug("Create choice pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahChoicePseudostate = stateMachineDiagramEditor.createChoicePseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahChoicePseudostate);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createDeepHistoryPseudostate(McpSyncServerExchange exchange, NewDeepHistoryPseudostateDTO param) throws Exception {
        log.debug("Create deep history pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahDeepHistoryPseudostate = stateMachineDiagramEditor.createDeepHistoryPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahDeepHistoryPseudostate);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createShallowHistoryPseudostate(McpSyncServerExchange exchange, NewShallowHistoryPseudostateDTO param) throws Exception {
        log.debug("Create shallow history pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahShallowHistoryPseudostate = stateMachineDiagramEditor.createShallowHistoryPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahShallowHistoryPseudostate);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createFinalState(McpSyncServerExchange exchange, NewFinalStateDTO param) throws Exception {
        log.debug("Create final state: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahFinalState = stateMachineDiagramEditor.createFinalState(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahFinalState);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createForkPseudostate(McpSyncServerExchange exchange, NewForkPseudostateDTO param) throws Exception {
        log.debug("Create fork pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahForkPseudostate = stateMachineDiagramEditor.createForkPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()),
                param.width(),
                param.height());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahForkPseudostate);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createInitialPseudostate(McpSyncServerExchange exchange, NewInitialPseudostateDTO param) throws Exception {
        log.debug("Create initial pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahInitialPseudostate = stateMachineDiagramEditor.createInitialPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahInitialPseudostate);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createJoinPseudostate(McpSyncServerExchange exchange, NewJoinPseudostateDTO param) throws Exception {
        log.debug("Create join pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahJoinPseudostate = stateMachineDiagramEditor.createJoinPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()),
                param.width(),
                param.height());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahJoinPseudostate);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createJunctionPseudostate(McpSyncServerExchange exchange, NewJunctionPseudostateDTO param) throws Exception {
        log.debug("Create junction pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahJunctionPseudostate = stateMachineDiagramEditor.createJunctionPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahJunctionPseudostate);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createState(McpSyncServerExchange exchange, NewStateDTO param) throws Exception {
        log.debug("Create state: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahState = stateMachineDiagramEditor.createState(
                param.newStateName(),
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahState);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DiagramDTO createStateMachineDiagram(McpSyncServerExchange exchange, NewStateMachineDiagramDTO param) throws Exception {
        log.debug("Create state machine diagram: {}", param);

        INamedElement astahParentNamedElement = astahProToolSupport.getNamedElement(param.parentNamedElementId());

        try {
            transactionManager.beginTransaction();
            IStateMachineDiagram astahStateMachineDiagram = stateMachineDiagramEditor.createStatemachineDiagram(
                astahParentNamedElement,
                param.newStateMachineDiagramName());
            transactionManager.endTransaction();

            return DiagramDTOAssembler.toDTO(astahStateMachineDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createSubMachineState(McpSyncServerExchange exchange, NewSubMachineStateDTO param) throws Exception {
        log.debug("Create sub machine state: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        IStateMachineDiagram astahSubMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.subMachineDiagramId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            stateMachineDiagramEditor.createSubmachineState(
                astahParentNodePresentation,
                astahSubMachineDiagram,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createTransition(McpSyncServerExchange exchange, NewTransitionDTO param) throws Exception {
        log.debug("Create transition: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLinkPresentation = stateMachineDiagramEditor.createTransition(
                astahSourceNodePresentation,
                astahTargetNodePresentation);
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
