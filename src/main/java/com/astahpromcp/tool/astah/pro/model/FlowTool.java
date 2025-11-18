package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.FlowWithActionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.FlowWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.FlowDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.FlowDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IFlow;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IFlow.html
@Slf4j
public class FlowTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public FlowTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
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
            log.error("Failed to create tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_flow_info",
                        "Return detailed information about the specified flow (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        FlowDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_actn_of_flow",
                        "Set the action of the specified flow (specified by ID), and return the flow information after it is set.",
                        this::setAction,
                        FlowWithActionDTO.class,
                        FlowDTO.class),

                ToolSupport.definition(
                        "set_guard_of_flow",
                        "Set the guard of the specified flow (specified by ID), and return the flow information after it is set.",
                        this::setGuard,
                        FlowWithGuardDTO.class,
                        FlowDTO.class)
        );
    }

    private FlowDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get flow information: {}", param);

        IFlow astahFlow = astahProToolSupport.getFlow(param.id());

        return FlowDTOAssembler.toDTO(astahFlow);
    }

    private FlowDTO setAction(McpSyncServerExchange exchange, FlowWithActionDTO param) throws Exception {
        log.debug("Set action of flow: {}", param);

        IFlow astahFlow = astahProToolSupport.getFlow(param.targetFlowId());

        try {
            transactionManager.beginTransaction();
            astahFlow.setAction(param.action());
            transactionManager.endTransaction();

            return FlowDTOAssembler.toDTO(astahFlow);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private FlowDTO setGuard(McpSyncServerExchange exchange, FlowWithGuardDTO param) throws Exception {
        log.debug("Set guard of flow: {}", param);

        IFlow astahFlow = astahProToolSupport.getFlow(param.targetFlowId());

        try {
            transactionManager.beginTransaction();
            astahFlow.setGuard(param.guard());
            transactionManager.endTransaction();

            return FlowDTOAssembler.toDTO(astahFlow);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
