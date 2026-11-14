package com.astahpromcp.info;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class McpServerInfoTest {
    
    @Test
    void buildId_ok_isKnownInTheMavenBuild() {
        // The Maven build writes the version and the build time into project.properties. Without them the converted reference
        // documents are not cached, so this fails if the build stops writing either of them.
        assertTrue(McpServerInfo.buildId().isPresent(),
                () -> "project.properties gives version '%s' and build time '%s'".formatted(McpServerInfo.VERSION, McpServerInfo.BUILD_TIMESTAMP));
    }
    
    @Test
    void buildIdOf_ok_joinsTheVersionAndTheBuildTime() {
        assertEquals(Optional.of("0.3.6+2026-11-09T08:00:00Z"), McpServerInfo.buildIdOf("0.3.6", "2026-11-09T08:00:00Z"));
    }
    
    @Test
    void buildIdOf_ng_isEmptyWhenEitherValueIsNotKnown() {
        assertEquals(Optional.empty(), McpServerInfo.buildIdOf("unknown", "2026-11-09T08:00:00Z"));
        assertEquals(Optional.empty(), McpServerInfo.buildIdOf("0.3.6", "unknown"));
        assertEquals(Optional.empty(), McpServerInfo.buildIdOf("0.3.6", ""));
        assertEquals(Optional.empty(), McpServerInfo.buildIdOf("0.3.6", null));
    }
    
    @Test
    void buildIdOf_ng_isEmptyWhenTheBuildLeftAPlaceholder() {
        // As when the classes run from an IDE that copies project.properties without replacing its placeholders
        assertEquals(Optional.empty(), McpServerInfo.buildIdOf("${project.version}", "2026-11-09T08:00:00Z"));
        assertEquals(Optional.empty(), McpServerInfo.buildIdOf("0.3.6", "${build.timestamp}"));
    }
}
