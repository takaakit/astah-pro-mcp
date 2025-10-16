package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ILink.html
@Slf4j
public class LinkTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public LinkTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.definition(
                    "get_link_info",
                    "Return detailed information about the specified link (specified by ID).",
                    this::getInfo,
                    IdDTO.class,
                    LinkDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create link tools", e);
            return List.of();
        }
    }

    private LinkDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get link information: {}", param);

        ILink astahLink = astahProToolSupport.getLink(param.id());

        return LinkDTOAssembler.toDTO(astahLink);
    }
}
