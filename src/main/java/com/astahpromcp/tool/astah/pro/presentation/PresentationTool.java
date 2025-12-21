package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ElementDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithColorDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithLabelDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/presentation/IPresentation.html
@Slf4j
public class PresentationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public PresentationTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
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
                ToolSupport.definition(
                        "get_element_of_prst",
                        "Return the element information that corresponds to the specified presentation (specified by ID).",
                        this::getElement,
                        IdDTO.class,
                        ElementDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_label",
                        "Set the label of the specified presentation (specified by ID), and return the presentation information after it is set. Note that newline characters (\\n) cannot be used in labels.",
                        this::setLabel,
                        PresentationWithLabelDTO.class,
                        PresentationDTO.class),

                ToolSupport.definition(
                        "change_fill_color",
                        "Change the fill color of the specified presentation (specified by ID), and return the presentation information after it is changed.",
                        this::changeFillColor,
                        PresentationWithColorDTO.class,
                        PresentationDTO.class),

                ToolSupport.definition(
                        "change_line_color",
                        "Change the line color of the specified presentation (specified by ID), and return the presentation information after it is changed.",
                        this::changeLineColor,
                        PresentationWithColorDTO.class,
                        PresentationDTO.class),

                ToolSupport.definition(
                        "change_font_color",
                        "Change the font color of the specified presentation (specified by ID), and return the presentation information after it is changed.",
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

    private PresentationDTO setLabel(McpSyncServerExchange exchange, PresentationWithLabelDTO param) throws Exception {
        log.debug("Set label of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        try {
            transactionManager.beginTransaction();
            astahPresentation.setLabel(param.label());
            transactionManager.endTransaction();

            return PresentationDTOAssembler.toDTO(astahPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private PresentationDTO changeFillColor(McpSyncServerExchange exchange, PresentationWithColorDTO param) throws Exception {
        log.debug("Change fill color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        try {
            transactionManager.beginTransaction();
            astahPresentation.setProperty(Key.FILL_COLOR, param.color());
            transactionManager.endTransaction();

            return PresentationDTOAssembler.toDTO(astahPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private PresentationDTO changeLineColor(McpSyncServerExchange exchange, PresentationWithColorDTO param) throws Exception {
        log.debug("Change line color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        try {
            transactionManager.beginTransaction();
            astahPresentation.setProperty(Key.LINE_COLOR, param.color());
            transactionManager.endTransaction();

            return PresentationDTOAssembler.toDTO(astahPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private PresentationDTO changeFontColor(McpSyncServerExchange exchange, PresentationWithColorDTO param) throws Exception {
        log.debug("Change font color of presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        try {
            transactionManager.beginTransaction();
            astahPresentation.setProperty(Key.FONT_COLOR, param.color());
            transactionManager.endTransaction();

            return PresentationDTOAssembler.toDTO(astahPresentation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
