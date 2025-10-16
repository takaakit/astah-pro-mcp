package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DependencyDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DependencyDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IDependency.html
@Slf4j
public class DependencyTool implements ToolProvider {
    
    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public DependencyTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_dep_info",
                            "Return detailed information about the specified dependency (specified by ID).",
                            this::getInfo,
                            IdDTO.class,
                            DependencyDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create dependency tools", e);
            return List.of();
        }
    }

    private DependencyDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get dependency information: {}", param);
        
        IDependency astahDependency = astahProToolSupport.getDependency(param.id());

        return DependencyDTOAssembler.toDTO(astahDependency);
    }
}
