package com.astahpromcp.tool.info;

import com.astahpromcp.info.McpServerInfo;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.info.outputdto.AstahProMcpVersionDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tool for obtaining Astah Pro MCP server information
@Slf4j
public class InfoTool implements ToolProvider {

    public InfoTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
            "get_astah_pro_mcp_version",
            "Return the version of the Astah Pro MCP server.",
            this::getAstahProMcpVersion,
            NoInputDTO.class,
            AstahProMcpVersionDTO.class)
        );
    }

    private AstahProMcpVersionDTO getAstahProMcpVersion(NoInputDTO param) throws Exception {
        log.debug("Get Astah Pro MCP version: {}", param);

        return new AstahProMcpVersionDTO(McpServerInfo.VERSION);
    }
}
