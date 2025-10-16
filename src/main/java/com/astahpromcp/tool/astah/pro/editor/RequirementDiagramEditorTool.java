package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.RequirementDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRequirementDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/ja/doc/javadoc/com/change_vision/jude/api/inf/editor/RequirementDiagramEditor.html
@Slf4j
public class RequirementDiagramEditorTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final RequirementDiagramEditor requirementDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;

    public RequirementDiagramEditorTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, RequirementDiagramEditor requirementDiagramEditor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.requirementDiagramEditor = requirementDiagramEditor;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.definition(
                    "create_req_dgm",
                    "Create a new requirement diagram under the specified package (specified by ID), and return the newly created requirement diagram information.",
                    this::createRequirementDiagram,
                    NewDiagramInPackageDTO.class,
                    DiagramDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create requirement diagram editor tools", e);
            return List.of();
        }
    }

    private DiagramDTO createRequirementDiagram(McpSyncServerExchange exchange, NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create requirement diagram: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        try {
            transactionManager.beginTransaction();
            IRequirementDiagram createdAstahRequirementDiagram = requirementDiagramEditor.createRequirementDiagram(
                parentAstahPackage,
                param.newDiagramName());
            transactionManager.endTransaction();

            return DiagramDTOAssembler.toDTO(createdAstahRequirementDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
