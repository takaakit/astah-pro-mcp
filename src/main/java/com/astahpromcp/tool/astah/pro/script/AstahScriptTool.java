package com.astahpromcp.tool.astah.pro.script;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.script.inputdto.RunScriptDTO;
import com.astahpromcp.tool.astah.pro.script.outputdto.ScriptResultDTO;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tool for running JavaScript (Astah script) inside the running Astah.
@Slf4j
public class AstahScriptTool implements ToolProvider {

    private final AstahScriptExecutor scriptExecutor;
    private final boolean includeEditTools;

    public AstahScriptTool(ProjectAccessor projectAccessor, boolean includeEditTools) {
        this.scriptExecutor = new AstahScriptExecutor(projectAccessor);
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
            log.error("Failed to create astah script tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "run_astah_script",
                "Run a JavaScript (Nashorn, ECMAScript 5.1) script inside the running Astah and return its result. MCP client (you) MUST call 'astah_script_guide' tool function before using this tool to learn how to write the script. Prefer the dedicated tool functions of this MCP server when they cover the need; use this tool for operations they do not cover (i.e., operations that are possible with the Astah API but are not supported by the dedicated tool functions). Keep scripts short and never run scripts that block or loop indefinitely. Script errors are reported in the tool output ('ok' is false, with the error message, line, and column). Fix the script and retry. NEVER save the Astah project from a script unless the user explicitly instructs you to do so.",
                this::runScript,
                RunScriptDTO.class,
                ScriptResultDTO.class)
        );
    }

    private ScriptResultDTO runScript(McpSyncServerExchange exchange, RunScriptDTO param) throws Exception {
        log.debug("Run astah script: {}", param);

        if (param.script() == null || param.script().trim().isEmpty()) {
            throw new IllegalArgumentException("The script is empty.");
        }

        // Note: Script errors are reported through the DTO (ok=false), not as a tool error
        AstahScriptExecutor.Result result = scriptExecutor.execute(param.script());

        return new ScriptResultDTO(
            result.ok(),
            result.value(),
            result.output(),
            result.errorOutput(),
            result.errorMessage(),
            result.errorLine(),
            result.errorColumn());
    }
}
