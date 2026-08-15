package com.astahpromcp.config;

// Resolves an MCP endpoint port
public final class PortResolver {

    // Lowest port
    static final int MIN_PORT = 1024;

    // Highest port
    static final int MAX_PORT = 65535;

    public record Result(int port, String warning) {
    }

    private PortResolver() {
    }

    // Resolve the port from the given raw value.
    public static Result resolve(String name, String rawValue, int defaultPort) {
        if (rawValue == null || rawValue.isBlank()) {
            return new Result(defaultPort, null);
        }

        int port;
        try {
            port = Integer.parseInt(rawValue.trim());

        } catch (NumberFormatException e) {
            return new Result(defaultPort, name + "='" + rawValue + "' is not a number. Falling back to the default port " + defaultPort + ".");
        }

        if (port < MIN_PORT || port > MAX_PORT) {
            return new Result(defaultPort, name + "=" + port + " is outside the allowed range " + MIN_PORT + "-" + MAX_PORT + ". Falling back to the default port " + defaultPort + ".");
        }

        return new Result(port, null);
    }
}
