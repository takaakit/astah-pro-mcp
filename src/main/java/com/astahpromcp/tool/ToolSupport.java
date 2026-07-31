package com.astahpromcp.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

// Utility methods for building tool schemas and handlers
@Slf4j
public final class ToolSupport {

    private ToolSupport() {
    }

    // Tool function returning DTO
    @FunctionalInterface
    public interface ToolFunctionReturningDto<INPUT_DTO, OUTPUT_DTO> {
        OUTPUT_DTO apply(INPUT_DTO input) throws Exception;
    }

    // Create a tool definition returning DTO
    public static <INPUT_DTO, OUTPUT_DTO> ToolDefinition toolDefinitionReturningDto(
            String name,
            String description,
            ToolFunctionReturningDto<INPUT_DTO, OUTPUT_DTO> function,
            Class<INPUT_DTO> inputDtoType,
            Class<OUTPUT_DTO> outputDtoType) {

        // Create a tool schema
        McpSchema.Tool schema = toolSchemaReturningDto(
            name,
            description,
            inputDtoType,
            outputDtoType);
        
        // Create a tool handler
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler = (ignoredExchange, request) -> toolHandlerReturningDto(
            request,
            name,
            function,
            inputDtoType);

        return new ToolDefinition(schema, handler);
    }

    // Create a tool schema for tools returning DTO
    public static <INPUT_DTO, OUTPUT_DTO> McpSchema.Tool toolSchemaReturningDto(
            String toolName,
            String toolDescription,
            Class<INPUT_DTO> inputDtoType,
            Class<OUTPUT_DTO> outputDtoType) {

        String inputSchema = SchemaSupport.generateSchema(inputDtoType);
        String outputSchema = SchemaSupport.generateSchema(outputDtoType);

        log.debug("inputSchema: {}", inputSchema);
        log.debug("outputSchema: {}", outputSchema);

        return McpSchema.Tool.builder(toolName, JsonSupport.MCP_JSON_MAPPER, inputSchema)
                .description(toolDescription)
                .outputSchema(JsonSupport.MCP_JSON_MAPPER, outputSchema)
                .build();
    }

    // Create a tool handler for tools returning DTO
    public static <INPUT_DTO, OUTPUT_DTO> McpSchema.CallToolResult toolHandlerReturningDto(
            McpSchema.CallToolRequest request,
            String toolName,
            ToolFunctionReturningDto<INPUT_DTO, OUTPUT_DTO> function,
            Class<INPUT_DTO> inputDtoType) {

        DtoBinder.BindResult<INPUT_DTO> parsed = DtoBinder.bind(request.arguments(), inputDtoType);
        if (parsed.error() != null) {
            return parsed.error();
        }

        try {
            INPUT_DTO inputDto = parsed.dto();
            log.atDebug()
                    .setMessage("Tool input of {}: \n{}")
                    .addArgument(toolName)
                    .addArgument(() -> ReflectionToStringBuilder.toString(inputDto, ToStringStyle.MULTI_LINE_STYLE))
                    .log();
            OUTPUT_DTO outputDto = function.apply(inputDto);
            log.atDebug()
                    .setMessage("Tool output of {}: \n{}")
                    .addArgument(toolName)
                    .addArgument(() -> ReflectionToStringBuilder.toString(outputDto, ToStringStyle.MULTI_LINE_STYLE))
                    .log();
            if (outputDto == null) {
                String msg = String.format("Failure @tool=%s", toolName);
                log.error(msg);
                return ResponseSupport.error(msg);
            }
            
            return ResponseSupport.success(outputDto);
            
        } catch (Throwable t) {
            String msg = String.format("Exception @tool=%s: %s", toolName, t.getMessage());
            log.error(msg, t);
            return ResponseSupport.error(msg);
        }
    }

    // ----------

    // Tool function returning contents
    @FunctionalInterface
    public interface ToolFunctionReturningContents<INPUT_DTO> {
        List<McpSchema.Content> apply(INPUT_DTO input) throws Exception;
    }

    // Create a tool definition returning contents
    public static <INPUT_DTO> ToolDefinition toolDefinitionReturningContents(
            String name,
            String description,
            ToolFunctionReturningContents<INPUT_DTO> function,
            Class<INPUT_DTO> inputDtoType) {

        // Create a tool schema
        McpSchema.Tool schema = toolSchemaReturningContents(
            name,
            description,
            inputDtoType);
        
        // Create a tool handler
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler = (ignoredExchange, request) -> toolHandlerReturningContents(
            request,
            name,
            function,
            inputDtoType);

        return new ToolDefinition(schema, handler);
    }

    // Create a tool schema for tools returning contents
    public static <INPUT_DTO> McpSchema.Tool toolSchemaReturningContents(
            String toolName,
            String toolDescription,
            Class<INPUT_DTO> inputDtoType) {

        String inputSchema = SchemaSupport.generateSchema(inputDtoType);

        log.debug("inputSchema: {}", inputSchema);

        return McpSchema.Tool.builder(toolName, JsonSupport.MCP_JSON_MAPPER, inputSchema)
                .description(toolDescription)
                .build();
    }

