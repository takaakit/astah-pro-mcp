package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ElementDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithColorDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithLabelDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationTypeListDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/IPresentation.html
@Slf4j
public class PresentationTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final ImageCaptureSupport imageCaptureSupport;

    public PresentationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, ImageCaptureSupport imageCaptureSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.imageCaptureSupport = imageCaptureSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_element_of_prst",
                "Return the element that corresponds to the specified presentation (specified by ID).",
                this::getElement,
                IdDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_all_prst_types",
                "Return the list of all presentation type names. A presentation type name is the notation the presentation is drawn with, which may differ from the type of the model element. For example, an enumeration, a component, an artifact and a use case on a class diagram are all drawn with the class rectangle, so their type is \"Class\". Note that \"Unknown\" is excluded from the type names.",
                this::getAllTypes,
                NoInputDTO.class,
                PresentationTypeListDTO.class),


            ToolSupport.toolDefinitionReturningDtoAndContents(
                "set_label",
                "Set the label of the specified presentation (specified by ID), and return the presentation after it is set along with the updated diagram image in low resolution. Note that escape sequences such as \\n cannot be used in labels, but actual newline characters (Unicode U+000A, embedded directly in the string) are supported.",
                this::setLabel,
                PresentationWithLabelDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_fill_color",
                "Change the fill color of the specified presentation (specified by ID), and return the presentation after it is changed along with the updated diagram image in low resolution.",
                this::changeFillColor,
                PresentationWithColorDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_line_color",
                "Change the line color of the specified presentation (specified by ID), and return the presentation after it is changed along with the updated diagram image in low resolution.",
                this::changeLineColor,
                PresentationWithColorDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDtoAndContents(
                "change_font_color",
                "Change the font color of the specified presentation (specified by ID), and return the presentation after it is changed along with the updated diagram image in low resolution.",
                this::changeFontColor,
                PresentationWithColorDTO.class,
                PresentationDTO.class)
        );
    }

    private ElementDTO getElement(IdDTO param) throws Exception {
        log.debug("Get element corresponding to presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.id());

        IElement astahElement = astahPresentation.getModel();

        if (astahElement != null) {
            return ElementDTOAssembler.toDTO(astahElement);
        } else {
            throw new RuntimeException("No element exists that corresponds to the presentation.");
        }
    }

    private PresentationTypeListDTO getAllTypes(NoInputDTO param) throws Exception {
        log.debug("Get all presentation types: {}", param);

        List<String> typeNames = Arrays.stream(PresentationDTO.Type.values())
                .filter(type -> type != PresentationDTO.Type.UNKNOWN)
                .map(type -> type.typeName)
                .toList();

        return new PresentationTypeListDTO(typeNames);
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> setLabel(PresentationWithLabelDTO param) throws Exception {
        log.debug("Set label of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setLabel(param.label());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(astahPresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> changeFillColor(PresentationWithColorDTO param) throws Exception {
        log.debug("Change fill color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setProperty(Key.FILL_COLOR, param.color());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(astahPresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> changeLineColor(PresentationWithColorDTO param) throws Exception {
        log.debug("Change line color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setProperty(Key.LINE_COLOR, param.color());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(astahPresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }

    private Pair<PresentationDTO, List<McpSchema.Content>> changeFontColor(PresentationWithColorDTO param) throws Exception {
        log.debug("Change font color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        txnAstah.run( () -> {
            astahPresentation.setProperty(Key.FONT_COLOR, param.color());
        });

        PresentationDTO dto = PresentationDTOAssembler.toDTO(astahPresentation);

        McpSchema.ImageContent image = imageCaptureSupport.createSmallImageContent(astahPresentation.getDiagram().getId());

        return Pair.of(dto, List.of(image));
    }
}
