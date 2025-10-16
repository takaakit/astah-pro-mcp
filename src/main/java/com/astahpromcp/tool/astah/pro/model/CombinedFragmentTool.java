package com.astahpromcp.tool.astah.pro.model;

import java.util.List;

import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.NewInteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.CombinedFragmentWithKindDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CombinedFragmentDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.CombinedFragmentDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ICombinedFragment.html
@Slf4j
public class CombinedFragmentTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public CombinedFragmentTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
                ToolSupport.definition(
                        "add_inter_oper",
                        "Add an interaction operand to the specified combined fragment (specified by ID), and return the combined fragment information after it is edited.",
                        this::addInteractionOperand,
                        NewInteractionOperandDTO.class,
                        CombinedFragmentDTO.class),

                ToolSupport.definition(
                        "set_comb_flag_kind",
                        "Set the kind (specified by string) of the specified combined fragment (specified by ID), and return the combined fragment information after it is edited.",
                        this::setCombinedFragmentKind,
                        CombinedFragmentWithKindDTO.class,
                        CombinedFragmentDTO.class)
        );
    }

    private CombinedFragmentDTO addInteractionOperand(McpSyncServerExchange exchange, NewInteractionOperandDTO param) throws Exception {
        log.debug("Add interaction operand: {}", param);

        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.targetCombinedFragmentId());

        try {
            transactionManager.beginTransaction();
            astahCombinedFragment.addInteractionOperand(param.newInteractionOperandName(), param.guard());
            transactionManager.endTransaction();

            return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private CombinedFragmentDTO setCombinedFragmentKind(McpSyncServerExchange exchange, CombinedFragmentWithKindDTO param) throws Exception {
        log.debug("Set combined fragment kind: {}", param);

        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.targetCombinedFragmentId());

        try {
            transactionManager.beginTransaction();
            astahCombinedFragment.setInteractionOperator(param.kind().toAstahValue());
            transactionManager.endTransaction();

            return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
