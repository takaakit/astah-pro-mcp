package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.RealizationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.RealizationDTOAssembler;
import com.change_vision.jude.api.inf.model.IRealization;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IRealization.html
@Slf4j
public class RealizationTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public RealizationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_real_info",
                "Return model element information about the specified realization (specified by ID).",
                this::getInfo,
                IdDTO.class,
                RealizationDTO.class)
        );
    }

    private RealizationDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get realization information: {}", param);

        IRealization astahRealization = astahProToolSupport.getRealization(param.id());

        return RealizationDTOAssembler.toDTO(astahRealization);
    }
}
