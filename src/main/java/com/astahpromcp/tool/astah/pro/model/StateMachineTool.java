package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateMachineDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.StateMachineDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IStateMachine;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IStateMachine.html
@Slf4j
public class StateMachineTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public StateMachineTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create state machine tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_state_mach_info",
                        "Return detailed information about the specified state machine (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        StateMachineDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of();
    }

    private StateMachineDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get state machine information: {}", param);

        IStateMachine astahStateMachine = astahProToolSupport.getStateMachine(param.id());

        return StateMachineDTOAssembler.toDTO(astahStateMachine);
    }
}
