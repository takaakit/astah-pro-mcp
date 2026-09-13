package com.astahpromcp.tool;

import tools.jackson.core.JacksonException;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class ResponseSupport {

    private ResponseSupport() {
    }

    // Return a DTO as structured content
    public static McpSchema.CallToolResult success(Object dto) {
        return successWithStructuredContent(dto, List.of());
    }

    // Success method is used to return a success response with contents
    public static McpSchema.CallToolResult success(List<McpSchema.Content> contents) {
        return McpSchema.CallToolResult.builder()
                .content(withoutEmptyImages(contents))
                .isError(false)
                .build();
    }

    // Return a DTO as structured content and other McpSchema contents
    public static McpSchema.CallToolResult success(Object dto, List<McpSchema.Content> contents) {
        return successWithStructuredContent(dto, contents == null ? List.of() : contents);
    }

    private static McpSchema.CallToolResult successWithStructuredContent(
            Object dto,
            List<McpSchema.Content> contents) {
        try {
            String jsonContent = JsonSupport.OBJ_MAPPER.writeValueAsString(dto);

            return McpSchema.CallToolResult.builder()
                    .content(withoutEmptyImages(contents))
                    .isError(false)
                    .structuredContent(JsonSupport.MCP_JSON_MAPPER, jsonContent)
                    .build();

        } catch (JacksonException | IllegalArgumentException e) {
            return error(serializationErrorMessage(dto, e));
        }
    }

    // Drop any image content that carries no data
    private static List<McpSchema.Content> withoutEmptyImages(List<McpSchema.Content> contents) {
        if (contents == null || contents.isEmpty()) {
            return List.of();
        
        } else {
            List<McpSchema.Content> kept = new ArrayList<>(contents.size());
            for (McpSchema.Content content : contents) {
                if (content instanceof McpSchema.ImageContent image
                        && (image.data() == null || image.data().isEmpty())) {
                    log.debug("Dropped an image content of type {} carrying no data", image.mimeType());
                    continue;
                }
                kept.add(content);
            }
    
            return List.copyOf(kept);
        }
    }

    // Error method is used to return an error response
    public static McpSchema.CallToolResult error(String message) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }

    // Build a log-friendly error message for a DTO serialization failure
    private static String serializationErrorMessage(Object dto, Exception e) {
        String dtoType = dto == null ? "null" : dto.getClass().getName();
        log.error("Failed to serialize DTO of type {}", dtoType, e);
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = e.getClass().getSimpleName();
        }

        return "Failed to serialize DTO: " + message;
    }

}
