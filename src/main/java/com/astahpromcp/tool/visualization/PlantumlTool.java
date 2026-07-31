package com.astahpromcp.tool.visualization;

import com.astahpromcp.tool.*;
import com.astahpromcp.tool.visualization.inputdto.PlantumlDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.dot.ExeState;
import net.sourceforge.plantuml.dot.GraphvizRuntimeEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

@Slf4j
public class PlantumlTool implements ToolProvider {

    public PlantumlTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningContents(
                    "generate_dgm_img_from_puml",
                    "Generate a diagram image based on the provided PlantUML code. Use this tool, for example, to visualize the structure and behavior of architectures and algorithms. Some diagram types require the DOT command for rendering.",
                    this::generateDiagramImageFromPlantuml,
                    PlantumlDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create plantuml tools", e);
            return List.of();
        }
    }

    private List<McpSchema.Content> generateDiagramImageFromPlantuml(PlantumlDTO param) throws Exception {
        log.debug("Generate diagram image from PlantUML code: {}", param);

        McpSchema.ImageContent content = createPngImageContent(param.plantumlCode());
        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(content);

        return contents;
    }

    private McpSchema.ImageContent createPngImageContent(String plantumlCode) throws Exception {
        log.debug("Generate PlantUML diagram image from code");

        if (plantumlCode == null || plantumlCode.trim().isEmpty()) {
            throw new IllegalArgumentException("PlantUML code is null or empty");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantumlCode);
            FileFormatOption formatOption = new FileFormatOption(FileFormat.PNG);
            DiagramDescription description = reader.outputImage(outputStream, formatOption);

            byte[] pngBytes = outputStream.toByteArray();

            if (pngBytes.length == 0) {
                throw new RuntimeException("PlantUML PNG generation produced empty result");
            }

            log.info("PlantUML PNG generation succeeded ({} bytes, description: {})",
                    pngBytes.length, description);

            String encoded = Base64.getEncoder().encodeToString(pngBytes);
            return McpSchema.ImageContent.builder(encoded, "image/png").build();

        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Insufficient memory to process PlantUML diagram", e);
        } catch (Exception e) {
            // If rendering failed while Graphviz (dot) is unavailable, the diagram type most likely requires Graphviz.
            boolean graphvizAvailable;
            try {
                File dotExe = GraphvizRuntimeEnvironment.getInstance().getDotExe();
                graphvizAvailable = dotExe != null && ExeState.checkFile(dotExe) == ExeState.OK;
            } catch (Exception ex) {
                log.debug("Failed to check Graphviz availability", ex);
                graphvizAvailable = false;
            }

            if (!graphvizAvailable) {
                throw new RuntimeException("Failed to render the diagram because Graphviz (dot) is not available.", e);
            } else {
                throw new RuntimeException("Failed to convert PlantUML to PNG", e);
            }
        }
    }
}
