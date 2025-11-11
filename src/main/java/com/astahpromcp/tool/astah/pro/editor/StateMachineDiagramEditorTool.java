package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.TransitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.TransitionDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.StateMachineDiagramEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/StateMachineDiagramEditor.html
@Slf4j
public class StateMachineDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final StateMachineDiagramEditor stateMachineDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public StateMachineDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, StateMachineDiagramEditor stateMachineDiagramEditor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.stateMachineDiagramEditor = stateMachineDiagramEditor;
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
            log.error("Failed to create state machine diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "add_reg",
                        "Add a new region in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the parent node presentation information.",
                        this::addRegion,
                        NewRegionDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "delete_reg",
                        "Delete the specified region (specified by index) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the parent node presentation information.",
                        this::deleteRegion,
                        DeleteRegionDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "change_prt_of_state",
                        "Change the parent state (specified by ID) of the specified state (specified by ID) on the specified state machine diagram (specified by ID), and return the parent-changed state information. If there is no parent state (i.e., when rendering at the top level), set the parent state ID to an empty string.",
                        this::changeParentOfState,
                        ChangeParentStateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_choic_pse",
                        "Create a new choice pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created choice pseudostate information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createChoicePseudostate,
                        NewChoicePseudostateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_deep_his_pse",
                        "Create a new deep history pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created deep history pseudostate information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createDeepHistoryPseudostate,
                        NewDeepHistoryPseudostateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_shal_his_pse",
                        "Create a new shallow history pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created shallow history pseudostate information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createShallowHistoryPseudostate,
                        NewShallowHistoryPseudostateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_fin_state",
                        "Create a new final state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created final state information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createFinalState,
                        NewFinalStateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_fork_pse",
                        "Create a new fork pseudostate of the specified size (specified by width and height) at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created fork pseudostate information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createForkPseudostate,
                        NewForkPseudostateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_init_pse",
                        "Create a new initial pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created initial pseudostate information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createInitialPseudostate,
                        NewInitialPseudostateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_join_pse",
                        "Create a new join pseudostate of the specified size (specified by width and height) at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created join pseudostate information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createJoinPseudostate,
                        NewJoinPseudostateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_junc_pse",
                        "Create a new junction pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created junction pseudostate information. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                        this::createJunctionPseudostate,
                        NewJunctionPseudostateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_state",
                        "Create a new state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created state information. If there is no parent state (i.e., when rendering at the top level), set the parent state ID to an empty string.",
                        this::createState,
                        NewStateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_state_dgm",
                        "Create a new state machine diagram under the specified package (specified by ID), and return the newly created state machine diagram information.",
                        this::createStateMachineDiagram,
                        NewStateMachineDiagramDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "create_sub_stat",
                        "Create a new sub machine state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created sub machine state information.",
                        this::createSubMachineState,
                        NewSubMachineStateDTO.class,
                        NodePresentationDTO.class),

                ToolSupport.definition(
                        "create_trans",
                        "Create a new transition between the specified source state (specified by ID) and the specified target state (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created transition information.",
                        this::createTransition,
                        NewTransitionDTO.class,
                        TransitionDTO.class)
        );
    }

    private NodePresentationDTO addRegion(McpSyncServerExchange exchange, NewRegionDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO deleteRegion(McpSyncServerExchange exchange, DeleteRegionDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO changeParentOfState(McpSyncServerExchange exchange, ChangeParentStateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahTargetNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createChoicePseudostate(McpSyncServerExchange exchange, NewChoicePseudostateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahChoicePseudostate);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createDeepHistoryPseudostate(McpSyncServerExchange exchange, NewDeepHistoryPseudostateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahDeepHistoryPseudostate);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createShallowHistoryPseudostate(McpSyncServerExchange exchange, NewShallowHistoryPseudostateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahShallowHistoryPseudostate);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createFinalState(McpSyncServerExchange exchange, NewFinalStateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahFinalState);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createForkPseudostate(McpSyncServerExchange exchange, NewForkPseudostateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahForkPseudostate);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createInitialPseudostate(McpSyncServerExchange exchange, NewInitialPseudostateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahInitialPseudostate);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createJoinPseudostate(McpSyncServerExchange exchange, NewJoinPseudostateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahJoinPseudostate);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createJunctionPseudostate(McpSyncServerExchange exchange, NewJunctionPseudostateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahJunctionPseudostate);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NodePresentationDTO createState(McpSyncServerExchange exchange, NewStateDTO param) throws Exception {
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
                "State",
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            return NodePresentationDTOAssembler.toDTO(astahState);

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

    private NodePresentationDTO createSubMachineState(McpSyncServerExchange exchange, NewSubMachineStateDTO param) throws Exception {
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

            return NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private TransitionDTO createTransition(McpSyncServerExchange exchange, NewTransitionDTO param) throws Exception {
        log.debug("Create transition: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahTransition = stateMachineDiagramEditor.createTransition(
                astahSourceNodePresentation,
                astahTargetNodePresentation);
            transactionManager.endTransaction();

            return TransitionDTOAssembler.toDTO((ITransition) astahTransition.getModel());

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
