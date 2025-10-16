package com.astahpromcp.tool.astah.pro.model;

import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateMachineDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateMachineDiagramDTOAssembler;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IStateMachineDiagram.html
@Slf4j
public class StateMachineDiagramTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public StateMachineDiagramTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.definition(
                "get_state_mach_dgm_info",
                "Return detailed information about the specified state machine diagram (specified by ID).",
                this::getInfo,
                IdDTO.class,
                StateMachineDiagramDTO.class)
        );
    }

    private StateMachineDiagramDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get state machine diagram information: {}", param);

        IStateMachineDiagram astahStateMachineDiagram = astahProToolSupport.getStateMachineDiagram(param.id());

        return StateMachineDiagramDTOAssembler.toDTO(astahStateMachineDiagram);
    }
}
