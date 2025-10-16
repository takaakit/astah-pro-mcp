package com.astahpromcp.tool;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;

public final class JsonSupport {

    private JsonSupport() {
    }

    // ObjectMapper is used to convert JSON to and from Java objects
    public static final ObjectMapper OBJ_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // McpJsonMapper is used to convert JSON to and from MCP objects
    public static final McpJsonMapper MCP_JSON_MAPPER = new JacksonMcpJsonMapper(OBJ_MAPPER);
}
