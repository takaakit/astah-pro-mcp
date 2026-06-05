package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ActivityDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.LinkPresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ActivityDiagramEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;
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
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/ActivityDiagramEditor.html
@Slf4j
public class ActivityDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final ActivityDiagramEditor activityDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public ActivityDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, ActivityDiagramEditor activityDiagramEditor, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.activityDiagramEditor = activityDiagramEditor;
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
            log.error("Failed to create activity diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_accept_event_act",
                "Create a new accept event action at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the accept event action along with the updated diagram image. An empty string is not allowed as an action name.",
                this::createAcceptEventAction,
                NewAcceptEventActionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_accept_time_event_act",
                "Create a new accept time event action at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the accept time event action along with the updated diagram image. An empty string is not allowed as an action name.",
                this::createAcceptTimeEventAction,
                NewAcceptTimeEventActionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_act",
                "Create a new action at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the action along with the updated diagram image. An empty string is not allowed as an action name.",
                this::createAction,
                NewActionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_activity_dgm",
                "Create a new activity diagram under the specified package (specified by ID), and return the newly created activity diagram information.",
                this::createActivityDiagram,
                NewActivityDiagramDTO.class,
                ActivityDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_activity_param_node",
                "Create a new activity parameter node of the base class (specified by ID) at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the activity parameter node along with the updated diagram image. An empty string is not allowed as a node name.",
                this::createActivityParameterNode,
                NewActivityParameterNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_call_behavior_act",
                "Create a new call behavior action of the specified activity diagram (specified by ID) at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the call behavior action along with the updated diagram image. An empty string is not allowed as an action name.",
                this::createCallBehaviorAction,
                NewCallBehaviorActionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_connector",
                "Create a new connector at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the connector along with the updated diagram image. An empty string is not allowed as a connector name.",
                this::createConnector,
                NewConnectorDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_decision_merge_node",
                "Create a new decision merge node at the specified point (specified by x and y coordinates) on the specified package (specified by ID) of the specified activity diagram (specified by ID), and return the newly created node presentation of the decision merge node along with the updated diagram image.",
                this::createDecisionMergeNode,
                NewDecisionMergeNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_dep_between_nodes",
                "Create a new dependency between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified activity diagram (specified by ID), and return the newly created link presentation of the dependency along with the updated diagram image.",
                this::createDependency,
                NewDependencyDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_final_node",
                "Create a new final node at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the final node along with the updated diagram image. An empty string is not allowed as a node name.",
                this::createFinalNode,
                NewFinalNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_flow",
                "Create a new flow between the specified source node presentation (specified by ID) and the specified target node presentation (specified by ID) on the specified activity diagram (specified by ID), and return the newly created link presentation of the flow along with the updated diagram image.",
                this::createFlow,
                NewFlowDTO.class,
                LinkPresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_flow_final_node",
                "Create a new flow final node at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the flow final node along with the updated diagram image. An empty string is not allowed as a node name.",
                this::createFlowFinalNode,
                NewFlowFinalNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_fork_node",
                "Create a new fork node at the specified point (specified by x and y coordinates) on the specified node presentation (specified by ID) of the specified activity diagram (specified by ID), and return the newly created node presentation of the fork node along with the updated diagram image.",
                this::createForkNode,
                NewForkNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_init_node",
                "Create a new initial node at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the initial node along with the updated diagram image. An empty string is not allowed as a node name. An empty string is not allowed as a node name.",
                this::createInitialNode,
                NewInitialNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_join_node",
                "Create a new join node at the specified point (specified by x and y coordinates) on the specified node presentation (specified by ID) of the specified activity diagram (specified by ID), and return the newly created node presentation of the join node along with the updated diagram image.",
                this::createJoinNode,
                NewJoinNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_obj_node",
                "Create a new object node of the base class (specified by ID) at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the object node along with the updated diagram image. An empty string is not allowed as a node name.",
                this::createObjectNode,
                NewObjectNodeDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_partition",
                "Create a new partition by specifying the super partition (specified by ID) and the previous partition (specified by ID) on the specified activity diagram (specified by ID), and return the newly created node presentation of the partition along with the updated diagram image. In cases where no super partition or previous partition exists, set the ID of those partitions to an empty string.",
                this::createPartition,
                NewPartitionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_in_or_out_pin",
                "Create a new input/output pin of the base class (specified by ID) on the specified parent action (specified by ID) on the specified activity diagram (specified by ID), and return the newly created node presentation of the pin along with the updated diagram image. An empty string is not allowed as a pin name.",
                this::createPin,
                NewPinWithBaseClassAndParentActionDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_process",
                "Create a new process at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the process along with the updated diagram image. An empty string is not allowed as a process name.",
                this::createProcess,
                NewProcessDTO.class,
                NodePresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "create_send_signal_act",
                "Create a new send signal action at the specified point (specified by x and y coordinates) on the specified activity diagram (specified by ID), and return the newly created node presentation of the send signal action along with the updated diagram image. An empty string is not allowed as an action name.",
                this::createSendSignalAction,
                NewSendSignalActionDTO.class,
                NodePresentationDTO.class)
        );
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createAcceptEventAction(McpSyncServerExchange exchange, NewAcceptEventActionDTO param) throws Exception {
        log.debug("Create accept event action: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createAcceptEventAction(
                param.newAcceptEventActionName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createAcceptTimeEventAction(McpSyncServerExchange exchange, NewAcceptTimeEventActionDTO param) throws Exception {
        log.debug("Create accept time event action: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createAcceptTimeEventAction(
                param.newAcceptTimeEventActionName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createAction(McpSyncServerExchange exchange, NewActionDTO param) throws Exception {
        log.debug("Create action: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createAction(
                param.newActionName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ActivityDiagramDTO createActivityDiagram(McpSyncServerExchange exchange, NewActivityDiagramDTO param) throws Exception {
        log.debug("Create activity diagram: {}", param);

        IPackage astahParentPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IActivityDiagram astahActivityDiagram = activityDiagramEditor.createActivityDiagram(
                astahParentPackage,
                param.newActivityDiagramName());
            transactionManager.endTransaction();

            return ActivityDiagramDTOAssembler.toDTO(astahActivityDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createActivityParameterNode(McpSyncServerExchange exchange, NewActivityParameterNodeDTO param) throws Exception {
        log.debug("Create activity parameter node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());
        IClass astahBaseClass = astahProToolSupport.getClass(param.baseClassId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createActivityParameterNode(
                param.newActivityParameterNodeName(),
                astahBaseClass,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createCallBehaviorAction(McpSyncServerExchange exchange, NewCallBehaviorActionDTO param) throws Exception {
        log.debug("Create call behavior action: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());
        IActivityDiagram astahReferenceActivityDiagram = astahProToolSupport.getActivityDiagram(param.referenceActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createCallBehaviorAction(
                param.newCallBehaviorActionName(),
                astahReferenceActivityDiagram,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createConnector(McpSyncServerExchange exchange, NewConnectorDTO param) throws Exception {
        log.debug("Create connector: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createConnector(
                param.newConnectorName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createDecisionMergeNode(McpSyncServerExchange exchange, NewDecisionMergeNodeDTO param) throws Exception {
        log.debug("Create decision merge node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createDecisionMergeNode(
                null,   // No parent node presentation
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createDependency(McpSyncServerExchange exchange, NewDependencyDTO param) throws Exception {
        log.debug("Create dependency: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());
        INodePresentation astahClientNodePresentation = astahProToolSupport.getNodePresentation(param.clientNodePresentationId());
        INodePresentation astahSupplierNodePresentation = astahProToolSupport.getNodePresentation(param.supplierNodePresentationId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahLinkPresentation = activityDiagramEditor.createDependency(
                param.newDependencyName(),
                astahClientNodePresentation,
                astahSupplierNodePresentation);
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahLinkPresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createFinalNode(McpSyncServerExchange exchange, NewFinalNodeDTO param) throws Exception {
        log.debug("Create final node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createFinalNode(
                param.newFinalNodeName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<LinkPresentationDTO, List<McpSchema.Content>> createFlow(McpSyncServerExchange exchange, NewFlowDTO param) throws Exception {
        log.debug("Create flow: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());
        INodePresentation astahSourceNodePresentation = astahProToolSupport.getNodePresentation(param.sourceNodePresentationId());
        INodePresentation astahTargetNodePresentation = astahProToolSupport.getNodePresentation(param.targetNodePresentationId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            ILinkPresentation astahNodePresentation = activityDiagramEditor.createFlow(
                astahSourceNodePresentation,
                astahTargetNodePresentation);
            transactionManager.endTransaction();

            LinkPresentationDTO dto = LinkPresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createFlowFinalNode(McpSyncServerExchange exchange, NewFlowFinalNodeDTO param) throws Exception {
        log.debug("Create flow final node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createFlowFinalNode(
                param.newFlowFinalNodeName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createForkNode(McpSyncServerExchange exchange, NewForkNodeDTO param) throws Exception {
        log.debug("Create fork node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createForkNode(
                null,   // No parent node presentation
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createInitialNode(McpSyncServerExchange exchange, NewInitialNodeDTO param) throws Exception {
        log.debug("Create initial node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createInitialNode(
                param.newInitialNodeName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createJoinNode(McpSyncServerExchange exchange, NewJoinNodeDTO param) throws Exception {
        log.debug("Create join node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createJoinNode(
                null,   // No parent node presentation
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createObjectNode(McpSyncServerExchange exchange, NewObjectNodeDTO param) throws Exception {
        log.debug("Create object node: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());
        IClass astahBaseClass = astahProToolSupport.getClass(param.baseClassId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createObjectNode(
                param.newObjectNodeName(),
                astahBaseClass,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createPartition(McpSyncServerExchange exchange, NewPartitionDTO param) throws Exception {
        log.debug("Create partition: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());
        INodePresentation astahSuperNodePresentation = param.superPartitionId().isEmpty()
            ? null
            : astahProToolSupport.getNodePresentation(param.superPartitionId());
        INodePresentation astahPreviousNodePresentation = param.previousPartitionId().isEmpty()
            ? null
            : astahProToolSupport.getNodePresentation(param.previousPartitionId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createPartition(
                astahSuperNodePresentation,
                astahPreviousNodePresentation,
                param.newPartitionName(),
                param.isHorizontal());
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createPin(McpSyncServerExchange exchange, NewPinWithBaseClassAndParentActionDTO param) throws Exception {
        log.debug("Create pin: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());
        IClass astahBaseClass = astahProToolSupport.getClass(param.baseClassId());
        INodePresentation astahParentAction = astahProToolSupport.getNodePresentation(param.parentActionId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createPin(
                param.newPinName(),
                astahBaseClass,
                param.isInput(),
                astahParentAction,
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createProcess(McpSyncServerExchange exchange, NewProcessDTO param) throws Exception {
        log.debug("Create process: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createProcess(
                param.newProcessName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private Pair<NodePresentationDTO, List<McpSchema.Content>> createSendSignalAction(McpSyncServerExchange exchange, NewSendSignalActionDTO param) throws Exception {
        log.debug("Create send signal action: {}", param);

        IActivityDiagram astahActivityDiagram = astahProToolSupport.getActivityDiagram(param.targetActivityDiagramId());

        activityDiagramEditor.setDiagram(astahActivityDiagram);

        try {
            transactionManager.beginTransaction();
            INodePresentation astahNodePresentation = activityDiagramEditor.createSendSignalAction(
                param.newSendSignalActionName(),
                new Point2D.Double(
                    param.locationX(),
                    param.locationY()));
            transactionManager.endTransaction();

            NodePresentationDTO dto = NodePresentationDTOAssembler.toDTO(astahNodePresentation);

            McpSchema.ImageContent image = imageCaptureSupport.createImageContent(param.targetActivityDiagramId(), ImageRegion.FULL);

            return Pair.of(dto, List.of(image));

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
