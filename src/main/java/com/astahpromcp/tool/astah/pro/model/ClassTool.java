package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithActiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithInvariantDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithLeafDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ClassDTOAssembler;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IClass.html
@Slf4j
public class ClassTool implements ToolProvider {

    private final BasicModelEditor basicModelEditor;
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ClassTool(BasicModelEditor basicModelEditor, ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.basicModelEditor = basicModelEditor;
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
            log.error("Failed to create class tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_class_info",
                "Return model element information about the specified class or interface (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ClassDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_abstract_of_class",
                "Set the Abstract property of the specified class (specified by ID), and return the model element of the class after it is set.",
                this::setAbstract,
                ClassWithAbstractDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_active_of_class",
                "Set the Active property of the specified class (specified by ID), and return the model element of the class after it is set.",
                this::setActive,
                ClassWithActiveDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_leaf_of_class",
                "Set the Leaf property of the specified class (specified by ID), and return the model element of the class after it is set.",
                this::setLeaf,
                ClassWithLeafDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "add_invariant_to_class",
                "Add an invariant to the specified class (specified by ID), and return the model element of the class after it is added. Invariants are set as constraints.",
                this::addInvariant,
                ClassWithInvariantDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_invariant_from_class",
                "Remove the specified invariant from the specified class (specified by ID), and return the model element of the class after it is removed. Invariants are set as constraints.",
                this::removeInvariant,
                ClassWithInvariantDTO.class,
                ClassDTO.class)
        );
    }

    private ClassDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get class or interface information: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.id());

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO setAbstract(ClassWithAbstractDTO param) throws Exception {
        log.debug("Set abstract of class: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        txnAstah.run( () -> {
            astahClass.setAbstract(param.isAbstract());
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO setActive(ClassWithActiveDTO param) throws Exception {
        log.debug("Set active of class: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        txnAstah.run( () -> {
            astahClass.setActive(param.isActive());
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO setLeaf(ClassWithLeafDTO param) throws Exception {
        log.debug("Set leaf of class: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        txnAstah.run( () -> {
            astahClass.setLeaf(param.isLeaf());
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO addInvariant(ClassWithInvariantDTO param) throws Exception {
        log.debug("Add invariant to class: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        txnAstah.run( () -> {
            basicModelEditor.createConstraint(astahClass, param.invariant());
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO removeInvariant(ClassWithInvariantDTO param) throws Exception {
        log.debug("Remove invariant from class: {}", param);

        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        for (IConstraint constraint : astahClass.getConstraints()) {
            if (param.invariant().equals(constraint.getName())) {
                txnAstah.run( () -> {
                    basicModelEditor.delete(constraint);
                });

                break;
            }
        }

        return ClassDTOAssembler.toDTO(astahClass);
    }
}
