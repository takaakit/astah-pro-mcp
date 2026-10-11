package com.astahpromcp.info;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Information of the MCP server
@Slf4j
public final class McpServerInfo {

    private McpServerInfo() {
    }

    private static final String UNKNOWN = "unknown";

    // MCP server artifact ID
    public static final String ARTIFACT_ID;

    // MCP server version
    public static final String VERSION;

    static {
        Properties props = loadProjectProperties();
        ARTIFACT_ID = props.getProperty("project.artifactId", UNKNOWN);
        VERSION = props.getProperty("project.version", UNKNOWN);
    }

    private static Properties loadProjectProperties() {
        Properties props = new Properties();
        try (InputStream is = McpServerInfo.class.getClassLoader().getResourceAsStream("project.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                log.warn("project.properties was not found on the classpath");
            }
        } catch (IOException e) {
            log.warn("Failed to load project.properties", e);
        }
        return props;
    }
}
