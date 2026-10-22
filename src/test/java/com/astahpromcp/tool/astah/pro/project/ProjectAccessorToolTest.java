package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.FilePathDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.NameDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.BooleanDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.project.outputdto.ProjectPathDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.ProjectLockedException;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        projectAccessor.open("src/test/resources/modelfile/project/ProjectAccessorToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ProjectAccessorTool(
            projectAccessor,
            astahProToolSupport);

        // createProject() method
        createProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "createProject",
            NoInputDTO.class);

        // openProject() method
        openProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "openProject",
            FilePathDTO.class);

        // getProject() method
        getProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "getProject",
            NoInputDTO.class);

        // isProjectOpen() method
        isProjectOpen = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "isProjectOpen",
            NoInputDTO.class);

        // isProjectModified() method
        isProjectModified = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "isProjectModified",
            NoInputDTO.class);

        // findNamedElementsByName() method
        findNamedElementsByName = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "findNamedElementsByName",
            NameDTO.class);

        // saveProject() method
        saveProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "saveProject",
            NoInputDTO.class);

        // saveProjectAs() method
        saveProjectAs = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "saveProjectAs",
            FilePathDTO.class);

        // closeProject() method
        closeProject = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "closeProject",
            NoInputDTO.class);

        // getProjectPath() method
        getProjectPath = TestSupport.getAccessibleMethod(
            ProjectAccessorTool.class,
            "getProjectPath",
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
    void openProject_ng_relativePath() throws Exception {
        String projectPath = projectAccessor.getProjectPath();
        String relativePath = "src/test/resources/modelfile/project/ProjectAccessorToolTest.asta";

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            openProject,
            tool,
            new FilePathDTO(relativePath),
            NamedElementDTO.class));

        // Astah would resolve it against its own folder, not the agent's, so it is rejected even though the file exists here
        assertEquals("The specified file path is not an absolute path: " + relativePath + ". Specify the absolute path, e.g., C:\\work\\model.asta.",
            exception.getCause().getMessage());
        assertEquals(projectPath, projectAccessor.getProjectPath());
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
        assertEquals(projectAccessor.getProject().getName(), outputDTO.name());

        // Check that the project was opened
        assertNotNull(projectAccessor.getProject());
    }

    @Test
    void openProject_ng_fileNotFound() throws Exception {
        String projectPath = projectAccessor.getProjectPath();

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            openProject,
            tool,
            new FilePathDTO(Path.of("src/test/resources/modelfile/project/NotExisting.asta").toAbsolutePath().toString()),
            NamedElementDTO.class));

        // Check that the current project is kept open
        assertTrue(exception.getCause().getMessage().contains("does not exist"));
        assertEquals(projectPath, projectAccessor.getProjectPath());
    }

    @Test
    void createProject_ng_modifiedUntitledProject() throws Exception {
        // Make an untitled project with unsaved changes
        makeModifiedUntitledProject();

        // ----------------------------------------
        // Call createProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            createProject,
            tool,
            new NoInputDTO(),
            NamedElementDTO.class));

        // Check that the unsaved changes were kept
        assertTrue(exception.getCause().getMessage().contains("untitled"));
        assertUnsavedChangesKept("no_title");
    }

    @Test
    void createProject_ng_modifiedSavedProject() throws Exception {
        // Make the saved project modified
        String projectPath = projectAccessor.getProjectPath();
        projectAccessor.getProject().setName(UNSAVED_WORK_NAME);

        // ----------------------------------------
        // Call createProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            createProject,
            tool,
            new NoInputDTO(),
            NamedElementDTO.class));

        // Check that the unsaved changes were kept
        assertTrue(exception.getCause().getMessage().contains("unsaved changes"));
        assertUnsavedChangesKept(projectPath);
    }

    @Test
    void openProject_ng_modifiedUntitledProject() throws Exception {
        // Make an untitled project with unsaved changes
        makeModifiedUntitledProject();

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            openProject,
            tool,
            new FilePathDTO("src/test/resources/modelfile/project/ProjectAccessorToolTest.asta"),
            NamedElementDTO.class));

        // Check that the unsaved changes were kept
        assertTrue(exception.getCause().getMessage().contains("untitled"));
        assertUnsavedChangesKept("no_title");
    }

    @Test
    void openProject_ng_modifiedSavedProject() throws Exception {
        // Make the saved project modified
        String projectPath = projectAccessor.getProjectPath();
        projectAccessor.getProject().setName(UNSAVED_WORK_NAME);

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            openProject,
            tool,
            new FilePathDTO("src/test/resources/modelfile/project/ProjectAccessorToolTest.asta"),
            NamedElementDTO.class));

        // Check that the unsaved changes were kept
        assertTrue(exception.getCause().getMessage().contains("unsaved changes"));
        assertUnsavedChangesKept(projectPath);
    }

    private static final String UNSAVED_WORK_NAME = "Unsaved Work";

    private void makeModifiedUntitledProject() throws Exception {
        projectAccessor.close();
        projectAccessor.create();
        projectAccessor.getProject().setName(UNSAVED_WORK_NAME);
        assertEquals("no_title", projectAccessor.getProjectPath());
        assertTrue(projectAccessor.isProjectModified());
    }

    private void assertUnsavedChangesKept(String expectedProjectPath) throws Exception {
        assertEquals(expectedProjectPath, projectAccessor.getProjectPath());
        assertTrue(projectAccessor.isProjectModified());
        assertEquals(UNSAVED_WORK_NAME, projectAccessor.getProject().getName());
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
        assertEquals(projectAccessor.getProject().getName(), outputDTO.name());
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
        assertTrue(outputDTO.value());

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
        assertFalse(outputDTO.value());
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
        assertFalse(outputDTO.value());

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
        assertTrue(outputDTO.value());
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
        assertEquals(1, outputDTO.value().size());
        assertEquals("Foo", outputDTO.value().get(0).name());
    }

    @Test
    void findNamedElementsByName_ok_primitiveType() throws Exception {
        // Create input DTO
        NameDTO inputDTO = new NameDTO("int");

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
        assertTrue(outputDTO.value().stream().anyMatch(nameIdType ->
            "int".equals(nameIdType.name()) && "PrimitiveType".equals(nameIdType.type())));
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
    void saveProjectAs_ng_relativePath() throws Exception {
        String projectPath = projectAccessor.getProjectPath();

        // A file name alone, and a path with a folder
        for (String relativePath : new String[] {"ProjectAccessorToolTest_2.asta", "./ProjectAccessorToolTest_2.asta"}) {
            // ----------------------------------------
            // Call saveProjectAs()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
                saveProjectAs,
                tool,
                new FilePathDTO(relativePath),
                ProjectPathDTO.class));

            // Astah would resolve it against its own folder (usually its install folder), not the agent's
            assertEquals("The specified file path is not an absolute path: " + relativePath + ". Specify the absolute path, e.g., C:\\work\\model.asta.",
                exception.getCause().getMessage());
            assertFalse(new File(relativePath).exists());
            assertEquals(projectPath, projectAccessor.getProjectPath());
        }
    }

    @Test
    void saveProjectAs_ok_upperCaseExtension(@TempDir Path workspace) throws Exception {
        Path target = workspace.resolve("UpperCase.ASTA");

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        ProjectPathDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            new FilePathDTO(target.toString()),
            ProjectPathDTO.class);

        // Astah keeps the extension as written rather than appending .asta to it
        assertEquals(projectAccessor.getProjectPath(), outputDTO.projectPath());
        assertEquals("UpperCase.ASTA", target.toRealPath().getFileName().toString());
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
    void saveProjectAs_ng_2(@TempDir Path workspace) throws Exception {
        // Create input DTO
        // Incorrect file path
        FilePathDTO inputDTO = new FilePathDTO(workspace.resolve("ProjectAccessorToolTest_2.txt").toString());

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            inputDTO,
            ProjectPathDTO.class));

        assertTrue(exception.getCause().getMessage().contains("not a valid Astah project file"));
    }

    @Test
    void saveProjectAs_ng_3(@TempDir Path workspace) throws Exception {
        // Create input DTO
        FilePathDTO inputDTO = new FilePathDTO(workspace.resolve("ProjectAccessorToolTest_2.asta").toString());

        // Create the file
        File targetFile = new File(inputDTO.filePath());
        assertTrue(targetFile.createNewFile(), "failed to create file: " + targetFile);

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            tool,
            inputDTO,
            ProjectPathDTO.class));

        assertTrue(exception.getCause().getMessage().contains("This tool does not overwrite an existing file"));
        assertEquals(0, targetFile.length());
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
        assertEquals(projectName, outputDTO.name());

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
        assertEquals(projectAccessor.getProjectPath(), outputDTO.projectPath());
    }

    @Test
    void getProjectPath_ok_returnsEmptyStringForUnsavedProject() throws Exception {
        projectAccessor.close();
        projectAccessor.create();

        ProjectPathDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getProjectPath,
            tool,
            new NoInputDTO(),
            ProjectPathDTO.class);

        assertNotNull(outputDTO);
        assertEquals("", outputDTO.projectPath());
    }

    @Test
    void getProjectPath_ng_returnsEmptyStringWhenNoProjectIsOpen() throws Exception {
        projectAccessor.close();

        ProjectPathDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getProjectPath,
            tool,
            new NoInputDTO(),
            ProjectPathDTO.class);

        assertNotNull(outputDTO);
        assertEquals("", outputDTO.projectPath());
    }

    @Test
    void openProject_ng_fileIsNotAnAstahProject(@TempDir Path workspace) throws Exception {
        Path notAProject = Files.writeString(workspace.resolve("NotAProject.asta"), "This is not an Astah project.");

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            openProject,
            tool,
            new FilePathDTO(notAProject.toString()),
            NamedElementDTO.class));

        // The offline API throws a StreamCorruptedException ("invalid stream header: ..."), which must be kept as the cause
        Throwable failure = exception.getCause();
        assertEquals("Failed to open project: " + notAProject + ". java.io.StreamCorruptedException: invalid stream header: 54686973", failure.getMessage());
        assertInstanceOf(StreamCorruptedException.class, failure.getCause(), "The exception from Astah must be kept as the cause");
    }

    @Test
    void openProject_ng_astahOpenedNothingWithoutThrowing(@TempDir Path workspace) throws Exception {
        Path notAProject = Files.writeString(workspace.resolve("NotAProject.asta"), "This is not an Astah project.");
        AtomicBoolean projectIsOpen = new AtomicBoolean(true);
        ProjectAccessor guiLikeAccessor = mock(ProjectAccessor.class);
        when(guiLikeAccessor.hasProject()).thenAnswer(invocation -> projectIsOpen.get());
        when(guiLikeAccessor.getProjectPath()).thenReturn("C:\\work\\Previous.asta");
        doAnswer(invocation -> {
            projectIsOpen.set(false);
            return null;
        }).when(guiLikeAccessor).open(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
        ProjectAccessorTool guiLikeTool = new ProjectAccessorTool(guiLikeAccessor, new AstahProToolSupport(guiLikeAccessor));

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            openProject,
            guiLikeTool,
            new FilePathDTO(notAProject.toString()),
            NamedElementDTO.class));

        // Without the check, getProject() would have failed next with a message that does not say the open failed
        assertEquals("Failed to open project: " + notAProject + ". Astah opened no project from it, and its log ('astahLog' of 'get_info_of_logs') has the reason.",
            exception.getCause().getMessage());
    }

    @Test
    void openProject_ng_astahLeftAnotherProjectOpen(@TempDir Path workspace) throws Exception {
        Path requested = Files.writeString(workspace.resolve("Requested.asta"), "This is not an Astah project.");
        ProjectAccessor guiLikeAccessor = mock(ProjectAccessor.class);
        when(guiLikeAccessor.hasProject()).thenReturn(true);
        when(guiLikeAccessor.getProjectPath()).thenReturn("C:\\work\\Previous.asta");
        ProjectAccessorTool guiLikeTool = new ProjectAccessorTool(guiLikeAccessor, new AstahProToolSupport(guiLikeAccessor));

        // ----------------------------------------
        // Call openProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            openProject,
            guiLikeTool,
            new FilePathDTO(requested.toString()),
            NamedElementDTO.class));

        // Without the check, the project left open would have been returned as the one opened
        assertEquals("Failed to open project: " + requested + ". Astah opened no project from it, and its log ('astahLog' of 'get_info_of_logs') has the reason.",
            exception.getCause().getMessage());
    }

    @Test
    void saveProject_ok_savesAModifiedProject(@TempDir Path workspace) throws Exception {
        Path project = openModifiedCopy(workspace, "Modified.asta");

        // ----------------------------------------
        // Call saveProject()
        // ----------------------------------------
        ProjectPathDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            saveProject,
            tool,
            new NoInputDTO(),
            ProjectPathDTO.class);

        // The check after the save must not mistake a save that worked for one that did not
        assertEquals(projectAccessor.getProjectPath(), outputDTO.projectPath());
        assertFalse(projectAccessor.isProjectModified());

        // Read back from the file
        projectAccessor.close();
        projectAccessor.open(project.toString());
        assertEquals(UNSAVED_WORK_DEFINITION, projectAccessor.getProject().getDefinition());
    }

    @Test
    void saveProject_ng_astahSavedNothingWithoutThrowing(@TempDir Path workspace) throws Exception {
        Path project = Files.writeString(workspace.resolve("Unsaved.asta"), "The file Astah failed to write.");
        ProjectAccessor guiLikeAccessor = mock(ProjectAccessor.class);
        when(guiLikeAccessor.hasProject()).thenReturn(true);
        when(guiLikeAccessor.isProjectModified()).thenReturn(true);
        when(guiLikeAccessor.getProjectPath()).thenReturn(project.toString());
        ProjectAccessorTool guiLikeTool = new ProjectAccessorTool(guiLikeAccessor, new AstahProToolSupport(guiLikeAccessor));

        // ----------------------------------------
        // Call saveProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProject,
            guiLikeTool,
            new NoInputDTO(),
            ProjectPathDTO.class));

        // Reported as a success, the failure would have let the agent tell the user that the work was saved
        assertEquals("Failed to save the current project. It still has unsaved changes, so the file was not written. The file may be read-only or locked.",
            exception.getCause().getMessage());
    }

    @Test
    void saveProjectAs_ng_astahSavedNothingWithoutThrowing(@TempDir Path workspace) throws Exception {
        Path target = workspace.resolve("SavedAs.asta");
        ProjectAccessor guiLikeAccessor = mock(ProjectAccessor.class);
        when(guiLikeAccessor.hasProject()).thenReturn(true);
        when(guiLikeAccessor.isProjectModified()).thenReturn(false);
        when(guiLikeAccessor.getProjectPath()).thenReturn("C:\\work\\Previous.asta");
        ProjectAccessorTool guiLikeTool = new ProjectAccessorTool(guiLikeAccessor, new AstahProToolSupport(guiLikeAccessor));

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            guiLikeTool,
            new FilePathDTO(target.toString()),
            ProjectPathDTO.class));

        // The project had no unsaved changes before, so only the missing file and the unchanged path tell that nothing was saved
        assertEquals("Failed to save the current project as: " + target + ". Astah did not write the file.",
            exception.getCause().getMessage());
    }

    @Test
    void saveProject_ng_readOnlyFile(@TempDir Path workspace) throws Exception {
        Path project = openModifiedCopy(workspace, "ReadOnly.asta");
        String projectPath = projectAccessor.getProjectPath();
        byte[] contentBefore = Files.readAllBytes(project);
        assertTrue(project.toFile().setReadOnly());

        try {
            // ----------------------------------------
            // Call saveProject()
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
                saveProject,
                tool,
                new NoInputDTO(),
                ProjectPathDTO.class));

            // Without the check, Astah would open its save dialog and leave the agent waiting for the user
            assertEquals("Failed to save the current project because its file is not writable: " + projectPath + ". The file may be read-only, or you may have no permission to write to it (e.g., a file under Program Files). Save it to a writable folder with 'save_proj_as' instead.",
                exception.getCause().getMessage());
            assertTrue(projectAccessor.isProjectModified());
            assertArrayEquals(contentBefore, Files.readAllBytes(project));
        } finally {
            // So that the temporary folder can be deleted
            project.toFile().setWritable(true);
        }
    }

    @Test
    void saveProject_ng_astahThrowsWithoutMessage(@TempDir Path workspace) throws Exception {
        Path project = Files.writeString(workspace.resolve("Unwritable.asta"), "The file Astah failed to write.");
        ProjectAccessor guiLikeAccessor = mock(ProjectAccessor.class);
        when(guiLikeAccessor.hasProject()).thenReturn(true);
        when(guiLikeAccessor.isProjectModified()).thenReturn(true);
        when(guiLikeAccessor.getProjectPath()).thenReturn(project.toString());
        doThrow(new IOException()).when(guiLikeAccessor).save();
        ProjectAccessorTool guiLikeTool = new ProjectAccessorTool(guiLikeAccessor, new AstahProToolSupport(guiLikeAccessor));

        // ----------------------------------------
        // Call saveProject()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProject,
            guiLikeTool,
            new NoInputDTO(),
            ProjectPathDTO.class));

        // Astah throws an IOException with no message for a file it cannot write, so the exception alone tells the agent nothing it can act on
        Throwable failure = exception.getCause();
        assertEquals("Failed to save the current project: " + project + ". java.io.IOException. Astah could not write the file: another program (or another Astah) may have it open, its folder may not be writable, or the disk may be full.",
            failure.getMessage());
        assertInstanceOf(IOException.class, failure.getCause(), "The exception from Astah must be kept as the cause");
    }

    @Test
    void saveProjectAs_ng_astahThrowsWithoutMessage(@TempDir Path workspace) throws Exception {
        Path target = workspace.resolve("SavedAs.asta");
        ProjectAccessor guiLikeAccessor = mock(ProjectAccessor.class);
        when(guiLikeAccessor.hasProject()).thenReturn(true);
        when(guiLikeAccessor.getProjectPath()).thenReturn("C:\\work\\Previous.asta");
        doThrow(new ProjectLockedException()).when(guiLikeAccessor).saveAs(anyString());
        ProjectAccessorTool guiLikeTool = new ProjectAccessorTool(guiLikeAccessor, new AstahProToolSupport(guiLikeAccessor));

        // ----------------------------------------
        // Call saveProjectAs()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            saveProjectAs,
            guiLikeTool,
            new FilePathDTO(target.toString()),
            ProjectPathDTO.class));

        // Astah throws a ProjectLockedException with no message for a folder it cannot write to, as if another program had the file open
        Throwable failure = exception.getCause();
        assertEquals("Failed to save the current project as: " + target + ". com.change_vision.jude.api.inf.exception.ProjectLockedException. Astah could not write the file: another program (or another Astah) may have it open, its folder may not be writable, or the disk may be full.",
            failure.getMessage());
        assertInstanceOf(ProjectLockedException.class, failure.getCause(), "The exception from Astah must be kept as the cause");
    }

    private static final String UNSAVED_WORK_DEFINITION = "Unsaved work";

    // A copy of the test project, opened and given a change to save. The definition is changed rather than the name, which Astah takes from the file name.
    private Path openModifiedCopy(Path workspace, String fileName) throws Exception {
        Path project = Files.copy(Path.of("src/test/resources/modelfile/project/ProjectAccessorToolTest.asta"), workspace.resolve(fileName));
        projectAccessor.close();
        projectAccessor.open(project.toString());
        new TransactionSupport(projectAccessor.getTransactionManager()).run(() -> projectAccessor.getProject().setDefinition(UNSAVED_WORK_DEFINITION));
        assertTrue(projectAccessor.isProjectModified());
        return project;
    }
}
