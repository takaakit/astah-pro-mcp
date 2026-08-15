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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Activator implements BundleActivator {

    private McpServerApp server;

    @Override
    public void start(BundleContext context) throws Exception {
        // Both profiles need their own port, otherwise the second one fails to bind
        if (McpServerConfig.PORT_FOR_FULL_TOOL == McpServerConfig.PORT_FOR_QUERY_ONLY_TOOL) {
            showPluginDisabledDialog("The astah-pro-mcp plugin is disabled because the same port ("
                    + McpServerConfig.PORT_FOR_FULL_TOOL + ") is configured for both the full tool version ("
                    + McpServerConfig.ENV_PORT_FOR_FULL_TOOL + ") and the query-only tool version ("
                    + McpServerConfig.ENV_PORT_FOR_QUERY_ONLY_TOOL + ").");
            
            throw new IllegalStateException("The same port is configured for both profiles: " + McpServerConfig.PORT_FOR_FULL_TOOL);
        }

        // Check port availability
        List<Integer> portsInUse = PortAvailabilityChecker.findPortsInUse(
                McpServerConfig.HOST,
                List.of(McpServerConfig.PORT_FOR_FULL_TOOL, McpServerConfig.PORT_FOR_QUERY_ONLY_TOOL));

        if (!portsInUse.isEmpty()) {
            String ports = portsInUse.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            
            showPluginDisabledDialog("The astah-pro-mcp plugin is disabled because the port(s) are already in use: "
                    + ports + "\nThe ports can be changed with the environment variables "
                    + McpServerConfig.ENV_PORT_FOR_FULL_TOOL + " and "
                    + McpServerConfig.ENV_PORT_FOR_QUERY_ONLY_TOOL + ".");
            
            throw new IllegalStateException("Port is already in use: " + portsInUse.get(0));
        }

        // Configure Logback
        LogbackConfig.configure(McpServerConfig.ROOT_OUTPUT_DIR);

        // Report how the ports were resolved
        McpServerConfig.portResolutionWarnings().forEach(log::warn);
        log.info("MCP ports: full tool={}, query-only tool={}", McpServerConfig.PORT_FOR_FULL_TOOL, McpServerConfig.PORT_FOR_QUERY_ONLY_TOOL);

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
