package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.ImageRegion;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ElementDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithColorDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithLabelDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/IPresentation.html
@Slf4j
public class PresentationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;
    private final boolean includeEditTools;

    public PresentationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.imageCaptureSupport = imageCaptureSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create presentation tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_element_of_prst",
                "Return the element that corresponds to the specified presentation (specified by ID).",
                this::getElement,
                IdDTO.class,
                ElementDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_label",
                "Set the label of the specified presentation (specified by ID), and return the presentation after it is set along with the updated diagram image. Note that escape sequences such as \\n cannot be used in labels, but actual newline characters (Unicode U+000A, embedded directly in the string) are supported.",
                this::setLabel,
                PresentationWithLabelDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_fill_color",
                "Change the fill color of the specified presentation (specified by ID), and return the presentation after it is changed along with the updated diagram image.",
                this::changeFillColor,
                PresentationWithColorDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_line_color",
                "Change the line color of the specified presentation (specified by ID), and return the presentation after it is changed along with the updated diagram image.",
                this::changeLineColor,
                PresentationWithColorDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_font_color",
                "Change the font color of the specified presentation (specified by ID), and return the presentation after it is changed along with the updated diagram image.",
                this::changeFontColor,
                PresentationWithColorDTO.class,
                PresentationDTO.class)
        );
    }

    private ElementDTO getElement(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get element corresponding to presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.id());

        IElement astahElement = astahPresentation.getModel();

        if (astahElement != null) {
            return ElementDTOAssembler.toDTO(astahElement);
        } else {
            throw new RuntimeException("No element exists that corresponds to the presentation.");
        }
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> setLabel(McpSyncServerExchange exchange, PresentationWithLabelDTO param) throws Exception {
        log.debug("Set label of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setLabel(param.label());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createImageContent(astahPresentation.getDiagram().getId(), ImageRegion.FULL);

        return Pair.of(dto, List.of(image));
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> changeFillColor(McpSyncServerExchange exchange, PresentationWithColorDTO param) throws Exception {
        log.debug("Change fill color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setProperty(Key.FILL_COLOR, param.color());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createImageContent(astahPresentation.getDiagram().getId(), ImageRegion.FULL);

        return Pair.of(dto, List.of(image));
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> changeLineColor(McpSyncServerExchange exchange, PresentationWithColorDTO param) throws Exception {
        log.debug("Change line color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setProperty(Key.LINE_COLOR, param.color());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createImageContent(astahPresentation.getDiagram().getId(), ImageRegion.FULL);

        return Pair.of(dto, List.of(image));
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> changeFontColor(McpSyncServerExchange exchange, PresentationWithColorDTO param) throws Exception {
        log.debug("Change font color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setProperty(Key.FONT_COLOR, param.color());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createImageContent(astahPresentation.getDiagram().getId(), ImageRegion.FULL);

        return Pair.of(dto, List.of(image));
    }
}
