package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.NameDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.BooleanDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTOAssembler;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/project/ProjectAccessor.html
@Slf4j
public class ProjectAccessorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final AstahProToolSupport astahProToolSupport;

    public ProjectAccessorTool(ProjectAccessor projectAccessor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "create_proj",
                            "Create an Astah project (root package), and return the project information. The project element is the root package.",
                            this::createProject,
                            NoInputDTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "open_proj",
                            "Open the specified project (specified by the full path of the Astah project file), and return the project information.",
                            this::openProject,
                            NameDTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "get_proj",
                            "Return the project (root package) information.",
                            this::getProject,
                            NoInputDTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "is_proj_open",
                            "Return whether a project is opened or not.",
                            this::isProjectOpen,
                            NoInputDTO.class,
                            BooleanDTO.class),

                    ToolSupport.definition(
                            "is_proj_mod",
                            "Return whether the current project is modified or not.",
                            this::isProjectModified,
                            NoInputDTO.class,
                            BooleanDTO.class),

                    ToolSupport.definition(
                            "find_named_elems_by_name",
                            "Search named elements in the project by partially matching the element name. Search names are case-insensitive. Note that presentations won't be searched.",
                            this::findNamedElementsByName,
                            NameDTO.class,
                            NameIdTypeListDTO.class)

                    /* Saving and closing the project should be performed based on the user's decision.
                       So these tool functions are disabled.

                    ToolSupport.definition(
                            "save_proj",
                            "Save the current project, and return the current project information.",
                            this::saveProject,
                            NoInputDTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "close_proj",
                            "Close the current project, and return the current project information.",
                            this::closeProject,
                            NoInputDTO.class,
                            NamedElementDTO.class)
                    */
            );
        } catch (Exception e) {
            log.error("Failed to create project accessor tools", e);
            return List.of();
        }
    }

    private NamedElementDTO createProject(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Create project (root package): {}", param);

        try {
            projectAccessor.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create project (root package).");
        }

        IModel astahProject = projectAccessor.getProject();
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private NamedElementDTO openProject(McpSyncServerExchange exchange, NameDTO param) throws Exception {
        log.debug("Open project: {}", param);

        try {
            projectAccessor.open(param.name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to open project: " + param.name());
        }

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }
    
    private NamedElementDTO getProject(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get project (root package): {}", param);

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get project (root package).");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private BooleanDTO isProjectOpen(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Is project opened: {}", param);

        return new BooleanDTO(projectAccessor.hasProject());
    }

    private BooleanDTO isProjectModified(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Is project modified: {}", param);

        return new BooleanDTO(projectAccessor.isProjectModified());
    }

    private NameIdTypeListDTO findNamedElementsByName(McpSyncServerExchange exchange, NameDTO param) throws Exception {
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

    private NamedElementDTO saveProject(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Save project: {}", param);

        IModel astahProject;
        try {
            astahProject = projectAccessor.getProject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current project.");
        }

        try {
            projectAccessor.save();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save the current project.");
        }

        return NamedElementDTOAssembler.toDTO(astahProject);
    }

    private NamedElementDTO closeProject(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
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
}
