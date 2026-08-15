package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithAggregationDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithCompositionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithNavigationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkEndDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.LinkEndDTOAssembler;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ILinkEnd.html
@Slf4j
public class LinkEndTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public LinkEndTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
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
            log.error("Failed to create link end tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_link_end_info",
                "Return model element information about the specified link end (specified by ID).",
                this::getInfo,
                IdDTO.class,
                LinkEndDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_aggr_of_link_end",
                "Set an aggregation of the specified link end (specified by ID), and return the model element of the link end after it is set.",
                this::setAggregation,
                LinkEndWithAggregationDTO.class,
                LinkEndDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_composition_of_link_end",
                "Set a composition of the specified link end (specified by ID), and return the model element of the link end after it is set.",
                this::setComposition,
                LinkEndWithCompositionDTO.class,
                LinkEndDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_navigation_of_link_end",
                "Set a navigation of the specified link end (specified by ID), and return the model element of the link end after it is set.",
                this::setNavigation,
                LinkEndWithNavigationDTO.class,
                LinkEndDTO.class)
        );
    }

    private LinkEndDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get link end information: {}", param);

        ILinkEnd astahLinkEnd = astahProToolSupport.getLinkEnd(param.id());

        return LinkEndDTOAssembler.toDTO(astahLinkEnd);
    }

    private LinkEndDTO setAggregation(LinkEndWithAggregationDTO param) throws Exception {
        log.debug("Set aggregation of link end: {}", param);

        ILinkEnd astahLinkEnd = astahProToolSupport.getLinkEnd(param.targetLinkEndId());

        txnAstah.run( () -> {
            astahLinkEnd.setAggregation(param.isAggregation());
        });

        return LinkEndDTOAssembler.toDTO(astahLinkEnd);
    }

    private LinkEndDTO setComposition(LinkEndWithCompositionDTO param) throws Exception {
        log.debug("Set composition of link end: {}", param);

        ILinkEnd astahLinkEnd = astahProToolSupport.getLinkEnd(param.targetLinkEndId());

        txnAstah.run( () -> {
            astahLinkEnd.setComposite(param.isComposition());
        });

        return LinkEndDTOAssembler.toDTO(astahLinkEnd);
    }

    private LinkEndDTO setNavigation(LinkEndWithNavigationDTO param) throws Exception {
        log.debug("Set navigation of link end: {}", param);

        ILinkEnd astahLinkEnd = astahProToolSupport.getLinkEnd(param.targetLinkEndId());

        txnAstah.run( () -> {
            astahLinkEnd.setNavigability(param.isNavigation() ? "Navigable" : "Non_Navigable");
        });

        return LinkEndDTOAssembler.toDTO(astahLinkEnd);
    }
}
