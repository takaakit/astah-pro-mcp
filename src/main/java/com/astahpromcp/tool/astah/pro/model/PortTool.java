package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PortWithBehaviorDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PortWithServiceDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PortDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.PortDTOAssembler;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IPort.html
@Slf4j
public class PortTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public PortTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_port_info",
                "Return model element information about the specified port (specified by ID).",
                this::getInfo,
                IdDTO.class,
                PortDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_behavior_of_port",
                "Set the behavior (specified by boolean) of the specified port (specified by ID), and return the model element of the port after it is set.",
                this::setBehavior,
                PortWithBehaviorDTO.class,
                PortDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_service_of_port",
                "Set the service (specified by boolean) of the specified port (specified by ID), and return the model element of the port after it is set.",
                this::setService,
                PortWithServiceDTO.class,
                PortDTO.class)
        );
    }

    private PortDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get port information: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.id());

        return PortDTOAssembler.toDTO(astahPort);
    }

    private PortDTO setBehavior(PortWithBehaviorDTO param) throws Exception {
        log.debug("Set behavior of port: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());

        txnAstah.run( () -> {
            astahPort.setIsBehavior(param.isBehavior());
        });

        return PortDTOAssembler.toDTO(astahPort);
    }

    private PortDTO setService(PortWithServiceDTO param) throws Exception {
        log.debug("Set service of port: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());

        txnAstah.run( () -> {
            astahPort.setIsService(param.isService());
        });

        return PortDTOAssembler.toDTO(astahPort);
    }
}
