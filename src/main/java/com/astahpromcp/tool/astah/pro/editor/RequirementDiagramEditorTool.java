package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewDiagramInPackageDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.change_vision.jude.api.inf.editor.RequirementDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRequirementDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/RequirementDiagramEditor.html
@Slf4j
public class RequirementDiagramEditorTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final RequirementDiagramEditor requirementDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;

    public RequirementDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, RequirementDiagramEditor requirementDiagramEditor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.requirementDiagramEditor = requirementDiagramEditor;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_req_dgm",
                "Create a new requirement diagram under the specified package (specified by ID), and return the newly created model element of the requirement diagram.",
                this::createRequirementDiagram,
                NewDiagramInPackageDTO.class,
                DiagramDTO.class)
        );
    }

    private DiagramDTO createRequirementDiagram(NewDiagramInPackageDTO param) throws Exception {
        log.debug("Create requirement diagram: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.targetPackageId());

        IRequirementDiagram createdAstahRequirementDiagram = txnAstah.call( () -> {
            return requirementDiagramEditor.createRequirementDiagram(
                parentAstahPackage,
                param.newDiagramName());
        });

        return DiagramDTOAssembler.toDTO(createdAstahRequirementDiagram);
    }
}
