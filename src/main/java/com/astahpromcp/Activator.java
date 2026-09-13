package com.astahpromcp;

import com.astahpromcp.config.LogbackConfig;
import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.server.McpServerApp;
import com.astahpromcp.server.PortAvailabilityChecker;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Activator implements BundleActivator {

    private McpServerApp server;

    // A port the plugin listens on, with the environment variable that overrides it.
    private record PortSetting(
        String label,
        int port,
        String envVar
    ) {
    }

    private static List<PortSetting> portSettings() {
        return List.of(
                new PortSetting("direct tool calling mode",
                        McpServerConfig.PORT_FOR_DIRECT, McpServerConfig.ENV_PORT_FOR_DIRECT),
                new PortSetting("programmatic tool calling mode",
                        McpServerConfig.PORT_FOR_PROGRAMMATIC, McpServerConfig.ENV_PORT_FOR_PROGRAMMATIC));
    }

    @Override
    public void start(BundleContext context) throws Exception {
        // Every profile needs its own port, otherwise whichever starts second fails to bind.
        Map<Integer, PortSetting> byPort = new LinkedHashMap<>();
        for (PortSetting setting : portSettings()) {
            PortSetting clashing = byPort.putIfAbsent(setting.port(), setting);
            if (clashing != null) {
                showPluginDisabledDialog("The astah-pro-mcp plugin is disabled because the same port ("
                        + setting.port() + ") is configured for both the " + clashing.label() + " ("
                        + clashing.envVar() + ") and the " + setting.label() + " ("
                        + setting.envVar() + ").");

                throw new IllegalStateException("The same port is configured for two profiles: " + setting.port());
            }
        }

        // Check port availability
        List<Integer> portsInUse = PortAvailabilityChecker.findPortsInUse(
                McpServerConfig.HOST,
                portSettings().stream().map(PortSetting::port).toList());

        if (!portsInUse.isEmpty()) {
            String ports = portsInUse.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            String envVars = portSettings().stream()
                    .map(PortSetting::envVar)
                    .collect(Collectors.joining(", "));

            showPluginDisabledDialog("The astah-pro-mcp plugin is disabled because the port(s) are already in use: "
                    + ports + "\nThe ports can be changed with the environment variables " + envVars + ".");

            throw new IllegalStateException("Port is already in use: " + portsInUse.get(0));
        }

        // Configure Logback
        LogbackConfig.configure(McpServerConfig.ROOT_OUTPUT_DIR);

        // Report how the ports were resolved
        McpServerConfig.portResolutionWarnings().forEach(log::warn);
        log.info("MCP ports: direct tool calling={}, programmatic tool calling={}",
                McpServerConfig.PORT_FOR_DIRECT,
                McpServerConfig.PORT_FOR_PROGRAMMATIC);

        log.info("MCP-BUNDLE: Starting bundle...");

        // The class loader must be switched to the Jetty bundle's class loader to prevent ClassNotFoundExceptions for Jetty classes.
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        log.debug("Original context class loader: {}", originalClassLoader);

        // Set the context class loader to this bundle's class loader, which will contain Jetty
        currentThread.setContextClassLoader(Activator.class.getClassLoader());
        log.debug("Context class loader set to bundle class loader");

        try {
            this.server = new McpServerApp();
            this.server.start();
            log.info("MCP-BUNDLE: Bundle started successfully.");

        } catch (Throwable t) {
            log.error("MCP-BUNDLE: Failed to start MCP server", t);
            if (t instanceof Exception) {
                throw (Exception) t;
            }
            throw new Exception("Failed to start MCP server", t);

        } finally {
            // Restore the original class loader
            currentThread.setContextClassLoader(originalClassLoader);
            log.debug("Context class loader restored");
        }
    }

    // Notify the user that the plugin has been disabled
    private void showPluginDisabledDialog(String message) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);

            Frame tempOwner = null;
            JDialog dialog = null;
            try {
                tempOwner = new Frame();
                tempOwner.setUndecorated(true);
                tempOwner.setType(Window.Type.UTILITY);
                tempOwner.setAlwaysOnTop(true);
                tempOwner.setLocationRelativeTo(null);
                tempOwner.setVisible(true);

                dialog = optionPane.createDialog(tempOwner, "astah-pro-mcp");
                dialog.setAlwaysOnTop(true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                dialog.setVisible(true);

            } finally {
                if (dialog != null) {
                    dialog.dispose();
                }
                if (tempOwner != null) {
                    tempOwner.dispose();
                }
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug("MCP-BUNDLE: Stopping bundle...");
        if (this.server != null) {
            try {
                this.server.stop();
                log.info("MCP-BUNDLE: Bundle stopped successfully.");

            } catch (Exception e) {
                log.warn("MCP-BUNDLE: Exception occurred during shutdown: {}", e.getMessage());
                log.debug("MCP-BUNDLE: Shutdown exception details", e);

            } finally {
                this.server = null;
            }
        }
    }
}
