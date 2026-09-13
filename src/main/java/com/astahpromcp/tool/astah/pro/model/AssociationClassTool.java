package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.AssociationClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.AssociationClassDTOAssembler;
import com.change_vision.jude.api.inf.model.IAssociationClass;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IAssociationClass.html
@Slf4j
public class AssociationClassTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public AssociationClassTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_asso_class_info",
                "Return the model element information about the specified association class (specified by ID).",
                this::getInfo,
                IdDTO.class,
                AssociationClassDTO.class)
        );
    }

    private AssociationClassDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get association class information: {}", param);

        IAssociationClass astahAssociationClass = astahProToolSupport.getAssociationClass(param.id());

        return AssociationClassDTOAssembler.toDTO(astahAssociationClass);
    }
}
