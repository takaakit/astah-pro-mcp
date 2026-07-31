package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.*;
import com.change_vision.jude.api.inf.editor.CompositeStructureModelEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/CompositeStructureModelEditor.html
@Slf4j
public class CompositeStructureModelEditorTool implements ToolProvider {

    private final CompositeStructureModelEditor compositeStructureModelEditor;
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public CompositeStructureModelEditorTool(CompositeStructureModelEditor compositeStructureModelEditor, ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.compositeStructureModelEditor = compositeStructureModelEditor;
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
            log.error("Failed to create composite structure model editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_connector_between_parts_and_ports",
                "Create a new connector between the specified source part (specified by ID) and the specified target part (specified by ID), each of which is connected via its port (specified by ID) when the port is specified, and return the newly created model element of the connector.",
                this::createConnector,
                NewConnectorBetweenPartsAndPortsDTO.class,
                ConnectorDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_provided_interface_of_port",
                "Create a new realization between the specified port (specified by ID) and the specified interface (specified by ID) so that the interface becomes a provided interface of the port, and return the newly created model element of the realization.",
                this::createRealization,
                NewProvidedInterfaceOfPortDTO.class,
                RealizationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_required_interface_of_port",
                "Create a new usage between the specified port (specified by ID) and the specified interface (specified by ID) so that the interface becomes a required interface of the port, and return the newly created model element of the usage.",
                this::createUsage,
                NewRequiredInterfaceOfPortDTO.class,
                UsageDTO.class)
        );
    }

    private ConnectorDTO createConnector(NewConnectorBetweenPartsAndPortsDTO param) throws Exception {
        log.debug("Create connector between parts: {}", param);

        IAttribute astahSourcePart = astahProToolSupport.getAttribute(param.sourcePartId());
        if (astahSourcePart.getAssociation() == null) {
            throw new IllegalArgumentException("Source part for connector must be an association end, not a plain attribute.");
        }

        IAttribute astahTargetPart = astahProToolSupport.getAttribute(param.targetPartId());
        if (astahTargetPart.getAssociation() == null) {
            throw new IllegalArgumentException("Target part for connector must be an association end, not a plain attribute.");
        }

        IPort astahSourcePort = getPortOfPart(param.sourcePortId(), astahSourcePart, "Source");
        IPort astahTargetPort = getPortOfPart(param.targetPortId(), astahTargetPart, "Target");

        IConnector createdAstahConnector = txnAstah.call( () -> {
            return compositeStructureModelEditor.createConnector(
                astahSourcePart,
                astahSourcePort,
                astahTargetPart,
                astahTargetPort);
        });

        return ConnectorDTOAssembler.toDTO(createdAstahConnector);
    }

    private IPort getPortOfPart(String portId, IAttribute astahPart, String side) throws Exception {
        if (portId.isEmpty()) {
            return null;
        }

        // Note: The API accepts only a port owned by the type of the part, and rejects any other port with an InvalidEditingException whose message does not tell which argument is wrong.
        IPort astahPort = astahProToolSupport.getPort(portId);
        if (astahPart.getType() == null || !astahPort.getOwner().equals(astahPart.getType())) {
            throw new IllegalArgumentException(side + " port for connector must be a port owned by the type of the " + side.toLowerCase() + " part.");
        }

        return astahPort;
    }

    private RealizationDTO createRealization(NewProvidedInterfaceOfPortDTO param) throws Exception {
        log.debug("Create provided interface of port: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());
        IClass astahInterface = astahProToolSupport.getClass(param.targetInterfaceId());

        IRealization createdAstahRealization = txnAstah.call( () -> {
            return compositeStructureModelEditor.createRealization(
                astahPort,
                astahInterface,
                "");
        });

        return RealizationDTOAssembler.toDTO(createdAstahRealization);
    }

    private UsageDTO createUsage(NewRequiredInterfaceOfPortDTO param) throws Exception {
        log.debug("Create required interface of port: {}", param);

        IPort astahPort = astahProToolSupport.getPort(param.targetPortId());
        IClass astahInterface = astahProToolSupport.getClass(param.targetInterfaceId());

        IUsage createdAstahUsage = txnAstah.call( () -> {
            return compositeStructureModelEditor.createUsage(
                astahPort,
                astahInterface,
                "");
        });

        return UsageDTOAssembler.toDTO(createdAstahUsage);
    }
}
