package com.astahpromcp.tool;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public final class ResponseSupport {

    private ResponseSupport() {
    }

    // Success method is used to return a success response with DTO
    public static McpSchema.CallToolResult success(Object dto) {
        try {
            ObjectMapper mapper = JsonSupport.OBJ_MAPPER;
            JsonNode jsonNode = mapper.valueToTree(dto);
            String jsonContent = mapper.writeValueAsString(jsonNode);
            Map<String, Object> structuredContent = mapper.convertValue(
                    jsonNode,
                    new TypeReference<Map<String, Object>>() {});

            return McpSchema.CallToolResult.builder()
                    .content(List.of(new McpSchema.TextContent(jsonContent)))
                    .isError(false)
                    .structuredContent(structuredContent)
                    .build();
            
        } catch (JacksonException | IllegalArgumentException e) {
            String dtoType = dto == null ? "null" : dto.getClass().getName();
            log.error("Failed to serialize DTO of type {}", dtoType, e);
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = e.getClass().getSimpleName();
            }
            
            return error("Failed to serialize DTO: " + message);
        }
    }

    // Success method is used to return a success response with contents
    public static McpSchema.CallToolResult success(List<McpSchema.Content> contents) {
        return McpSchema.CallToolResult.builder()
                .content(contents)
                .isError(false)
                .build();
    }

    // Success method is used to return a success response with DTO and contents
    public static McpSchema.CallToolResult success(Object dto, List<McpSchema.Content> contents) {
        try {
            ObjectMapper mapper = JsonSupport.OBJ_MAPPER;
            String jsonContent = mapper.writeValueAsString(mapper.valueToTree(dto));

            List<McpSchema.Content> mergedContents = new ArrayList<>();
            mergedContents.add(new McpSchema.TextContent(jsonContent));
            if (contents != null) {
                mergedContents.addAll(contents);
            }

            return McpSchema.CallToolResult.builder()
                    .content(mergedContents)
                    .isError(false)
                    .build();

        } catch (JacksonException | IllegalArgumentException e) {
            String dtoType = dto == null ? "null" : dto.getClass().getName();
            log.error("Failed to serialize DTO of type {}", dtoType, e);
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = e.getClass().getSimpleName();
            }

            return error("Failed to serialize DTO: " + message);
        }
    }

    // Error method is used to return an error response
    public static McpSchema.CallToolResult error(String message) {
        return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(message)))
                .isError(true)
                .build();
    }

}
