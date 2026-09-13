package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.*;
import com.change_vision.jude.api.inf.editor.UseCaseModelEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/UseCaseModelEditor.html
@Slf4j
public class UseCaseModelEditorTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final UseCaseModelEditor useCaseModelEditor;
    private final AstahProToolSupport astahProToolSupport;

    public UseCaseModelEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, UseCaseModelEditor useCaseModelEditor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.useCaseModelEditor = useCaseModelEditor;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
                ToolSupport.toolDefinitionReturningDto(
                        "create_actor",
                        "Create a new actor in the specified package (specified by ID), and return the newly created model element of the actor.",
                        this::createActor,
                        NewActorDTO.class,
                        ClassDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                        "create_include",
                        "Create a new include between a usecase (specified by ID) and an included usecase (specified by ID) on the specified usecase diagram (specified by ID), and return the newly created model element of the include. Set the name to an empty string when the include has no name.",
                        this::createInclude,
                        NewIncludeDTO.class,
                        IncludeDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                        "create_extend",
                        "Create a new extend between a usecase (specified by ID) and an extended usecase (specified by ID) on the specified usecase diagram (specified by ID), and return the newly created model element of the extend. Set the name to an empty string when the extend has no name.",
                        this::createExtend,
                        NewExtendDTO.class,
                        ExtendDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                        "create_extension_point",
                        "Create a new extension point in the specified usecase (specified by ID) on the specified usecase diagram (specified by ID), and return the newly created model element of the extension point.",
                        this::createExtensionPoint,
                        NewExtensionPointDTO.class,
                        NamedElementDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                        "create_usecase",
                        "Create a new usecase in the specified package (specified by ID), and return the newly created model element of the usecase.",
                        this::createUseCase,
                        NewUseCaseDTO.class,
                UseCaseDTO.class)
        );
    }

    private ClassDTO createActor(NewActorDTO param) throws Exception {
        log.debug("Create actor: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IClass astahActor = txnAstah.call( () -> {
            return useCaseModelEditor.createActor(
                    astahPackage,
                    param.newActorName());
        });

        return ClassDTOAssembler.toDTO(astahActor);
    }

    private IncludeDTO createInclude(NewIncludeDTO param) throws Exception {
        log.debug("Create include: {}", param);

        IUseCase astahUsecase = astahProToolSupport.getUseCase(param.includingUsecaseId());
        IUseCase astahIncludedUsecase = astahProToolSupport.getUseCase(param.includedUsecaseId());

        IInclude astahInclude = txnAstah.call( () -> {
            return useCaseModelEditor.createInclude(
                    astahUsecase,
                    astahIncludedUsecase,
                    param.newIncludeName());
        });

        return IncludeDTOAssembler.toDTO(astahInclude);
    }

    private ExtendDTO createExtend(NewExtendDTO param) throws Exception {
        log.debug("Create extend: {}", param);

        IUseCase astahUsecase = astahProToolSupport.getUseCase(param.extendingUsecaseId());
        IUseCase astahExtendedUsecase = astahProToolSupport.getUseCase(param.extendedUsecaseId());

        IExtend astahExtend = txnAstah.call( () -> {
            return useCaseModelEditor.createExtend(
                    astahUsecase,
                    astahExtendedUsecase,
                    param.newExtendName());
        });

        return ExtendDTOAssembler.toDTO(astahExtend);
    }

    private NamedElementDTO createExtensionPoint(NewExtensionPointDTO param) throws Exception {
        log.debug("Create extension point: {}", param);

        IUseCase astahUsecase = astahProToolSupport.getUseCase(param.targetUsecaseId());

        IExtentionPoint astahExtensionPoint = txnAstah.call( () -> {
            return useCaseModelEditor.createExtensionPoint(
                    astahUsecase,
                    param.newExtensionPointName());
        });

        return NamedElementDTOAssembler.toDTO(astahExtensionPoint);
    }

    private UseCaseDTO createUseCase(NewUseCaseDTO param) throws Exception {
        log.debug("Create usecase: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IUseCase astahUseCase = txnAstah.call( () -> {
            return useCaseModelEditor.createUseCase(
                    astahPackage,
                    param.newUsecaseName());
        });

        return UseCaseDTOAssembler.toDTO(astahUseCase);
    }
}
