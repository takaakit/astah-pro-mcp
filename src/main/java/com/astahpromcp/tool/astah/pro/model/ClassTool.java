package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithActiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithLeafDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IClass.html
@Slf4j
public class ClassTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public ClassTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_cls_info",
                            "Return detailed information about the specified class or interface (specified by ID).",
                            this::getInfo,
                            IdDTO.class,
                            ClassDTO.class),

                    ToolSupport.definition(
                            "set_abs_of_class",
                            "Set the Abstract property of the specified class (specified by ID), and return the class information after it is set.",
                            this::setAbstract,
                            ClassWithAbstractDTO.class,
                            ClassDTO.class),

                    ToolSupport.definition(
                            "set_act_of_class",
                            "Set the Active property of the specified class (specified by ID), and return the class information after it is set.",
                            this::setActive,
                            ClassWithActiveDTO.class,
                            ClassDTO.class),

                    ToolSupport.definition(
                            "set_leaf_of_class",
                            "Set the Leaf property of the specified class (specified by ID), and return the class information after it is set.",
                            this::setLeaf,
                            ClassWithLeafDTO.class,
                            ClassDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create class tools", e);
            return List.of();
        }
    }

    private ClassDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get class or interface information: {}", param);
        
        IClass astahClass = astahProToolSupport.getClass(param.id());

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO setAbstract(McpSyncServerExchange exchange, ClassWithAbstractDTO param) throws Exception {
        log.debug("Set abstract of class: {}", param);
        
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        try {
            transactionManager.beginTransaction();
            astahClass.setAbstract(param.isAbstract());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(astahClass);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ClassDTO setActive(McpSyncServerExchange exchange, ClassWithActiveDTO param) throws Exception {
        log.debug("Set active of class: {}", param);
        
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        try {
            transactionManager.beginTransaction();
            astahClass.setActive(param.isActive());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(astahClass);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ClassDTO setLeaf(McpSyncServerExchange exchange, ClassWithLeafDTO param) throws Exception {
        log.debug("Set leaf of class: {}", param);
        
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        try {
            transactionManager.beginTransaction();
            astahClass.setLeaf(param.isLeaf());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(astahClass);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
