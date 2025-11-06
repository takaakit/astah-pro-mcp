package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.NameDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.BooleanDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectAccessorToolTest {

    private ProjectAccessor projectAccessor;
    private ProjectAccessorTool tool;
    private Method createProject;
    private Method openProject;
    private Method getProject;
    private Method isProjectOpen;
    private Method isProjectModified;
    private Method findNamedElementsByName;
    private Method saveProject;
    private Method closeProject;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/project/ProjectAccessorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ProjectAccessorTool(
            projectAccessor,
            astahProToolSupport,
            true);

        // createProject() method
        createProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "createProject",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // openProject() method
        openProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "openProject",
            McpSyncServerExchange.class,
            NameDTO.class);

        // getProject() method
        getProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "getProject",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // isProjectOpen() method
        isProjectOpen = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "isProjectOpen",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // isProjectModified() method
        isProjectModified = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "isProjectModified",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // findNamedElementsByName() method
        findNamedElementsByName = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "findNamedElementsByName",
            McpSyncServerExchange.class,
            NameDTO.class);

        // saveProject() method
        saveProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "saveProject",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // closeProject() method
        closeProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "closeProject",
            McpSyncServerExchange.class,
            NoInputDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void createProject_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call createProject()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createProject,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check that a new project was created
        assertNotNull(projectAccessor.getProject());
    }

    @Test
    void openProject_ok() throws Exception {
        // Create input DTO
        NameDTO inputDTO = new NameDTO("src/test/resources/modelfile/project/ProjectAccessorToolTest.asta");

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            openProject,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(outputDTO.name(), projectAccessor.getProject().getName());
        
        // Check that the project was opened
        assertNotNull(projectAccessor.getProject());
    }

    @Test
    void getProject_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getProject()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getProject,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(outputDTO.name(), projectAccessor.getProject().getName());
    }

    @Test
    void isProjectOpen_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call isProjectOpen()
        // ----------------------------------------
        BooleanDTO outputDTO = TestSupport.instance().invokeToolMethod(
            isProjectOpen,
            tool,
            inputDTO,
            BooleanDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(true, outputDTO.value());

        // Close project
        projectAccessor.close();

        // ----------------------------------------
        // Call isProjectOpen()
        // ----------------------------------------
        outputDTO = TestSupport.instance().invokeToolMethod(
            isProjectOpen,
            tool,
            inputDTO,
            BooleanDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(false, outputDTO.value());
    }

    @Test
    void isProjectModified_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call isProjectModified()
        // ----------------------------------------
        BooleanDTO outputDTO = TestSupport.instance().invokeToolMethod(
            isProjectModified,
            tool,
            inputDTO,
            BooleanDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(false, outputDTO.value());

        // Modify project
        projectAccessor.getProject().setName("New Name");

        // ----------------------------------------
        // Call isProjectModified()
        // ----------------------------------------
        outputDTO = TestSupport.instance().invokeToolMethod(
            isProjectModified,
            tool,
            inputDTO,
            BooleanDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(true, outputDTO.value());
    }

    @Test
    void findNamedElementsByName_ok() throws Exception {
        // Create input DTO
        NameDTO inputDTO = new NameDTO("Foo");

        // ----------------------------------------
        // Call findNamedElementsByName()
        // ----------------------------------------
        NameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethod(
            findNamedElementsByName,
            tool,
            inputDTO,
            NameIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(outputDTO.value().size(), 1);
        assertEquals(outputDTO.value().get(0).name(), "Foo");
    }

    @Disabled
    @Test
    void saveProject_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call saveProject()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            saveProject,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(outputDTO.name(), projectAccessor.getProject().getName());
    }

    @Test
    void closeProject_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // Get project name before closing
        String projectName = projectAccessor.getProject().getName();

        // ----------------------------------------
        // Call closeProject()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            closeProject,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(outputDTO.name(), projectName);
        
        // Check that the project was closed
        assertThrows(Exception.class, () -> projectAccessor.getProject());
    }
}
