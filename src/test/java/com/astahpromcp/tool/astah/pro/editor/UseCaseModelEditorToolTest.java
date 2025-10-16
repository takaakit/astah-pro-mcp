package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.UseCaseModelEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IUseCase;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UseCaseModelEditorToolTest {

    private ProjectAccessor projectAccessor;
    private UseCaseModelEditorTool tool;
    private Method createActor;
    private Method createUseCase;
    private Method createInclude;
    private Method createExtend;
    private Method createExtensionPoint;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        UseCaseModelEditor useCaseModelEditor = projectAccessor.getModelEditorFactory().getUseCaseModelEditor();
        projectAccessor.open("src/test/resources/modelfile/editor/UseCaseModelEditorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new UseCaseModelEditorTool(
            projectAccessor,
            transactionManager,
            useCaseModelEditor,
            astahProToolSupport);

        // createActor() method
        createActor = TestSupport.getAccessibleMethod(
            UseCaseModelEditorTool.class,
            "createActor",
            McpSyncServerExchange.class,
            NewActorDTO.class);

        // createUseCase() method
        createUseCase = TestSupport.getAccessibleMethod(
            UseCaseModelEditorTool.class,
            "createUseCase",
            McpSyncServerExchange.class,
            NewUseCaseDTO.class);

        // createInclude() method
        createInclude = TestSupport.getAccessibleMethod(
            UseCaseModelEditorTool.class,
            "createInclude",
            McpSyncServerExchange.class,
            NewIncludeDTO.class);

        // createExtend() method
        createExtend = TestSupport.getAccessibleMethod(
            UseCaseModelEditorTool.class,
            "createExtend",
            McpSyncServerExchange.class,
            NewExtendDTO.class);

        // createExtensionPoint() method
        createExtensionPoint = TestSupport.getAccessibleMethod(
            UseCaseModelEditorTool.class,
            "createExtensionPoint",
            McpSyncServerExchange.class,
            NewExtensionPointDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createActor_ok() throws Exception {
        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewActorDTO inputDTO = new NewActorDTO(
            astahPackage.getId(),
            "Test Actor"
        );

        // ----------------------------------------
        // Call createActor()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createActor,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createUseCase_ok() throws Exception {
        // Get package
        IPackage astahPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewUseCaseDTO inputDTO = new NewUseCaseDTO(
            astahPackage.getId(),
            "Test Use Case"
        );

        // ----------------------------------------
        // Call createUseCase()
        // ----------------------------------------
        UseCaseDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createUseCase,
            tool,
            inputDTO,
            UseCaseDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInclude_ok() throws Exception {
        // Get use cases
        IUseCase astahUseCase = (IUseCase) TestSupport.instance().getNamedElement(
            IUseCase.class,
            "UseCase0");
        IUseCase astahIncludedUseCase = (IUseCase) TestSupport.instance().getNamedElement(
            IUseCase.class,
            "UseCase1");
        
        // Create input DTO
        NewIncludeDTO inputDTO = new NewIncludeDTO(
            astahUseCase.getId(),
            astahIncludedUseCase.getId(),
            "Test Include");

        // ----------------------------------------
        // Call createInclude()
        // ----------------------------------------
        IncludeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInclude,
            tool,
            inputDTO,
            IncludeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createExtend_ok() throws Exception {
        // Get use cases
        IUseCase astahUseCase = (IUseCase) TestSupport.instance().getNamedElement(
            IUseCase.class,
            "UseCase0");
        IUseCase astahExtendedUseCase = (IUseCase) TestSupport.instance().getNamedElement(
            IUseCase.class,
            "UseCase1");
        
        // Create input DTO
        NewExtendDTO inputDTO = new NewExtendDTO(
            astahUseCase.getId(),
            astahExtendedUseCase.getId(),
            "Test Extend");

        // ----------------------------------------
        // Call createExtend()
        // ----------------------------------------
        ExtendDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createExtend,
            tool,
            inputDTO,
            ExtendDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createExtensionPoint_ok() throws Exception {
        // Get use case
        IUseCase astahUseCase = (IUseCase) TestSupport.instance().getNamedElement(
            IUseCase.class,
            "UseCase0");
        
        // Create input DTO
        NewExtensionPointDTO inputDTO = new NewExtensionPointDTO(
            astahUseCase.getId(),
            "Test Extension Point");

        // ----------------------------------------
        // Call createExtensionPoint()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createExtensionPoint,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
