package com.astahpromcp.tool;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;

public final class JsonSupport {

    private JsonSupport() {
    }

    // JsonMapper is used to convert JSON to and from Java objects
    public static final JsonMapper OBJ_MAPPER = JsonMapper.builder()
            .build();

    // McpJsonMapper is used to convert JSON to and from MCP objects
    public static final McpJsonMapper MCP_JSON_MAPPER = new JacksonMcpJsonMapper(OBJ_MAPPER);

    // Strict JsonMapper for binding tool arguments into input DTOs
    public static final JsonMapper STRICT_OBJ_MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .build();
}
