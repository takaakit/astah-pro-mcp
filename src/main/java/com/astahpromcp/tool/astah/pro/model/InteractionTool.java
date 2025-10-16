package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IInteraction;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IInteraction.html
@Slf4j
public class InteractionTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public InteractionTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_intr_info",
                            "Return detailed information about the specified interaction (specified by ID).",
                            this::getInfo,
                            IdDTO.class,
                            InteractionDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create interaction tools", e);
            return List.of();
        }
    }

    private InteractionDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get interaction information: {}", param);

        IInteraction astahInteraction = astahProToolSupport.getInteraction(param.id());

        return InteractionDTOAssembler.toDTO(astahInteraction);
    }
}
