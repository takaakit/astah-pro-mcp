package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithAlignAttributeItemsDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithInitialDisplayLevelDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithModelTypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithNotationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.model.IERDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERDiagram.html
@Slf4j
public class ERDiagramTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ERDiagramTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_er_dgm_info",
                "Return model element information about the specified ER diagram (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ERDiagramDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_align_er_attr_items_of_er_dgm",
                "Set the align attribute items of the specified ER diagram (specified by ID), and return the model element of the ER diagram after it is set.",
                this::setAlignAttributeItems,
                ERDiagramWithAlignAttributeItemsDTO.class,
                ERDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_initial_display_level_of_er_dgm",
                "Set the initial display level (specified by string) of the specified ER diagram (specified by ID), and return the model element of the ER diagram after it is set.",
                this::setInitialDisplayLevel,
                ERDiagramWithInitialDisplayLevelDTO.class,
                ERDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_model_type_of_er_dgm",
                "Set the model type (specified by string) of the specified ER diagram (specified by ID), and return the model element of the ER diagram after it is set.",
                this::setModelType,
                ERDiagramWithModelTypeDTO.class,
                ERDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_notation_of_er_dgm",
                "Set the notation (specified by string) of the specified ER diagram (specified by ID), and return the model element of the ER diagram after it is set.",
                this::setNotation,
                ERDiagramWithNotationDTO.class,
                ERDiagramDTO.class)
        );
    }

    private ERDiagramDTO getInfo(IdDTO param) throws Exception {
        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.id());

        return ERDiagramDTOAssembler.toDTO(astahERDiagram);
    }

    private ERDiagramDTO setAlignAttributeItems(ERDiagramWithAlignAttributeItemsDTO param) throws Exception {
        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        txnAstah.run( () -> {
            astahERDiagram.setAlignAttributeItems(param.isAlignAttributeItems());
        });

        return ERDiagramDTOAssembler.toDTO(astahERDiagram);
    }

    private ERDiagramDTO setInitialDisplayLevel(ERDiagramWithInitialDisplayLevelDTO param) throws Exception {
        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        txnAstah.run( () -> {
            astahERDiagram.setInitialDisplayLevel(param.initialDisplayLevel());
        });

        return ERDiagramDTOAssembler.toDTO(astahERDiagram);
    }

    private ERDiagramDTO setModelType(ERDiagramWithModelTypeDTO param) throws Exception {
        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        txnAstah.run( () -> {
            astahERDiagram.setModelType(param.modelType());
        });

        return ERDiagramDTOAssembler.toDTO(astahERDiagram);
    }

    private ERDiagramDTO setNotation(ERDiagramWithNotationDTO param) throws Exception {
        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        txnAstah.run( () -> {
            astahERDiagram.setNotation(param.notation());
        });

        return ERDiagramDTOAssembler.toDTO(astahERDiagram);
    }
}
