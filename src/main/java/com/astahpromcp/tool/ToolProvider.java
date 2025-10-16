package com.astahpromcp.tool;

import java.util.List;

// An interface for providing a collection of tools to the MCP server
public interface ToolProvider {

    List<ToolDefinition> createToolDefinitions();
}
