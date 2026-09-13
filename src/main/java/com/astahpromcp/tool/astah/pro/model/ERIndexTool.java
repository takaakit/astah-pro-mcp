package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithERAttributeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithKeyDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithUniqueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERIndexDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERIndexDTOAssembler;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERIndex.html
@Slf4j
public class ERIndexTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ERIndexTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_er_index_info",
                "Return model element information about the specified ER index (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ERIndexDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "add_er_attr_to_er_index",
                "Add the ER attribute (specified by ID) to the specified ER index (specified by ID), and return the model element of the ER index after it is set.",
                this::addERAttribute,
                ERIndexWithERAttributeDTO.class,
                ERIndexDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_er_attr_from_er_index",
                "Remove the ER attribute (specified by ID) from the specified ER index (specified by ID), and return the model element of the ER index after it is set.",
                this::removeERAttribute,
                ERIndexWithERAttributeDTO.class,
                ERIndexDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_key_of_er_index",
                "Set the key of the specified ER index (specified by ID), and return the model element of the ER index after it is set.",
                this::setKey,
                ERIndexWithKeyDTO.class,
                ERIndexDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_unique_of_er_index",
                "Set the unique of the specified ER index (specified by ID), and return the model element of the ER index after it is set.",
                this::setUnique,
                ERIndexWithUniqueDTO.class,
                ERIndexDTO.class)
        );
    }

    private ERIndexDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get ER index information: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.id());

        return ERIndexDTOAssembler.toDTO(astahERIndex);
    }

    private ERIndexDTO addERAttribute(ERIndexWithERAttributeDTO param) throws Exception {
        log.debug("Add ER attribute to ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());
        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.erAttributeId());

        txnAstah.run( () -> {
            astahERIndex.addERAttribute(astahERAttribute);
        });

        return ERIndexDTOAssembler.toDTO(astahERIndex);
    }

    private ERIndexDTO removeERAttribute(ERIndexWithERAttributeDTO param) throws Exception {
        log.debug("Remove ER attribute from ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());
        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.erAttributeId());

        txnAstah.run( () -> {
            astahERIndex.removeERAttribute(astahERAttribute);
        });

        return ERIndexDTOAssembler.toDTO(astahERIndex);
    }

    private ERIndexDTO setKey(ERIndexWithKeyDTO param) throws Exception {
        log.debug("Set key of ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());

        txnAstah.run( () -> {
            astahERIndex.setKey(param.isKey());
        });

        return ERIndexDTOAssembler.toDTO(astahERIndex);
    }

    private ERIndexDTO setUnique(ERIndexWithUniqueDTO param) throws Exception {
        log.debug("Set unique of ER index: {}", param);

        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.targetERIndexId());

        txnAstah.run( () -> {
            astahERIndex.setUnique(param.isUnique());
        });

        return ERIndexDTOAssembler.toDTO(astahERIndex);
    }
}
