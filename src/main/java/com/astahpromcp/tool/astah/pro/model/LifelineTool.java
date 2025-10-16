package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LifelineWithBaseClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LifelineDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LifelineDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ILifeline.html
@Slf4j
public class LifelineTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public LifelineTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.definition(
                    "get_life_info",
                    "Return detailed information about the specified lifeline (specified by ID).",
                    this::getInfo,
                    IdDTO.class,
                    LifelineDTO.class),

                ToolSupport.definition(
                    "set_base_cls_of_life",
                    "Set the base class of the specified lifeline (specified by ID), and return the lifeline information after it is set.",
                    this::setBaseClass,
                    LifelineWithBaseClassDTO.class,
                    LifelineDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create lifeline tools", e);
            return List.of();
        }
    }

    private LifelineDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get lifeline information: {}", param);

        ILifeline astahLifeline = astahProToolSupport.getLifeline(param.id());

        return LifelineDTOAssembler.toDTO(astahLifeline);
    }

    private LifelineDTO setBaseClass(McpSyncServerExchange exchange, LifelineWithBaseClassDTO param) throws Exception {
        log.debug("Set base class of lifeline: {}", param);

        ILifeline astahLifeline = astahProToolSupport.getLifeline(param.targetLifelineId());
        IClass astahBaseClass = astahProToolSupport.getClass(param.baseClassId());

        try {
            transactionManager.beginTransaction();
            astahLifeline.setBase(astahBaseClass);
            transactionManager.endTransaction();

            return LifelineDTOAssembler.toDTO(astahLifeline);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
