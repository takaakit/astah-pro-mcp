package com.astahpromcp.tool.config;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.config.outputdto.WorkspaceDirectoryPathDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tool for obtaining the configuration of the MCP server
@Slf4j
public class ConfigTool implements ToolProvider {
    
    public ConfigTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.definition(
                "get_work_dir_path",
                "Return the path to the workspace directory.",
                this::getWorkspaceDirectoryPath,
                NoInputDTO.class,
                WorkspaceDirectoryPathDTO.class)
        );
    }

    private WorkspaceDirectoryPathDTO getWorkspaceDirectoryPath(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get workspace directory path: {}", param);

        return new WorkspaceDirectoryPathDTO(McpServerConfig.DEFAULT_WORKSPACE_DIR.toString());
    }
}
