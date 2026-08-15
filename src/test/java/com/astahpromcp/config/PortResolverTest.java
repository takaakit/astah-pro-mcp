package com.astahpromcp.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PortResolverTest {

    private static final String ENV_NAME = "ASTAH_PRO_MCP_PORT_FOR_FULL";
    private static final int DEFAULT_PORT = 8888;

    @Test
    void resolve_ok_usesDefaultWhenValueIsNotConfigured() {
        PortResolver.Result result = PortResolver.resolve(ENV_NAME, null, DEFAULT_PORT);

        assertEquals(DEFAULT_PORT, result.port());
        assertNull(result.warning(), "An unset variable is the normal case and must not warn");
    }

    @Test
    void resolve_ok_usesDefaultWhenValueIsBlank() {
        for (String rawValue : new String[]{"", "   "}) {
            PortResolver.Result result = PortResolver.resolve(ENV_NAME, rawValue, DEFAULT_PORT);

            assertEquals(DEFAULT_PORT, result.port(), () -> "Failed for '" + rawValue + "'");
            assertNull(result.warning(), "A blank variable is treated as unset and must not warn");
        }
    }

    @Test
    void resolve_ok_usesConfiguredPort() {
        PortResolver.Result result = PortResolver.resolve(ENV_NAME, "9888", DEFAULT_PORT);

        assertEquals(9888, result.port());
        assertNull(result.warning());
    }

    @Test
    void resolve_ok_ignoresSurroundingWhitespace() {
        PortResolver.Result result = PortResolver.resolve(ENV_NAME, "  9888  ", DEFAULT_PORT);

        assertEquals(9888, result.port());
        assertNull(result.warning());
    }

    @Test
    void resolve_ok_acceptsRangeBoundaries() {
        for (int port : new int[]{PortResolver.MIN_PORT, PortResolver.MAX_PORT}) {
            PortResolver.Result result = PortResolver.resolve(ENV_NAME, String.valueOf(port), DEFAULT_PORT);

            assertEquals(port, result.port(), () -> "Failed for " + port);
            assertNull(result.warning(), () -> "Boundary port " + port + " must be accepted without a warning");
        }
    }

    @Test
    void resolve_ng_fallsBackToDefaultWhenValueIsNotANumber() {
        for (String rawValue : new String[]{"abc", "98 88", "9888a", "88.8", "+-1"}) {
            assertFallsBackWithWarning(rawValue);
        }
    }

    @Test
    void resolve_ng_fallsBackToDefaultWhenPortIsOutOfRange() {
        for (String rawValue : new String[]{"0", "-1", "80", "1023", "65536", "70000"}) {
            assertFallsBackWithWarning(rawValue);
        }
    }

    // An unusable value must never prevent the plugin from starting; it falls back and tells the user why.
    private void assertFallsBackWithWarning(String rawValue) {
        PortResolver.Result result = PortResolver.resolve(ENV_NAME, rawValue, DEFAULT_PORT);

        assertEquals(DEFAULT_PORT, result.port(), () -> "Expected a fallback to the default port for '" + rawValue + "'");
        assertNotNull(result.warning(), () -> "The user must be told why '" + rawValue + "' was ignored");
        assertTrue(result.warning().contains(ENV_NAME), () -> "The warning must name the setting to fix, but was: " + result.warning());
    }
}
