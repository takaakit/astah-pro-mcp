package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.FilePathDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.NameDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.BooleanDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.NamedElementDTOAssembler;
import com.astahpromcp.tool.astah.pro.project.outputdto.ProjectPathDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.exception.ProjectLockedException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/project/ProjectAccessor.html
@Slf4j
public class ProjectAccessorTool extends AstahToolProvider {

    private static final String UNSAVED_PROJECT_PATH = "no_title";

    private final ProjectAccessor projectAccessor;
    private final AstahProToolSupport astahProToolSupport;

    public ProjectAccessorTool(ProjectAccessor projectAccessor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_proj",
                "Return model element information of the named element for the project (root package).",
                this::getProject,
                NoInputDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "is_proj_opened",
                "Return whether a project is opened or not.",
                this::isProjectOpen,
                NoInputDTO.class,
                BooleanDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "is_proj_modified",
                "Return whether the current project is modified or not.",
                this::isProjectModified,
                NoInputDTO.class,
                BooleanDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "find_named_elements_by_name",
                "Search named elements in the project by partial (substring) match on the element name, case-insensitively: searching \"Book\" also returns \"BookCopy\". Note that presentations won't be searched. The primitive types predefined in the project (e.g., int) are searched as well, and are returned with the element type \"PrimitiveType\".",
                this::findNamedElementsByName,
                NameDTO.class,
                NameIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_proj_path",
                "Return the full path of the Astah project file (e.g., /path/to/project.asta). If the project has not been saved, return an empty string. For example, use this tool when you want to derive a relative path from an absolute path based on the Astah project file location.",
                this::getProjectPath,
                NoInputDTO.class,
                ProjectPathDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "create_proj",
                "Create an Astah project (root package), and return the model element of the named element for the project. The project element is the root package.",
                this::createProject,
                NoInputDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "open_proj",
                "Open the specified project (specified by the full path of the Astah project file), and return the model element of the named element for the project. The project element is the root package.",
                this::openProject,
                FilePathDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "save_proj",
                "Save the current project, and return the full path of the Astah project file (e.g., /path/to/project.asta). Note: Save the project using this tool only when the user explicitly instructs you to do so, or when explicitly instructed in Agent Harness.",
                this::saveProject,
                NoInputDTO.class,
                ProjectPathDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "save_proj_as",
                "Save the current project with a new name, and return the full path of the Astah project file (e.g., /path/to/project.asta). Note: Save the project using this tool only when the user explicitly instructs you to do so, or when explicitly instructed in Agent Harness.",
                this::saveProjectAs,
                FilePathDTO.class,
                ProjectPathDTO.class)

