package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.CombinedFragmentWithKindDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NewInteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CombinedFragmentDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.CombinedFragmentDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ICombinedFragment.html
@Slf4j
public class CombinedFragmentTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public CombinedFragmentTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "add_interaction_operand",
                        "Add an interaction operand to the specified combined fragment (specified by ID), and return the combined fragment information after it is edited.",
                        this::addInteractionOperand,
                        NewInteractionOperandDTO.class,
                        CombinedFragmentDTO.class),

                ToolSupport.definition(
                        "set_combined_fragment_kind",
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
