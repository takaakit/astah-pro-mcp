package com.astahpromcp;

import com.astahpromcp.config.LogbackConfig;
import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.server.McpServerApp;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@Slf4j
public class Activator implements BundleActivator {

    private McpServerApp server;

    @Override
    public void start(BundleContext context) throws Exception {
        // Configure Logback
        LogbackConfig.configure(McpServerConfig.ROOT_OUTPUT_DIR);
        
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
