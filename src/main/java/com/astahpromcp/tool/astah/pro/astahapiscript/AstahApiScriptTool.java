package com.astahpromcp.tool.astah.pro.astahapiscript;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.astahapiscript.inputdto.RunAstahApiScriptDTO;
import com.astahpromcp.tool.common.outputdto.ScriptResultDTO;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tool for running an astah api script -- JavaScript with the raw Astah API bound -- inside the running Astah.
@Slf4j
public class AstahApiScriptTool extends AstahToolProvider {

    private final AstahApiScriptExecutor astahApiScriptExecutor;

    public AstahApiScriptTool(ProjectAccessor projectAccessor) {
        this.astahApiScriptExecutor = new AstahApiScriptExecutor(projectAccessor);
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "run_astah_api_script",
                "Run an astah api script -- JavaScript (Nashorn, ECMAScript 5.1) with the raw Astah API bound -- inside the running Astah, and return its result. MCP client (you) MUST call 'astah_api_script_guide' tool function before using this tool to learn how to write the script. Prefer the dedicated tool functions of this MCP server when they cover the need; use this tool for operations they do not cover (i.e., operations that are possible with the Astah API but are not supported by the dedicated tool functions). Keep scripts short and never run scripts that block or loop indefinitely. Script errors are reported in the tool output ('ok' is false, with the error message, line, and column). Fix the script and retry. NEVER save the Astah project from a script unless the user explicitly instructs you to do so.",
                this::runScript,
                RunAstahApiScriptDTO.class,
                ScriptResultDTO.class)
        );
    }

    private ScriptResultDTO runScript(RunAstahApiScriptDTO param) throws Exception {
        log.debug("Run astah api script: {}", param);

        if (param.script() == null || param.script().trim().isEmpty()) {
            throw new IllegalArgumentException("The astah api script is empty.");
        }

        // Note: script errors are reported through the DTO (ok=false), not as a tool error
        AstahApiScriptExecutor.Result result = astahApiScriptExecutor.execute(param.script());

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
