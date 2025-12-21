package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewUseCaseDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.UseCaseDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/UseCaseDiagramEditor.html
@Slf4j
public class UseCaseDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final UseCaseDiagramEditor useCaseDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public UseCaseDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, UseCaseDiagramEditor useCaseDiagramEditor, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.useCaseDiagramEditor = useCaseDiagramEditor;
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
            log.error("Failed to create usecase diagram editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_usecase_dgm",
                        "Create a new usecase diagram on the specified package (specified by ID), and return the newly created usecase diagram information.",
                        this::createUseCaseDiagram,
                        NewUseCaseDiagramDTO.class,
                        DiagramDTO.class)
        );
    }

    private DiagramDTO createUseCaseDiagram(McpSyncServerExchange exchange, NewUseCaseDiagramDTO param) throws Exception {
        log.debug("Create usecase diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IUseCaseDiagram astahUseCaseDiagram = useCaseDiagramEditor.createUseCaseDiagram(
                astahPackage,
                param.newUseCaseDiagramName());
            transactionManager.endTransaction();

            return DiagramDTOAssembler.toDTO(astahUseCaseDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
