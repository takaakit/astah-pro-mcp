package com.astahpromcp.tool.mcptoolscript.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RunMcpToolScriptDTO(
    @JsonPropertyDescription("Source code of the mcp tool script: JavaScript (Nashorn, ECMAScript 5.1), evaluated in strict mode, so declare every variable with 'var'. Call this MCP server's tool functions as tools.<name>({ ... }), for example tools.get_class_info({ id: '...' }). Find names with 'get_all_tools_callable_from_mcp_tool_script' and signatures with 'get_info_of_tools_callable_from_mcp_tool_script'. A tool function returns its result as a plain object, and reports failure by throwing. Use print() for output (console.log is unavailable). The raw Astah API is NOT available here: there is no 'astah' global and Java.type() cannot be used; run an astah api script with 'run_astah_api_script' when you need them. The whole script runs inside one Astah transaction: if any tool function call fails, every change the script made is rolled back at the end, so catching the failure with try/catch lets the script carry on but does not save its edits.")
    String script
) {
}
