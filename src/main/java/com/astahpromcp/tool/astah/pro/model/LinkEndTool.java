package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithAggregationDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithCompositionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithNavigationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkEndDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkEndDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ILinkEnd.html
@Slf4j
public class LinkEndTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public LinkEndTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create link end tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_aggr_of_link_end",
                        "Set an aggregation of the specified link end (specified by ID), and return the link end information after it is set.",
                        this::setAggregation,
                        LinkEndWithAggregationDTO.class,
                        LinkEndDTO.class),

                ToolSupport.definition(
                        "set_comp_of_link_end",
                        "Set a composition of the specified link end (specified by ID), and return the link end information after it is set.",
                        this::setComposition,
                        LinkEndWithCompositionDTO.class,
                        LinkEndDTO.class),

                ToolSupport.definition(
                        "set_nav_of_link_end",
                        "Set a navigation of the specified link end (specified by ID), and return the link end information after it is set.",
                        this::setNavigation,
                        LinkEndWithNavigationDTO.class,
                        LinkEndDTO.class)
        );
    }

    private LinkEndDTO setAggregation(McpSyncServerExchange exchange, LinkEndWithAggregationDTO param) throws Exception {
        log.debug("Set aggregation of link end: {}", param);

        ILinkEnd astahLinkEnd = astahProToolSupport.getLinkEnd(param.targetLinkEndId());

        try {
            transactionManager.beginTransaction();
            astahLinkEnd.setAggregation(param.isAggregation());
            transactionManager.endTransaction();

            return LinkEndDTOAssembler.toDTO(astahLinkEnd);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkEndDTO setComposition(McpSyncServerExchange exchange, LinkEndWithCompositionDTO param) throws Exception {
        log.debug("Set composition of link end: {}", param);

        ILinkEnd astahLinkEnd = astahProToolSupport.getLinkEnd(param.targetLinkEndId());

        try {
            transactionManager.beginTransaction();
            astahLinkEnd.setComposite(param.isComposition());
            transactionManager.endTransaction();

            return LinkEndDTOAssembler.toDTO(astahLinkEnd);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private LinkEndDTO setNavigation(McpSyncServerExchange exchange, LinkEndWithNavigationDTO param) throws Exception {
        log.debug("Set navigation of link end: {}", param);

        ILinkEnd astahLinkEnd = astahProToolSupport.getLinkEnd(param.targetLinkEndId());

        try {
            transactionManager.beginTransaction();
            astahLinkEnd.setNavigability(param.isNavigation() ? "Navigable" : "Non_Navigable");
            transactionManager.endTransaction();

            return LinkEndDTOAssembler.toDTO(astahLinkEnd);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
