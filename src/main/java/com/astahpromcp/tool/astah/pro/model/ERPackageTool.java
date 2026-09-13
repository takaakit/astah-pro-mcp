package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERPackageDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERPackageDTOAssembler;
import com.change_vision.jude.api.inf.model.IERPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERPackage.html
@Slf4j
public class ERPackageTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ERPackageTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_er_package_info",
                "Return model element information about the specified ER package (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ERPackageDTO.class)
        );
    }

    private ERPackageDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get ER package information: {}", param);

        IERPackage astahERPackage = astahProToolSupport.getERPackage(param.id());

        return ERPackageDTOAssembler.toDTO(astahERPackage);
    }
}
