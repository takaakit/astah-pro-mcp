package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
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
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/project/ProjectAccessor.html
@Slf4j
public class ProjectAccessorTool implements ToolProvider {

    private static final String UNSAVED_PROJECT_PATH = "no_title";

    private final ProjectAccessor projectAccessor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ProjectAccessorTool(ProjectAccessor projectAccessor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
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
            log.error("Failed to create project accessor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
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
                "Search named elements in the project by partially matching the element name. Search names are case-insensitive. Note that presentations won't be searched.",
                this::findNamedElementsByName,
                NameDTO.class,
                NameIdTypeListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_proj_path",
                "Return the full path of the Astah project file (e.g., /path/to/project.asta). If the project has not been saved, return an empty string. For example, use this tool when you want to derive a relative path from an absolute path based on the Astah project file location.",
                this::getProjectPath,
                NoInputDTO.class,
                ProjectPathDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
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
                "Save the current project, and return the full path of the Astah project file (e.g., /path/to/project.asta). Note: Save the project using this tool only when the user explicitly instructs you to do so, or when explicitly instructed in Agent Skills.",
                this::saveProject,
                NoInputDTO.class,
                ProjectPathDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "save_proj_as",
                "Save the current project with a new name, and return the full path of the Astah project file (e.g., /path/to/project.asta). Note: Save the project using this tool only when the user explicitly instructs you to do so, or when explicitly instructed in Agent Skills.",
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
        log.debug("Create project (root package): {}", param);

        // Check if the current project is modified
        if (projectAccessor.hasProject() && projectAccessor.isProjectModified()) {
            if (!"no_title".equals(projectAccessor.getProjectPath())) {
                // For a modified project
                throw new RuntimeException("The existing project needs to be saved before creating a new project.");

            } else {
                // For a new unsaved project, create and delete a dummy project before continuing to avoid showing the save confirmation dialog.
                try {
                    Path tempProjectPath = McpServerConfig.WORKSPACE_DIR.resolve("dummy.asta");
                    Files.createDirectories(tempProjectPath.getParent());
                    Files.deleteIfExists(tempProjectPath);
                    projectAccessor.saveAs(tempProjectPath.toString());
                    Files.deleteIfExists(tempProjectPath);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }

        try {
            projectAccessor.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create project (root package).");
        }

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private NamedElementDTO openProject(FilePathDTO param) throws Exception {
        log.debug("Open project: {}", param);

        // Check if the current project is modified
        if (projectAccessor.hasProject() && projectAccessor.isProjectModified()) {
            if (!"no_title".equals(projectAccessor.getProjectPath())) {
                // For a modified project
                throw new RuntimeException("The existing project needs to be saved before opening a new project.");

            } else {
                // For a new unsaved project, create and delete a dummy project before continuing to avoid showing the save confirmation dialog.
                try {
                    Path tempProjectPath = McpServerConfig.WORKSPACE_DIR.resolve("dummy.asta");
                    Files.createDirectories(tempProjectPath.getParent());
                    Files.deleteIfExists(tempProjectPath);
                    projectAccessor.saveAs(tempProjectPath.toString());
                    Files.deleteIfExists(tempProjectPath);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }

        try {
            projectAccessor.open(param.filePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to open project: " + param.filePath());
        }

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private NamedElementDTO getProject(NoInputDTO param) throws Exception {
        log.debug("Get project (root package): {}", param);

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get project (root package).");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private BooleanDTO isProjectOpen(NoInputDTO param) throws Exception {
        log.debug("Is project opened: {}", param);

        return new BooleanDTO(projectAccessor.hasProject());
    }

    private BooleanDTO isProjectModified(NoInputDTO param) throws Exception {
        log.debug("Is project modified: {}", param);

        return new BooleanDTO(projectAccessor.isProjectModified());
    }

    private NameIdTypeListDTO findNamedElementsByName(NameDTO param) throws Exception {
        log.debug("Find named elements by name: {}", param);

        INamedElement[] astahNamedElements = projectAccessor.findElements(INamedElement.class);

        List<NameIdTypeDTO> namedIdTypeDTOs = new ArrayList<>();
        for (INamedElement astahNamedElement : astahNamedElements) {
            if (astahNamedElement.getName().toLowerCase().contains(param.name().toLowerCase())) {
                namedIdTypeDTOs.add(NameIdTypeDTOAssembler.toDTO(astahNamedElement));
            }
        }

        return new NameIdTypeListDTO(namedIdTypeDTOs);
    }

    private ProjectPathDTO saveProject(NoInputDTO param) throws Exception {
        log.debug("Save project: {}", param);

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

        try {
            projectAccessor.save();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save the current project.");
        }

        return new ProjectPathDTO(projectAccessor.getProjectPath());
    }

    private ProjectPathDTO saveProjectAs(FilePathDTO param) throws Exception {
        log.debug("Save project as: {}", param);

        // Check that the parent directory of the file path exists
        File folder = new File(param.filePath()).getParentFile();
        if (folder == null || !folder.exists()) {
            throw new RuntimeException("The parent folder of the specified file path does not exist: " + param.filePath());
        }

        // Check that the file path has the .asta extension
        if (!param.filePath().endsWith(".asta")) {
            throw new RuntimeException("The specified file path is not a valid Astah project file (*.asta): " + param.filePath());
        }

        // Check that the target file does not already exist
        if (new File(param.filePath()).exists()) {
            throw new RuntimeException("The specified file path already exists: " + param.filePath());
        }

        try {
            projectAccessor.saveAs(param.filePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save the current project as: " + param.filePath());
        }

        return new ProjectPathDTO(projectAccessor.getProjectPath());
    }

    private NamedElementDTO closeProject(NoInputDTO param) throws Exception {
        log.debug("Close project: {}", param);

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.");
        }

        try {
            projectAccessor.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close the current project.");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private ProjectPathDTO getProjectPath(NoInputDTO param) throws Exception {
        log.debug("Get project path: {}", param);

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
