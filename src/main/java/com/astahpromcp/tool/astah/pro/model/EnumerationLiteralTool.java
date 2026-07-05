package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.EnumerationLiteralWithValueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EnumerationLiteralDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.EnumerationLiteralDTOAssembler;
import com.change_vision.jude.api.inf.model.IEnumerationLiteral;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

@Slf4j
public class EnumerationLiteralTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public EnumerationLiteralTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
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
            log.error("Failed to create enumeration literal tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_val_of_enum_literal",
                "Set the value of the specified enumeration literal (specified by ID), and return the model element of the enumeration literal after it is set.",
                this::setValue,
                EnumerationLiteralWithValueDTO.class,
                EnumerationLiteralDTO.class)
        );
    }

    private EnumerationLiteralDTO setValue(McpSyncServerExchange exchange, EnumerationLiteralWithValueDTO param) throws Exception {
        log.debug("Set value of enumeration literal: {}", param);

        IEnumerationLiteral astahEnumerationLiteral = astahProToolSupport.getEnumerationLiteral(param.targetEnumerationLiteralId());

        txnAstah.run( () -> {
            astahEnumerationLiteral.setValue(param.value());
        });

        return EnumerationLiteralDTOAssembler.toDTO(astahEnumerationLiteral);
    }
}
