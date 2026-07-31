package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.StateMachineDiagramEditor;
import com.change_vision.jude.api.inf.model.*;
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
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/StateMachineDiagramEditor.html
@Slf4j
public class StateMachineDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final StateMachineDiagramEditor stateMachineDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public StateMachineDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, StateMachineDiagramEditor stateMachineDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
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
                "Add a new region in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the parent node presentation along with the updated diagram image in low resolution.",
                this::addRegion,
                NewRegionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "delete_region",
                "Delete the specified region (specified by index) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the parent node presentation along with the updated diagram image in low resolution.",
                this::deleteRegion,
                DeleteRegionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_parent_state",
                "Change the parent state (specified by ID) of the specified state (specified by ID) on the specified state machine diagram (specified by ID), and return the node presentation of the parent-changed state along with the updated diagram image in low resolution. If there is no parent state (i.e., when rendering at the top level), set the parent state ID to an empty string.",
                this::changeParentOfState,
                ChangeParentStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_choice_pseudostate",
                "Create a new choice pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the choice pseudostate along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createChoicePseudostate,
                NewChoicePseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_deep_history_pseudostate",
                "Create a new deep history pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the deep history pseudostate along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createDeepHistoryPseudostate,
                NewDeepHistoryPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_shallow_history_pseudostate",
                "Create a new shallow history pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the shallow history pseudostate along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createShallowHistoryPseudostate,
                NewShallowHistoryPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_final_state",
                "Create a new final state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the final state along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createFinalState,
                NewFinalStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_fork_pseudostate",
                "Create a new fork pseudostate of the specified size (specified by width and height) at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the fork pseudostate along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createForkPseudostate,
                NewForkPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_init_pseudostate",
                "Create a new initial pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the initial pseudostate along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createInitialPseudostate,
                NewInitialPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_join_pseudostate",
                "Create a new join pseudostate of the specified size (specified by width and height) at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the join pseudostate along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createJoinPseudostate,
                NewJoinPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_junction_pseudostate",
                "Create a new junction pseudostate at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the junction pseudostate along with the updated diagram image in low resolution. If there is no parent node presentation (i.e., when rendering at the top level), set the parent node presentation ID to an empty string.",
                this::createJunctionPseudostate,
                NewJunctionPseudostateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_state",
                "Create a new state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the state along with the updated diagram image in low resolution. If there is no parent state (i.e., when rendering at the top level), set the parent state ID to an empty string.",
                this::createState,
                NewStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_state_machine_dgm",
                "Create a new state machine diagram under the specified package (specified by ID), and return the newly created model element of the state machine diagram.",
                this::createStateMachineDiagram,
                NewStateMachineDiagramDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_sub_machine_state",
                "Create a new sub machine state at the specified point (specified by x and y coordinates) in the parent node presentation (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created node presentation of the sub machine state along with the updated diagram image in low resolution.",
                this::createSubMachineState,
                NewSubMachineStateDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_transition",
                "Create a new transition between the specified source state (specified by ID) and the specified target state (specified by ID) on the specified state machine diagram (specified by ID), and return the newly created link presentation of the transition along with the updated diagram image in low resolution.",
                this::createTransition,
                NewTransitionDTO.class,
                LinkPresentationDTO.class)
        );
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> addRegion(NewRegionDTO param) throws Exception {
        log.debug("Add region: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        txnAstah.run( () -> {
            stateMachineDiagramEditor.addRegion(
                astahParentNodePresentation,
                param.isHorizontal());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> deleteRegion(DeleteRegionDTO param) throws Exception {
        log.debug("Delete region: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        txnAstah.run( () -> {
            stateMachineDiagramEditor.deleteRegion(
                astahParentNodePresentation,
                param.index());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> changeParentOfState(ChangeParentStateDTO param) throws Exception {
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

        txnAstah.run( () -> {
            stateMachineDiagramEditor.changeParentOfState(
                astahTargetNodePresentation,
                astahParentNodePresentation);
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahTargetNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createChoicePseudostate(NewChoicePseudostateDTO param) throws Exception {
        log.debug("Create choice pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahChoicePseudostate = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createChoicePseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahChoicePseudostate);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createDeepHistoryPseudostate(NewDeepHistoryPseudostateDTO param) throws Exception {
        log.debug("Create deep history pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahDeepHistoryPseudostate = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createDeepHistoryPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahDeepHistoryPseudostate);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createShallowHistoryPseudostate(NewShallowHistoryPseudostateDTO param) throws Exception {
        log.debug("Create shallow history pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahShallowHistoryPseudostate = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createShallowHistoryPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahShallowHistoryPseudostate);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createFinalState(NewFinalStateDTO param) throws Exception {
        log.debug("Create final state: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahFinalState = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createFinalState(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahFinalState);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createForkPseudostate(NewForkPseudostateDTO param) throws Exception {
        log.debug("Create fork pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahForkPseudostate = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createForkPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()),
                param.width(),
                param.height());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahForkPseudostate);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createInitialPseudostate(NewInitialPseudostateDTO param) throws Exception {
        log.debug("Create initial pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahInitialPseudostate = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createInitialPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahInitialPseudostate);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createJoinPseudostate(NewJoinPseudostateDTO param) throws Exception {
        log.debug("Create join pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahJoinPseudostate = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createJoinPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()),
                param.width(),
                param.height());
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahJoinPseudostate);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createJunctionPseudostate(NewJunctionPseudostateDTO param) throws Exception {
        log.debug("Create junction pseudostate: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahJunctionPseudostate = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createJunctionPseudostate(
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahJunctionPseudostate);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createState(NewStateDTO param) throws Exception {
        log.debug("Create state: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());

        INodePresentation astahParentNodePresentation;
        if (param.parentNodePresentationId().isEmpty()) {
            astahParentNodePresentation = null;
        } else {
            astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        }

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        INodePresentation astahState = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createState(
                param.newStateName(),
                astahParentNodePresentation,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahState);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private DiagramDTO createStateMachineDiagram(NewStateMachineDiagramDTO param) throws Exception {
        log.debug("Create state machine diagram: {}", param);

        INamedElement astahParentNamedElement = astahProToolSupport.getNamedElement(param.parentNamedElementId());

        IStateMachineDiagram astahStateMachineDiagram = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createStatemachineDiagram(
                astahParentNamedElement,
                param.newStateMachineDiagramName());
        });

        return DiagramDTOAssembler.toDTO(astahStateMachineDiagram);
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createSubMachineState(NewSubMachineStateDTO param) throws Exception {
        log.debug("Create sub machine state: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahParentNodePresentation = astahProToolSupport.getNodePresentation(param.parentNodePresentationId());
        IStateMachineDiagram astahSubMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.subMachineDiagramId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        txnAstah.run( () -> {
            stateMachineDiagramEditor.createSubmachineState(
                astahParentNodePresentation,
                astahSubMachineDiagram,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
        });

        NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahParentNodePresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createTransition(NewTransitionDTO param) throws Exception {
        log.debug("Create transition: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.targetDiagramId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        stateMachineDiagramEditor.setDiagram(astahStateMachineDiagram);

        ILinkPresentation astahLinkPresentation = txnAstah.call( () -> {
            return stateMachineDiagramEditor.createTransition(
                astahSourceNodePresentation,
                astahTargetNodePresentation);
        });

        LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(param.targetDiagramId());

        return Pair.of(dto, List.of(image));
    }
}
