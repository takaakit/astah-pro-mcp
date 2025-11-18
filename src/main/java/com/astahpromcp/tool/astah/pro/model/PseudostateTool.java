package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PseudostateDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.PseudostateDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IPseudostate;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IPseudostate.html
@Slf4j
public class PseudostateTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public PseudostateTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create pseudostate tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_pse_info",
                        "Return detailed information about the specified pseudostate (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        PseudostateDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of();
    }

    private PseudostateDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get pseudostate information: {}", param);

        IPseudostate astahPseudostate = astahProToolSupport.getPseudostate(param.id());

        return PseudostateDTOAssembler.toDTO(astahPseudostate);
    }
}
