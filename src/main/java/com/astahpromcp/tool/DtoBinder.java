package com.astahpromcp.tool;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

// Binds raw MCP request arguments into a typed DTO.
@Slf4j
public final class DtoBinder {

    private DtoBinder() {
    }

    // Converts the given argument map into the specified DTO.
    public static <T> BindResult<T> bind(Map<String, Object> args, Class<T> type) {
        try {
            // Convert the arguments into the DTO
            T dto = JsonSupport.OBJ_MAPPER.convertValue(args, type);

            return new BindResult<>(dto, null);

        } catch (Exception e) {
            log.error("Failed to convert arguments into {}", type.getName(), e);
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