            /* Closing the project should be performed based on the user's decision.
               So these tool functions are disabled.

            ToolSupport.definition(
                "close_proj",
                "Close the current project, and return the current project information.",
                this::closeProject,
                NoInputDTO.class,
                NamedElementDTO.class)
            */
        );
    }

    private NamedElementDTO createProject(NoInputDTO param) throws Exception {
        ensureNoUnsavedChanges();

        try {
            projectAccessor.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create project (root package).", e);
        }

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.", e);
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private NamedElementDTO openProject(FilePathDTO param) throws Exception {
        ensureNoUnsavedChanges();
        requireAbsolutePath(param.filePath());

        if (!new File(param.filePath()).isFile()) {
            throw new IllegalArgumentException("The specified project file does not exist: " + param.filePath());
        }

        try {
            projectAccessor.open(
                param.filePath(),
                false,  // isIgnoreModelVersion
                false,  // lockMode
                true);  // allowReadOnly
        } catch (Exception e) {
            throw new RuntimeException("Failed to open project: " + param.filePath() + ". " + e, e);
        }

        if (!isOpenAt(param.filePath())) {
            throw new RuntimeException("Failed to open project: " + param.filePath() + ". Astah opened no project from it, and its log ('get_info_of_astah_log') has the reason.");
        }

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.", e);
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private void ensureNoUnsavedChanges() throws Exception {
        if (!projectAccessor.hasProject() || !projectAccessor.isProjectModified()) {
            return;
        } else if (UNSAVED_PROJECT_PATH.equals(projectAccessor.getProjectPath())) {
            throw new RuntimeException("The current project is untitled and has unsaved changes.");
        } else {
            throw new RuntimeException("The current project has unsaved changes.");
        }
    }

    private NamedElementDTO getProject(NoInputDTO param) throws Exception {
        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get project (root package).", e);
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private BooleanDTO isProjectOpen(NoInputDTO param) throws Exception {
        return new BooleanDTO(projectAccessor.hasProject());
    }

    private BooleanDTO isProjectModified(NoInputDTO param) throws Exception {
        return new BooleanDTO(projectAccessor.isProjectModified());
    }

    private NameIdTypeListDTO findNamedElementsByName(NameDTO param) throws Exception {
        INamedElement[] astahNamedElements = projectAccessor.findElements(INamedElement.class);

        List<NameIdTypeDTO> namedIdTypeDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            if (astahNamedElement.getName().toLowerCase().contains(param.name().toLowerCase())) {
                namedIdTypeDTOs.add(NameIdTypeDTOAssembler.toDTO(astahNamedElement));
            }
        }

        // A primitive type is not a part of the model tree, so findElements() never returns one. Search the primitive types here as well, because a type is what is most often looked up by name.
        for (IClass astahPrimitiveType : astahProToolSupport.getPrimitiveTypes()) {
            if (astahPrimitiveType.getName().toLowerCase().contains(param.name().toLowerCase())) {
                namedIdTypeDTOs.add(NameIdTypeDTOAssembler.toPrimitiveTypeDTO(astahPrimitiveType));
            }
        }

        return new NameIdTypeListDTO(namedIdTypeDTOs);
    }

    private ProjectPathDTO saveProject(NoInputDTO param) throws Exception {
        // Check if the current project is created
        if (!projectAccessor.hasProject()) {
            throw new RuntimeException("The current project is not created.");
        }

        // Check if the current project is modified
        if (!projectAccessor.isProjectModified()) {
            throw new RuntimeException("The current project is not modified.");
        }

        // To prevent the save file path input dialog from appearing, check if the project has been saved.
        String projectPath = projectAccessor.getProjectPath();
        if (projectPath == null || projectPath.isEmpty() || !new File(projectPath).exists()) {
            throw new RuntimeException("The current project has not been saved yet, so specify a file path when saving it.");
        }

        if (!Files.isWritable(Path.of(projectPath))) {
            throw new RuntimeException("Failed to save the current project because its file is not writable: " + projectPath + ". The file may be read-only, or you may have no permission to write to it (e.g., a file under Program Files). Save it to a writable folder with 'save_proj_as' instead.");
        }

        try {
            projectAccessor.save();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save the current project: " + projectPath + ". " + describeWriteFailure(e), e);
        }

        if (projectAccessor.isProjectModified()) {
            throw new RuntimeException("Failed to save the current project. It still has unsaved changes, so the file was not written. The file may be read-only or locked.");
        }

        return new ProjectPathDTO(projectAccessor.getProjectPath());
    }

    private ProjectPathDTO saveProjectAs(FilePathDTO param) throws Exception {
        String filePath = param.filePath();
        requireAbsolutePath(filePath);

        if (!filePath.toLowerCase(Locale.ROOT).endsWith(".asta")) {
            throw new IllegalArgumentException("The specified file path is not a valid Astah project file (*.asta): " + filePath);
        }

        File folder = new File(filePath).getParentFile();
        if (folder == null || !folder.isDirectory()) {
            throw new IllegalArgumentException("The parent folder of the specified file path does not exist: " + filePath);
        }

        if (new File(filePath).exists()) {
            throw new IllegalArgumentException("The specified file path already exists: " + filePath + ". This tool does not overwrite an existing file. To save the current project to its own file, use 'save_proj' instead.");
        }

        if (!Files.isWritable(folder.toPath())) {
            throw new IllegalArgumentException("The parent folder of the specified file path is not writable: " + filePath + ". Specify a file path in a folder you have permission to write to.");
        }

        try {
            projectAccessor.saveAs(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save the current project as: " + filePath + ". " + describeWriteFailure(e), e);
        }

        if (!new File(filePath).isFile() || !isOpenAt(filePath)) {
            throw new RuntimeException("Failed to save the current project as: " + filePath + ". Astah did not write the file.");
        }

        return new ProjectPathDTO(projectAccessor.getProjectPath());
    }

    private static void requireAbsolutePath(String filePath) {
        if (!Path.of(filePath).isAbsolute()) {
            throw new IllegalArgumentException("The specified file path is not an absolute path: " + filePath + ". Specify the absolute path, e.g., C:\\work\\model.asta.");
        }
    }

    private static String describeWriteFailure(Exception e) {
        if (e instanceof IOException || e instanceof ProjectLockedException) {
            return e + ". Astah could not write the file: another program (or another Astah) may have it open, its folder may not be writable, or the disk may be full.";
        }
        return String.valueOf(e);
    }

    // Whether the project open in Astah is the file at the path
    private boolean isOpenAt(String path) throws Exception {
        return projectAccessor.hasProject()
            && new File(projectAccessor.getProjectPath()).getCanonicalFile().equals(new File(path).getCanonicalFile());
    }

    private NamedElementDTO closeProject(NoInputDTO param) throws Exception {
        log.debug("Close project: {}", param);

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.", e);
        }

        try {
            projectAccessor.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close the current project.", e);
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private ProjectPathDTO getProjectPath(NoInputDTO param) throws Exception {
        try {
            String projectPath = projectAccessor.getProjectPath();
            if (projectPath == null
                || projectPath.isBlank()
                || UNSAVED_PROJECT_PATH.equals(projectPath)) {
                return new ProjectPathDTO("");
            }

            return new ProjectPathDTO(projectPath);
            
        } catch (ProjectNotFoundException e) {
            return new ProjectPathDTO("");
        }
    }
}
