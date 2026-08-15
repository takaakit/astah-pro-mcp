package com.astahpromcp.tool.astah.pro.image;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.image.inputdto.SvgOverlayOnDiagramDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SvgOverlayTool implements ToolProvider {

    private final SvgOverlaySupport svgOverlaySupport;

    public SvgOverlayTool(SvgOverlaySupport svgOverlaySupport) {
        this.svgOverlaySupport = svgOverlaySupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.toolDefinitionReturningContents(
                "overlay_svg_img_on_dgm_img",
                "Overlay an SVG image (specified by SVG code) on the exported image of the specified diagram (specified by ID) and return the composed PNG image, without modifying the project. Specify an empty diagram ID to render the SVG image on its own, at the resolution diagram images are exported at. The returned PNG image is resized as needed to fit within the response size limit.",
                this::overlaySvgImageOnDiagramImage,
                SvgOverlayOnDiagramDTO.class)
        );
    }

    private List<McpSchema.Content> overlaySvgImageOnDiagramImage(SvgOverlayOnDiagramDTO param) throws Exception {
        log.debug("Overlay SVG image on diagram image: {}", param);

        // A diagram with nothing drawn on it yields an explanatory text alongside the image, so the contents are
        // returned as the support built them rather than wrapped into a fixed single-image list.
        return new ArrayList<>(svgOverlaySupport.createSvgOverlayContents(
                param.targetDiagramId(),
                param.imageSvgCode()));
    }
}
