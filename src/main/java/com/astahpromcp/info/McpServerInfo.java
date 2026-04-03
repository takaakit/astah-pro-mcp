package com.astahpromcp.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Information of the MCP server
public final class McpServerInfo {

    private McpServerInfo() {
    }

    // MCP server version
    public static final String VERSION = loadVersion();

    private static String loadVersion() {
        try (InputStream is = McpServerInfo.class.getClassLoader().getResourceAsStream("project.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("project.version", "unknown");
            } else {
                return "unknown";
            }

        } catch (IOException e) {
            return "unknown";
        }
    }
}
