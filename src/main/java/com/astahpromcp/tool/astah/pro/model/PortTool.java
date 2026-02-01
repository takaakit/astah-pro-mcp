package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PortWithBehaviorDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PortWithServiceDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PortDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.PortDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IPort.html
@Slf4j
public class PortTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public PortTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create port tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_port_info",
                        "Return detailed information about the specified port (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        PortDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_behavior_of_port",
                        "Set the behavior (specified by boolean) of the specified port (specified by ID), and return the port information after it is set.",
                        this::setBehavior,
                        PortWithBehaviorDTO.class,
                        PortDTO.class),

                ToolSupport.definition(
                        "set_service_of_port",
                        "Set the service (specified by boolean) of the specified port (specified by ID), and return the port information after it is set.",
                        this::setService,
                        PortWithServiceDTO.class,
                        PortDTO.class)
        );
    }

    private PortDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get port information: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.id());

        return PortDTOAssembler.toDTO(astahPort);
    }

    private PortDTO setBehavior(McpSyncServerExchange exchange, PortWithBehaviorDTO param) throws Exception {
        log.debug("Set behavior of port: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());

        try {
            transactionManager.beginTransaction();
            astahPort.setIsBehavior(param.isBehavior());
            transactionManager.endTransaction();

            return PortDTOAssembler.toDTO(astahPort);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private PortDTO setService(McpSyncServerExchange exchange, PortWithServiceDTO param) throws Exception {
        log.debug("Set service of port: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());

        try {
            transactionManager.beginTransaction();
            astahPort.setIsService(param.isService());
            transactionManager.endTransaction();

            return PortDTOAssembler.toDTO(astahPort);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
