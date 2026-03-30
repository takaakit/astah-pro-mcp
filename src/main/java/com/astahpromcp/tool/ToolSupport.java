package com.astahpromcp.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.function.BiFunction;

// Utility methods for building tool schemas and handlers
@Slf4j
public final class ToolSupport {

    private ToolSupport() {
    }

    // Tool function
    @FunctionalInterface
    public interface ToolFunction<INPUT, OUTPUT> {
        OUTPUT apply(McpSyncServerExchange exchange, INPUT input) throws Exception;
    }

    // Create a tool definition
    public static <INPUT, OUTPUT> ToolDefinition definition(
            String name,
            String description,
            ToolFunction<INPUT, OUTPUT> function,
            Class<INPUT> inputType,
            Class<OUTPUT> outputType) {

        // Create a tool schema
        McpSchema.Tool schema = toolSchema(
            name,
            description,
            inputType,
            outputType);
        
        // Create a tool handler
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler = (exchange, request) -> toolHandler(
            exchange,
            request,
            name,
            function,
            inputType);

        return new ToolDefinition(schema, handler);
    }

    // Create a tool schema
    public static <INPUT, OUTPUT> McpSchema.Tool toolSchema(
            String toolName,
            String toolDescription,
            Class<INPUT> inputType,
            Class<OUTPUT> outputType) {

        String inputSchema = SchemaSupport.generateSchema(inputType);
        String outputSchema = SchemaSupport.generateSchema(outputType);

        log.debug("inputSchema: {}", inputSchema);
        log.debug("outputSchema: {}", outputSchema);

        return McpSchema.Tool.builder()
                .name(toolName)
                .description(toolDescription)
                .inputSchema(JsonSupport.MCP_JSON_MAPPER, inputSchema)
                .outputSchema(JsonSupport.MCP_JSON_MAPPER, outputSchema)
                .build();
    }

    // Create a tool handler
    public static <INPUT, OUTPUT> McpSchema.CallToolResult toolHandler(
            McpSyncServerExchange exchange,
            McpSchema.CallToolRequest request,
            String toolName,
            ToolFunction<INPUT, OUTPUT> function,
            Class<INPUT> inputType) {

        ValidationSupport.ValidationResult<INPUT> parsed = ValidationSupport.parse(request.arguments(), inputType);
        if (parsed.error() != null) {
            return parsed.error();
        }

        try {
            INPUT inputDto = parsed.dto();
            log.debug("Tool input of {}: \n{}", toolName, ReflectionToStringBuilder.toString(inputDto, ToStringStyle.MULTI_LINE_STYLE));
            OUTPUT outputDto = function.apply(exchange, inputDto);
            log.debug("Tool output of {}: \n{}", toolName, ReflectionToStringBuilder.toString(outputDto, ToStringStyle.MULTI_LINE_STYLE));
            if (outputDto == null) {
                String msg = String.format("Failure @tool=%s", toolName);
                log.error(msg);
                return ResponseSupport.error(msg);
            }
            
            return ResponseSupport.success(outputDto);
            
        } catch (Exception e) {
            String msg = String.format("Exception @tool=%s: %s", toolName, e.getMessage());
            log.error(msg);
            return ResponseSupport.error(msg);
        }
    }
}
