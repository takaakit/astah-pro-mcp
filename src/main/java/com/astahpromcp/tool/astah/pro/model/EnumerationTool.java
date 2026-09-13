package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EnumerationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.EnumerationDTOAssembler;
import com.change_vision.jude.api.inf.model.IEnumeration;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IEnumeration.html
@Slf4j
public class EnumerationTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public EnumerationTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }


    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_enum_info",
                "Return model element information about the specified enumeration (specified by ID).",
                this::getInfo,
                IdDTO.class,
                EnumerationDTO.class)
        );
    }

    private EnumerationDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get enumeration information: {}", param);

        IEnumeration astahEnumeration = astahProToolSupport.getEnumeration(param.id());

        return EnumerationDTOAssembler.toDTO(astahEnumeration);
    }
}
