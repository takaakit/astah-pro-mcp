package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CommentDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CommentDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IComment;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IComment.html
@Slf4j
public class CommentTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public CommentTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_comt_info",
                            "Return detailed information about the specified comment (specified by ID).",
                            this::getInfo,
                            IdDTO.class,
                            CommentDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create comment tools", e);
            return List.of();
        }
    }

    private CommentDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get comment information: {}", param);

        IComment astahComment = astahProToolSupport.getComment(param.id());

        return CommentDTOAssembler.toDTO(astahComment);
    }
}
