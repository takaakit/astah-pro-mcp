package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewUseCaseDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.UseCaseDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/UseCaseDiagramEditor.html
@Slf4j
public class UseCaseDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final UseCaseDiagramEditor useCaseDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;

    public UseCaseDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, UseCaseDiagramEditor useCaseDiagramEditor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.useCaseDiagramEditor = useCaseDiagramEditor;
        this.astahProToolSupport = astahProToolSupport;
    }
    
    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.definition(
                "create_use_dgm",
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
