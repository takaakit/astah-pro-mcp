package com.astahpromcp.tool.visualization;

import com.astahpromcp.tool.*;
import com.astahpromcp.tool.visualization.inputdto.PlantumlDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.dot.ExeState;
import net.sourceforge.plantuml.dot.GraphvizRuntimeEnvironment;
import net.sourceforge.plantuml.error.PSystemError;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

@Slf4j
public class PlantumlTool implements ToolProvider {
    
    static final String ERROR_NOTE = "PlantUML could not render this code, so the image shows the error PlantUML reported instead of the diagram. The code is rendered in PlantUML's sandbox mode, so local files and URLs are unavailable (!include or !includeurl of a path or URL, !theme ... from, %load_json, %file_exists, and <img:...> of a file), while the standard library and the built-in themes are available.";
    
    // The image PlantUML drew, and whether it shows an error PlantUML reported instead of the diagram
    record Rendering(
        byte[] bytes,
        boolean reportsError
    ) {
    }
    
    public PlantumlTool() {
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningContents(
                    "generate_dgm_img_from_puml",
                    "Generate a diagram image based on the provided PlantUML code. Use this tool, for example, to visualize the structure and behavior of architectures and algorithms. Some diagram types require the DOT command for rendering. To write the PlantUML code, consult the PlantUML guide.",
                    this::generateDiagramImageFromPlantuml,
                    PlantumlDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create plantuml tools", e);
            return List.of();
        }
    }
    
    private List<McpSchema.Content> generateDiagramImageFromPlantuml(PlantumlDTO param) throws Exception {
        Rendering rendering = render(param.plantumlCode(), FileFormat.PNG);
        List<McpSchema.Content> contents = new ArrayList<>();
        
        // The note goes before the image, as GraphvizTool does with its own note
        if (rendering.reportsError()) {
            contents.add(McpSchema.TextContent.builder(ERROR_NOTE).build());
        }
        
        String encoded = Base64.getEncoder().encodeToString(rendering.bytes());
        contents.add(McpSchema.ImageContent.builder(encoded, "image/png").build());
        
        return contents;
    }
    
    // Render PlantUML code in the given format
    static Rendering render(String plantumlCode, FileFormat format) throws Exception {
        log.debug("Generate PlantUML diagram image from code");
        
        if (plantumlCode == null || plantumlCode.trim().isEmpty()) {
            throw new IllegalArgumentException("PlantUML code is null or empty");
        }
        
        // Checked before rendering and outside the try below, which would report a refusal as a missing Graphviz
        PlantumlSandbox.ensure();
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantumlCode);
            FileFormatOption formatOption = new FileFormatOption(format);
            DiagramDescription description = reader.outputImage(outputStream, formatOption);
            
            byte[] bytes = outputStream.toByteArray();
            
            if (bytes.length == 0) {
                throw new RuntimeException("PlantUML " + format + " generation produced empty result");
            }
            
            log.info("PlantUML {} generation succeeded ({} bytes, description: {})",
                    format, bytes.length, description);
            
            return new Rendering(bytes, reportsError(reader));
            
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
                throw new RuntimeException("Failed to convert PlantUML to " + format, e);
            }
        }
    }
    
    // Whether PlantUML drew an error instead of a diagram
    private static boolean reportsError(SourceStringReader reader) {
        for (BlockUml block : reader.getBlocks()) {
            Diagram diagram = block.getDiagram();
            if (diagram.getNbImages() > 0) {
                return diagram instanceof PSystemError;
            }
        }
        
        // No diagram at all, in which case PlantUML draws a message saying so
        return true;
    }
}
