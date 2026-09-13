package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.FlowWithActionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.FlowWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.FlowDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.FlowDTOAssembler;
import com.change_vision.jude.api.inf.model.IFlow;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IFlow.html
@Slf4j
public class FlowTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public FlowTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_flow_info",
                "Return model element information about the specified flow (specified by ID).",
                this::getInfo,
                IdDTO.class,
                FlowDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_action_of_flow",
                "Set the action of the specified flow (specified by ID), and return the model element of the flow after it is set.",
                this::setAction,
                FlowWithActionDTO.class,
                FlowDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_guard_of_flow",
                "Set the guard of the specified flow (specified by ID), and return the model element of the flow after it is set.",
                this::setGuard,
                FlowWithGuardDTO.class,
                FlowDTO.class)
        );
    }

    private FlowDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get flow information: {}", param);

        IFlow astahFlow = astahProToolSupport.getFlow(param.id());

        return FlowDTOAssembler.toDTO(astahFlow);
    }

    private FlowDTO setAction(FlowWithActionDTO param) throws Exception {
        log.debug("Set action of flow: {}", param);

        IFlow astahFlow = astahProToolSupport.getFlow(param.targetFlowId());

        txnAstah.run( () -> {
            astahFlow.setAction(param.action());
        });

        return FlowDTOAssembler.toDTO(astahFlow);
    }

    private FlowDTO setGuard(FlowWithGuardDTO param) throws Exception {
        log.debug("Set guard of flow: {}", param);

        IFlow astahFlow = astahProToolSupport.getFlow(param.targetFlowId());

        txnAstah.run( () -> {
            astahFlow.setGuard(param.guard());
        });

        return FlowDTOAssembler.toDTO(astahFlow);
    }
}
