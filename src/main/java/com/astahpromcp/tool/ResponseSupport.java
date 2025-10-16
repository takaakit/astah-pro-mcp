package com.astahpromcp.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public final class ResponseSupport {

    private ResponseSupport() {
    }

    // Success method is used to return a success response
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
            
        } catch (JsonProcessingException | IllegalArgumentException e) {
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
