package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.InteractionDTOAssembler;
import com.change_vision.jude.api.inf.model.IInteraction;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IInteraction.html
@Slf4j
public class InteractionTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public InteractionTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_interaction_info",
                "Return model element information about the specified interaction (specified by ID).",
                this::getInfo,
                IdDTO.class,
                InteractionDTO.class)
        );
    }

    private InteractionDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get interaction information: {}", param);

        IInteraction astahInteraction = astahProToolSupport.getInteraction(param.id());

        return InteractionDTOAssembler.toDTO(astahInteraction);
    }
}
