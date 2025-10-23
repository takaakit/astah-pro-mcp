package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.EnumerationLiteralWithValueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EnumerationLiteralDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EnumerationLiteralDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IEnumerationLiteral;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class EnumerationLiteralTool implements ToolProvider {
    
    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public EnumerationLiteralTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "set_val_of_enum_lit",
                            "Set the value of the specified enumeration literal (specified by ID), and return the enumeration literal information after it is set.",
                            this::setValue,
                            EnumerationLiteralWithValueDTO.class,
                            EnumerationLiteralDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create enumeration literal tools", e);
            return List.of();
        }
    }

    private EnumerationLiteralDTO setValue(McpSyncServerExchange exchange, EnumerationLiteralWithValueDTO param) throws Exception {
        log.debug("Set value of enumeration literal: {}", param);
        
        IEnumerationLiteral astahEnumerationLiteral = astahProToolSupport.getEnumerationLiteral(param.targetEnumerationLiteralId());

        try {
            transactionManager.beginTransaction();
            astahEnumerationLiteral.setValue(param.value());
            transactionManager.endTransaction();

            return EnumerationLiteralDTOAssembler.toDTO(astahEnumerationLiteral);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
