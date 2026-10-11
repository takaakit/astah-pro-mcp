package com.astahpromcp.tool.visualization;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.*;
import com.astahpromcp.tool.common.ImageConvertSupport;
import com.astahpromcp.tool.common.ImageConvertSupport.FittedSvgImage;
import com.astahpromcp.tool.common.ImageConvertSupport.ParsedSvg;
import com.astahpromcp.tool.visualization.inputdto.DotDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.dot.ExeState;
import net.sourceforge.plantuml.dot.Graphviz;
import net.sourceforge.plantuml.dot.GraphvizRuntimeEnvironment;
import net.sourceforge.plantuml.dot.ProcessState;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

@Slf4j
public class GraphvizTool implements ToolProvider {

    private final ImageConvertSupport imageConvertSupport;

    public GraphvizTool(ImageConvertSupport imageConvertSupport) {
        this.imageConvertSupport = imageConvertSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningContents(
                    "generate_graph_img_from_dot",
                    "Generate a graph image based on the provided Graphviz DOT code. Use this tool, for example, to visualize an algorithm's call graph or an abstract syntax tree. The DOT command is required for rendering.",
                    this::generateGraphImageFromDot,
                    DotDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create graphviz tools", e);
            return List.of();
        }
    }

    private List<McpSchema.Content> generateGraphImageFromDot(DotDTO param) throws Exception {
        return createPngContentsFromDot(param.dotCode());
    }

    private List<McpSchema.Content> createPngContentsFromDot(String dotCode) throws Exception {
        log.debug("Generate Graphviz graph image from DOT code");

        if (dotCode == null || dotCode.trim().isEmpty()) {
            throw new IllegalArgumentException("DOT code is null or empty");
        }

        // Check Graphviz (dot) availability
        boolean graphvizAvailable;
        try {
            File dotExe = GraphvizRuntimeEnvironment.getInstance().getDotExe();
            graphvizAvailable = dotExe != null && ExeState.checkFile(dotExe) == ExeState.OK;
        } catch (Exception e) {
            log.debug("Failed to check Graphviz availability", e);
            graphvizAvailable = false;
        }

        if (!graphvizAvailable) {
            throw new RuntimeException("Graphviz (dot) is not available.");
        }

        // Generate SVG from DOT using Graphviz.
        String svg;
        try (ByteArrayOutputStream svgOutputStream = new ByteArrayOutputStream()) {
            Graphviz graphviz = GraphvizRuntimeEnvironment.getInstance().create(null, dotCode, "svg");
            ProcessState state = graphviz.createFile3(svgOutputStream);

            svg = svgOutputStream.toString(StandardCharsets.UTF_8);

            // dot may return TERMINATED_OK while writing an error message instead of SVG,
            // so confirm the output actually contains an <svg> element.
            if (state.differs(ProcessState.TERMINATED_OK()) || !svg.contains("<svg")) {
                throw new RuntimeException("Failed to generate SVG from DOT code. Graphviz output: " + svg);
            }
        }

        // Graphviz emits the "transparent" paint value, which the bundled Batik rejects as an invalid CSS
        // value and logs a stack trace for on every call. Replacing it with "none" (visually identical for
        // paint: no paint is applied) keeps the rendering correct while silencing the noise.
        svg = svg.replace("\"transparent\"", "\"none\"");

        // Convert SVG to PNG via Batik. A large graph is scaled down to the pixel limit rather than refused, and the
        // scale is reported, so that the agent does not take the smaller text for the size Graphviz drew it at.
        ParsedSvg parsedSvg = imageConvertSupport.parseSvg(svg);
        List<McpSchema.Content> contents = new ArrayList<>();

        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            FittedSvgImage fitted = imageConvertSupport.rasterizeWithinPixelLimit(parsedSvg);
            BufferedImage image = fitted.image();
            if (fitted.scale() < 1.0) {
                log.info("Graphviz graph scaled down to {}x{} from {}x{} to stay within {} pixels",
                        image.getWidth(), image.getHeight(), parsedSvg.naturalWidth(), parsedSvg.naturalHeight(), McpServerConfig.SVG_MAX_RASTER_PIXELS);

                contents.add(McpSchema.TextContent.builder(String.format(
                    "The graph was rendered at %.0f%% of its natural size (%dx%d pixels instead of %dx%d) to stay within the limit of %d pixels per image, so its text and lines are smaller than Graphviz drew them.",
                    fitted.scale() * 100, image.getWidth(), image.getHeight(), parsedSvg.naturalWidth(), parsedSvg.naturalHeight(), McpServerConfig.SVG_MAX_RASTER_PIXELS)).build());
            }

            if (!ImageIO.write(image, "png", pngOutputStream)) {
                throw new RuntimeException("No PNG writer is available to encode the image");
            }

            byte[] pngBytes = pngOutputStream.toByteArray();

            if (pngBytes.length == 0) {
                throw new RuntimeException("Graphviz PNG generation produced empty result");
            }

            log.info("Graphviz PNG generation succeeded ({} bytes)", pngBytes.length);

            String encoded = Base64.getEncoder().encodeToString(pngBytes);
            contents.add(McpSchema.ImageContent.builder(encoded, "image/png").build());

            return contents;

        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Insufficient memory to process Graphviz graph", e);
        }
    }
}
