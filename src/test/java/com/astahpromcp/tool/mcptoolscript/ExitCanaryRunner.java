package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.FakeTransactionBoundary;

import java.util.List;
import java.util.Map;

// Runs an mcp tool script that tries to kill the JVM, and reports what happened.
//
// Started as a separate process by McpToolScriptExecutorIsolationTest. If the neutralization ever stops working,
// this process ends at the exit() call with the code the script chose, and the parent sees that code. Running the
// same script in the test JVM would instead make the whole test run vanish, which says far less about why.
public final class ExitCanaryRunner {

    // Chosen so the parent can tell "the mcp tool script killed the JVM" apart from any ordinary failure exit code
    public static final int CANARY_EXIT_CODE = 37;

    public static final String SURVIVED_PREFIX = "SURVIVED:";

    private ExitCanaryRunner() {
    }

    public static void main(String[] args) {
        String script = args.length > 0 ? args[0] : ("exit(" + CANARY_EXIT_CODE + ");");

        ToolProvider empty = List::of;
        AstahToolRegistry registry = AstahToolRegistry.of(empty.createToolDefinitions(), Map.of());

        McpToolScriptExecutor.Result result = new McpToolScriptExecutor(registry, 15, new FakeTransactionBoundary()).execute(script);

        // Reaching this line at all is the result being tested
        System.out.println(SURVIVED_PREFIX + result.ok() + ":" + result.errorMessage().replace('\n', ' '));
        System.out.flush();

        Runtime.getRuntime().halt(0);
    }
}
