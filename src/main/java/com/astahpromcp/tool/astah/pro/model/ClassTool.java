package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.ConditionTextSupport;
import com.astahpromcp.tool.astah.pro.SystemPropertySupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithActiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithInvariantDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithLeafDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ClassDTOAssembler;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IClass.html
@Slf4j
public class ClassTool extends AstahToolProvider {

    private final BasicModelEditor basicModelEditor;
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final SystemPropertySupport systemPropertySupport;

    public ClassTool(BasicModelEditor basicModelEditor, ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, SystemPropertySupport systemPropertySupport) {
        this.basicModelEditor = basicModelEditor;
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.systemPropertySupport = systemPropertySupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_class_info",
                "Return model element information about the specified class or interface (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ClassDTO.class),


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
        IClass astahClass = astahProToolSupport.getClass(param.id());

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO setAbstract(ClassWithAbstractDTO param) throws Exception {
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        txnAstah.run( () -> {
            astahClass.setAbstract(param.isAbstract());
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO setActive(ClassWithActiveDTO param) throws Exception {
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        txnAstah.run( () -> {
            astahClass.setActive(param.isActive());
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO setLeaf(ClassWithLeafDTO param) throws Exception {
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());

        txnAstah.run( () -> {
            astahClass.setLeaf(param.isLeaf());
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO addInvariant(ClassWithInvariantDTO param) throws Exception {
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());
        String invariant = ConditionTextSupport.normalize("invariant", param.invariant());
        ConditionTextSupport.requireAbsent(
            "invariant",
            astahClass.getId(),
            invariant,
            astahClass.getConstraints(),
            IConstraint::getSpecification);

        txnAstah.run( () -> {
            basicModelEditor.createConstraint(astahClass, invariant);
        });

        return ClassDTOAssembler.toDTO(astahClass);
    }

    private ClassDTO removeInvariant(ClassWithInvariantDTO param) throws Exception {
        IClass astahClass = astahProToolSupport.getClass(param.targetClassId());
        String invariant = ConditionTextSupport.normalize("invariant", param.invariant());
        List<IConstraint> constraints = ConditionTextSupport.requirePresent(
            "invariant",
            astahClass.getId(),
            invariant,
            astahClass.getConstraints(),
            IConstraint::getSpecification);
        List<String> constraintIds = constraints.stream().map(IConstraint::getId).toList();

        systemPropertySupport.deleteWithoutConfirmationDialog( () -> {
            txnAstah.run( () -> {
                for (IConstraint constraint : constraints) {
                    basicModelEditor.delete(constraint);
                }
            });
        });

        for (String constraintId : constraintIds) {
            astahProToolSupport.verifyDeleted(constraintId);
        }

        return ClassDTOAssembler.toDTO(astahClass);
    }
}
