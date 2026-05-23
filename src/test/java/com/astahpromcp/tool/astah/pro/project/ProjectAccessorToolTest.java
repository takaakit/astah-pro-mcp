package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.FilePathDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.NameDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.BooleanDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.ProjectPathDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
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
    private Method saveProjectAs;
    private Method closeProject;
    private Method getProjectPath;

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
            FilePathDTO.class);

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

        // saveProjectAs() method
        saveProjectAs = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "saveProjectAs",
            McpSyncServerExchange.class,
            FilePathDTO.class);

        // closeProject() method
        closeProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "closeProject",
            McpSyncServerExchange.class,
            NoInputDTO.class);

        // getProjectPath() method
        getProjectPath = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "getProjectPath",
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
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
    void openProject_ok_1() throws Exception {
        // Create input DTO
        // Relative path
        FilePathDTO inputDTO = new FilePathDTO("src/test/resources/modelfile/project/ProjectAccessorToolTest.asta");

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
    void openProject_ok_2() throws Exception {
        // Create input DTO
        // Absolute path
        final String currentDirectory = System.getProperty("user.dir");
        FilePathDTO inputDTO = new FilePathDTO(currentDirectory + "/src/test/resources/modelfile/project/ProjectAccessorToolTest.asta");

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        BooleanDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        BooleanDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        NameIdTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            findNamedElementsByName,
            tool,
            inputDTO,
            NameIdTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(outputDTO.value().size(), 1);
        assertEquals(outputDTO.value().get(0).name(), "Foo");
    }

    @Test
    void saveProject_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call saveProject()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProject,
            tool,
            inputDTO,
            ProjectPathDTO.class));
    }

    @Test
    void saveProject_ng_1() throws Exception {
        // Create a new project
        projectAccessor.create();

        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call saveProject()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProject,
            tool,
            inputDTO,
            ProjectPathDTO.class));
    }

    @Test
    void saveProject_ng_2() throws Exception {
        // Close the project
        projectAccessor.close();

        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call saveProject()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProject,
            tool,
            inputDTO,
            ProjectPathDTO.class));
    }

    @Test
    void saveProjectAs_ok_1() throws Exception {
        // Create input DTO
        // Relative path
        FilePathDTO inputDTO = new FilePathDTO("./ProjectAccessorToolTest_2.asta");

        // Delete the stale file if it exists
        File targetFile = new File(inputDTO.filePath());
        if (targetFile.exists()) {
            assertTrue(targetFile.delete(), "failed to delete stale file: " + targetFile);
        }

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        ProjectPathDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            inputDTO,
            ProjectPathDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(projectAccessor.getProjectPath(), outputDTO.projectPath());

        // Check that the project file exists
        assertTrue(new File(inputDTO.filePath()).exists());

        // Cleanup: Delete the saved project file
        new File(inputDTO.filePath()).delete();
    }

    @Test
    void saveProjectAs_ok_2() throws Exception {
        // Create input DTO
        // Absolute path
        final String currentDirectory = System.getProperty("user.dir");
        FilePathDTO inputDTO = new FilePathDTO(currentDirectory + "/ProjectAccessorToolTest_2.asta");

        // Delete the stale file if it exists
        File targetFile = new File(inputDTO.filePath());
        if (targetFile.exists()) {
            assertTrue(targetFile.delete(), "failed to delete stale file: " + targetFile);
        }

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        ProjectPathDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            inputDTO,
            ProjectPathDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(projectAccessor.getProjectPath(), outputDTO.projectPath());

        // Check that the project file exists
        assertTrue(new File(inputDTO.filePath()).exists());

        // Cleanup: Delete the saved project file
        new File(inputDTO.filePath()).delete();
    }

    @Test
    void saveProjectAs_ng_1() throws Exception {
        // Create input DTO
        // Dummy file path
        FilePathDTO inputDTO = new FilePathDTO("C:/dummy/ProjectAccessorToolTest_2.asta");

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            inputDTO,
            ProjectPathDTO.class));
    }

    @Test
    void saveProjectAs_ng_2() throws Exception {
        // Create input DTO
        // Incorrect file path
        FilePathDTO inputDTO = new FilePathDTO("./ProjectAccessorToolTest_2.txt");

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            inputDTO,
            ProjectPathDTO.class));
    }

    @Test
    void saveProjectAs_ng_3() throws Exception {
        // Create input DTO
        FilePathDTO inputDTO = new FilePathDTO("./ProjectAccessorToolTest_2.asta");

        // Create the file
        File targetFile = new File(inputDTO.filePath());
        assertTrue(targetFile.createNewFile(), "failed to create file: " + targetFile);

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            inputDTO,
            ProjectPathDTO.class));

        // Cleanup: Delete the created file
        targetFile.delete();
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
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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

    @Test
    void getProjectPath_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getProjectPath()
        // ----------------------------------------
        ProjectPathDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getProjectPath,
            tool,
            inputDTO,
            ProjectPathDTO.class);
            
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(outputDTO.projectPath(), projectAccessor.getProjectPath());
    }
}
