package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.mcptoolscript.inputdto.RunMcpToolScriptDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.McpToolScriptResultDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tool for running an mcp tool script -- JavaScript that drives this server's own tool functions.
@Slf4j
public class McpToolScriptTool implements ToolProvider {

    private final McpToolScriptExecutor mcpToolScriptExecutor;

    public McpToolScriptTool(McpToolScriptExecutor mcpToolScriptExecutor) {
        this.mcpToolScriptExecutor = mcpToolScriptExecutor;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "run_mcp_tool_script",
                "Run an mcp tool script -- JavaScript (Nashorn, ECMAScript 5.1, strict mode) that calls this MCP server's tool functions as tools.<name>({ ... }) -- and return its result. Call 'mcp_tool_script_guide' before using this tool: it returns worked example mcp tool scripts and a catalogue of every example this server ships. Prefer this tool over 'run_astah_api_script': it reaches almost every tool function this server provides, and one script can loop over many elements instead of costing one round trip per operation. Read every chunk of 'get_chunk_of_tools_callable_from_mcp_tool_script' to find the tool functions to call, and use 'get_info_of_tools_callable_from_mcp_tool_script' to learn their arguments. A tool function reports failure by throwing, so the script stops at the failing call unless you catch it. A run that edits the model shares one Astah transaction across every tool function call it makes: if any of them fails, every change the script made is rolled back, and catching the failure to carry on does not change that. A run that only reads never opens a transaction at all. A tool function is reachable either from an mcp tool script or as an MCP tool of this server, never both: whatever is already in your tool list is called directly, and this tool reaches everything else. Script errors are reported in the tool output ('ok' is false, with the error message, the line and column, and 'errorToolCallIndex', the ordinal of the tool function call that failed -- which is what tells you the pass of a loop a repeated call failed on). Keep scripts short and never run scripts that block or loop indefinitely.",
                this::runScript,
                RunMcpToolScriptDTO.class,
                McpToolScriptResultDTO.class)
        );
    }

    private McpToolScriptResultDTO runScript(RunMcpToolScriptDTO param) throws Exception {
        log.debug("Run mcp tool script: {}", param);

        if (param.script() == null || param.script().trim().isEmpty()) {
            throw new IllegalArgumentException("The mcp tool script is empty.");
        }

        // Note: script errors are reported through the DTO (ok=false), not as a tool error
        McpToolScriptExecutor.Result result = mcpToolScriptExecutor.execute(param.script());

        return new McpToolScriptResultDTO(
            result.ok(),
            result.value(),
            result.output(),
            result.errorOutput(),
            result.errorMessage(),
            result.errorLine(),
            result.errorColumn(),
            result.errorToolCallIndex());
    }
}