    // Create a tool handler for tools returning contents
    public static <INPUT_DTO> McpSchema.CallToolResult toolHandlerReturningContents(
            McpSchema.CallToolRequest request,
            String toolName,
            ToolFunctionReturningContents<INPUT_DTO> function,
            Class<INPUT_DTO> inputDtoType) {

        DtoBinder.BindResult<INPUT_DTO> parsed = DtoBinder.bind(request.arguments(), inputDtoType);
        if (parsed.error() != null) {
            return parsed.error();
        }

        try {
            INPUT_DTO inputDto = parsed.dto();
            log.atDebug()
                    .setMessage("Tool input of {}: \n{}")
                    .addArgument(toolName)
                    .addArgument(() -> ReflectionToStringBuilder.toString(inputDto, ToStringStyle.MULTI_LINE_STYLE))
                    .log();
            List<McpSchema.Content> contents = function.apply(inputDto);
            log.atDebug()
                    .setMessage("Tool output of {}: \n{}")
                    .addArgument(toolName)
                    .addArgument(() -> summarizeContents(contents))
                    .log();
            if (contents == null) {
                String msg = String.format("Failure @tool=%s", toolName);
                log.error(msg);
                return ResponseSupport.error(msg);
            }
            
            return ResponseSupport.success(contents);
            
        } catch (Throwable t) {
            String msg = String.format("Exception @tool=%s: %s", toolName, t.getMessage());
            log.error(msg, t);
            return ResponseSupport.error(msg);
        }
    }

    // Summarize contents for debug logging
    static String summarizeContents(List<McpSchema.Content> contents) {
        if (contents == null) {
            return "null";
        }

        return contents.stream()
                .map(content -> content instanceof McpSchema.ImageContent image
                        ? String.format("ImageContent(mimeType=%s, base64 %d chars)",
                                image.mimeType(), image.data() == null ? 0 : image.data().length())
                        : String.valueOf(content))
                .collect(Collectors.joining(",\n", "[", "]"));
    }

    // Tool function returning DTO and contents
    @FunctionalInterface
    public interface ToolFunctionReturningDtoAndContents<INPUT_DTO, OUTPUT_DTO> {
        Pair<OUTPUT_DTO, List<McpSchema.Content>> apply(INPUT_DTO input) throws Exception;
    }

    // Create a tool definition returning DTO and contents
    public static <INPUT_DTO, OUTPUT_DTO> ToolDefinition toolDefinitionReturningDtoAndContents(
            String name,
            String description,
            ToolFunctionReturningDtoAndContents<INPUT_DTO, OUTPUT_DTO> function,
            Class<INPUT_DTO> inputDtoType,
            Class<OUTPUT_DTO> outputDtoType) {

        // Create a tool schema
        McpSchema.Tool schema = toolSchemaReturningDtoAndContents(
            name,
            description,
            inputDtoType);

        // Create a tool handler
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler = (ignoredExchange, request) -> toolHandlerReturningDtoAndContents(
            request,
            name,
            function,
            inputDtoType);

        return new ToolDefinition(schema, handler);
    }

    // Create a tool schema for tools returning DTO and contents
    public static <INPUT_DTO> McpSchema.Tool toolSchemaReturningDtoAndContents(
            String toolName,
            String toolDescription,
            Class<INPUT_DTO> inputDtoType) {

        String inputSchema = SchemaSupport.generateSchema(inputDtoType);

        log.debug("inputSchema: {}", inputSchema);

        return McpSchema.Tool.builder(toolName, JsonSupport.MCP_JSON_MAPPER, inputSchema)
                .description(toolDescription)
                .build();
    }

    // Create a tool handler for tools returning DTO and contents
    public static <INPUT_DTO, OUTPUT_DTO> McpSchema.CallToolResult toolHandlerReturningDtoAndContents(
            McpSchema.CallToolRequest request,
            String toolName,
            ToolFunctionReturningDtoAndContents<INPUT_DTO, OUTPUT_DTO> function,
            Class<INPUT_DTO> inputDtoType) {

        DtoBinder.BindResult<INPUT_DTO> parsed = DtoBinder.bind(request.arguments(), inputDtoType);
        if (parsed.error() != null) {
            return parsed.error();
        }

        try {
            INPUT_DTO inputDto = parsed.dto();
            log.atDebug()
                    .setMessage("Tool input of {}: \n{}")
                    .addArgument(toolName)
                    .addArgument(() -> ReflectionToStringBuilder.toString(inputDto, ToStringStyle.MULTI_LINE_STYLE))
                    .log();
            Pair<OUTPUT_DTO, List<McpSchema.Content>> result = function.apply(inputDto);
            if (result == null || result.getLeft() == null) {
                String msg = String.format("Failure @tool=%s", toolName);
                log.error(msg);
                return ResponseSupport.error(msg);
            }

            OUTPUT_DTO outputDto = result.getLeft();
            List<McpSchema.Content> contents = result.getRight();
            log.atDebug()
                    .setMessage("Tool output DTO of {}: \n{}")
                    .addArgument(toolName)
                    .addArgument(() -> ReflectionToStringBuilder.toString(outputDto, ToStringStyle.MULTI_LINE_STYLE))
                    .log();
            log.atDebug()
                    .setMessage("Tool output contents of {}: \n{}")
                    .addArgument(toolName)
                    .addArgument(() -> summarizeContents(contents))
                    .log();

            return ResponseSupport.success(outputDto, contents);

        } catch (Throwable t) {
            String msg = String.format("Exception @tool=%s: %s", toolName, t.getMessage());
            log.error(msg, t);
            return ResponseSupport.error(msg);
        }
    }
}
