package com.astahpromcp.tool.visualization;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.common.ImageConvertSupport;
import io.modelcontextprotocol.spec.McpSchema;
import net.sourceforge.plantuml.dot.ExeState;
import net.sourceforge.plantuml.dot.GraphvizRuntimeEnvironment;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DiagramRenderingTest {
    @Test
    void plantumlToolReturnsDecodablePng() throws Exception {
        ToolDefinition definition = new PlantumlTool().createToolDefinitions().getFirst();
        assertPng(definition, Map.of("plantumlCode", "@startuml\nAlice -> Bob : Hello\n@enduml"));
    }

    @Test
    void graphvizToolConvertsDotThroughSvgToPng() throws Exception {
        File dot = GraphvizRuntimeEnvironment.getInstance().getDotExe();
        assumeTrue(dot != null && ExeState.checkFile(dot) == ExeState.OK, "Graphviz is not installed");
        ToolDefinition definition = new GraphvizTool(new ImageConvertSupport()).createToolDefinitions().getFirst();
        assertPng(definition, Map.of("dotCode", "digraph G { Alice -> Bob; }"));
    }

    private static void assertPng(ToolDefinition definition, Map<String, Object> arguments) throws Exception {
        var result = definition.toolHandler().apply(null,
                new McpSchema.CallToolRequest(definition.toolSchema().name(), arguments, null));
        assertFalse(Boolean.TRUE.equals(result.isError()), () -> result.content().toString());
        assertEquals(1, result.content().size());
        var content = assertInstanceOf(McpSchema.ImageContent.class, result.content().getFirst());
        assertEquals("image/png", content.mimeType());
        try (var input = new ByteArrayInputStream(Base64.getDecoder().decode(content.data()))) {
            var image = ImageIO.read(input);
            assertNotNull(image);
            assertTrue(image.getWidth() > 20);
            assertTrue(image.getHeight() > 20);
        }
    }
}
