package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.*;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.UseCaseModelEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/UseCaseModelEditor.html
@Slf4j
public class UseCaseModelEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final UseCaseModelEditor useCaseModelEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public UseCaseModelEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, UseCaseModelEditor useCaseModelEditor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.useCaseModelEditor = useCaseModelEditor;
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
            log.error("Failed to create usecase model editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_actor",
                        "Create a new actor in the specified package (specified by ID), and return the newly created actor information.",
                        this::createActor,
                        NewActorDTO.class,
                        ClassDTO.class),

                ToolSupport.definition(
                        "create_include",
                        "Create a new include between a usecase (specified by ID) and an included usecase (specified by ID) on the specified usecase diagram (specified by ID), and return the newly created include information.",
                        this::createInclude,
                        NewIncludeDTO.class,
                        IncludeDTO.class),

                ToolSupport.definition(
                        "create_extend",
                        "Create a new extend between a usecase (specified by ID) and an extended usecase (specified by ID) on the specified usecase diagram (specified by ID), and return the newly created extend information.",
                        this::createExtend,
                        NewExtendDTO.class,
                        ExtendDTO.class),

                ToolSupport.definition(
                        "create_extension_point",
                        "Create a new extension point in the specified usecase (specified by ID) on the specified usecase diagram (specified by ID), and return the newly created extension point information.",
                        this::createExtensionPoint,
                        NewExtensionPointDTO.class,
                        NamedElementDTO.class),

                ToolSupport.definition(
                        "create_usecase",
                        "Create a new usecase in the specified package (specified by ID), and return the newly created usecase information.",
                        this::createUseCase,
                        NewUseCaseDTO.class,
                        UseCaseDTO.class)
        );
    }

    private ClassDTO createActor(McpSyncServerExchange exchange, NewActorDTO param) throws Exception {
        log.debug("Create actor: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IClass astahActor = useCaseModelEditor.createActor(
                astahPackage,
                param.newActorName());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(astahActor);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private IncludeDTO createInclude(McpSyncServerExchange exchange, NewIncludeDTO param) throws Exception {
        log.debug("Create include: {}", param);

        IUseCase astahUsecase = astahProToolSupport.getUseCase(param.usecaseId());
        IUseCase astahIncludedUsecase = astahProToolSupport.getUseCase(param.includedUsecaseId());

        try {
            transactionManager.beginTransaction();
            IInclude astahInclude = useCaseModelEditor.createInclude(
                astahUsecase,
                astahIncludedUsecase,
                param.newIncludeName());
            transactionManager.endTransaction();

            return IncludeDTOAssembler.toDTO(astahInclude);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ExtendDTO createExtend(McpSyncServerExchange exchange, NewExtendDTO param) throws Exception {
        log.debug("Create extend: {}", param);

        IUseCase astahUsecase = astahProToolSupport.getUseCase(param.usecaseId());
        IUseCase astahExtendedUsecase = astahProToolSupport.getUseCase(param.extendedUsecaseId());

        try {
            transactionManager.beginTransaction();
            IExtend astahExtend = useCaseModelEditor.createExtend(
                astahUsecase,
                astahExtendedUsecase,
                param.newExtendName());
            transactionManager.endTransaction();

            return ExtendDTOAssembler.toDTO(astahExtend);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NamedElementDTO createExtensionPoint(McpSyncServerExchange exchange, NewExtensionPointDTO param) throws Exception {
        log.debug("Create extension point: {}", param);

        IUseCase astahUsecase = astahProToolSupport.getUseCase(param.targetUsecaseId());

        try {
            transactionManager.beginTransaction();
            IExtentionPoint astahExtensionPoint = useCaseModelEditor.createExtensionPoint(
                astahUsecase,
                param.newExtensionPointName());
            transactionManager.endTransaction();

            return NamedElementDTOAssembler.toDTO(astahExtensionPoint);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private UseCaseDTO createUseCase(McpSyncServerExchange exchange, NewUseCaseDTO param) throws Exception {
        log.debug("Create usecase: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IUseCase astahUseCase = useCaseModelEditor.createUseCase(
                astahPackage,
                param.newUsecaseName());
            transactionManager.endTransaction();

            return UseCaseDTOAssembler.toDTO(astahUseCase);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
