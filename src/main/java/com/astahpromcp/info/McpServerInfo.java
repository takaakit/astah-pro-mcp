package com.astahpromcp.info;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
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
    
    // When the MCP server was built
    public static final String BUILD_TIMESTAMP;
    
    static {
        Properties props = loadProjectProperties();
        ARTIFACT_ID = props.getProperty("project.artifactId", UNKNOWN);
        VERSION = props.getProperty("project.version", UNKNOWN);
        BUILD_TIMESTAMP = props.getProperty("project.buildTimestamp", UNKNOWN);
    }
    
    // What tells this build apart from every other build, or empty when it cannot be told
    public static Optional<String> buildId() {
        return buildIdOf(VERSION, BUILD_TIMESTAMP);
    }
    
    // Empty when either value is missing, or is a placeholder that the build left unreplaced, as when the classes run without the Maven build
    static Optional<String> buildIdOf(String version, String buildTimestamp) {
        if (!isKnown(version) || !isKnown(buildTimestamp)) {
            return Optional.empty();
        }
        return Optional.of(version + "+" + buildTimestamp);
    }
    
    private static boolean isKnown(String value) {
        return value != null && !value.isBlank() && !value.equals(UNKNOWN) && !value.contains("${");
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
