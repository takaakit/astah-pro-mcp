package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InstanceSpecificationWithClassifierDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InstanceSpecificationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.InstanceSpecificationDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IInstanceSpecification.html
@Slf4j
public class InstanceSpecificationTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public InstanceSpecificationTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create instance specification tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_inst_spec_info",
                        "Return detailed information about the specified instance specification (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        InstanceSpecificationDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_cls_to_inst_spec",
                        "Set the classifier (specified by ID) to the instance specification (specified by ID), and return the instance specification information after it is edited.",
                        this::setClassifier,
                        InstanceSpecificationWithClassifierDTO.class,
                        InstanceSpecificationDTO.class)
        );
    }

    private InstanceSpecificationDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get instance specification information: {}", param);

        IInstanceSpecification astahInstanceSpecification = astahProToolSupport.getInstanceSpecification(param.id());

        return InstanceSpecificationDTOAssembler.toDTO(astahInstanceSpecification);
    }

    private InstanceSpecificationDTO setClassifier(McpSyncServerExchange exchange, InstanceSpecificationWithClassifierDTO param) throws Exception {
        log.debug("Set classifier to instance specification: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassifierId());
        IInstanceSpecification astahInstanceSpecification = astahProToolSupport.getInstanceSpecification(param.targetInstanceSpecificationId());

        try {
            transactionManager.beginTransaction();
            astahInstanceSpecification.setClassifier(astahClass);
            transactionManager.endTransaction();

            return InstanceSpecificationDTOAssembler.toDTO(astahInstanceSpecification);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
