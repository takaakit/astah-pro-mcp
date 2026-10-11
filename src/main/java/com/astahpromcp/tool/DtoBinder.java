package com.astahpromcp.tool;

import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.exc.InvalidDefinitionException;

import java.util.Map;

// Binds raw MCP request arguments into a typed DTO.
public final class DtoBinder {

    private DtoBinder() {
    }

    // Converts the given argument map into the specified DTO.
    public static <T> BindResult<T> bind(Map<String, Object> args, Class<T> type) {
        try {
            // Convert the arguments into the DTO, rejecting unknown properties
            T dto = JsonSupport.STRICT_OBJ_MAPPER.convertValue(args == null ? Map.of() : args, type);

            return new BindResult<>(dto, null);

        } catch (Exception e) {
            // A DTO class that cannot be bound at all is a fault of this server; any other failure is an argument the caller got wrong.
            ToolSupport.logFailure(
                "Failed to convert arguments into " + type.getName() + ": " + e.getMessage(),
                e,
                !(e instanceof InvalidDefinitionException));
            McpSchema.CallToolResult error = ResponseSupport.error("Invalid arguments: " + e.getMessage());

            return new BindResult<>(null, error);
        }
    }

    // Result object carrying either a DTO or an error response.
    public record BindResult<T>(
        T dto,
        McpSchema.CallToolResult error
    ) {
    }
}
