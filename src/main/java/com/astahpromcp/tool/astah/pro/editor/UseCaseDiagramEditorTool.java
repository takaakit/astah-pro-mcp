package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.editor.inputdto.NewUseCaseDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.change_vision.jude.api.inf.editor.UseCaseDiagramEditor;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/UseCaseDiagramEditor.html
@Slf4j
public class UseCaseDiagramEditorTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final UseCaseDiagramEditor useCaseDiagramEditor;
    private final AstahProToolSupport astahProToolSupport;

    public UseCaseDiagramEditorTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, UseCaseDiagramEditor useCaseDiagramEditor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.useCaseDiagramEditor = useCaseDiagramEditor;
        this.astahProToolSupport = astahProToolSupport;
    }


    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_usecase_dgm",
                "Create a new usecase diagram on the specified package (specified by ID), and return the newly created model element of the usecase diagram.",
                this::createUseCaseDiagram,
                NewUseCaseDiagramDTO.class,
                DiagramDTO.class)
        );
    }

    private DiagramDTO createUseCaseDiagram(NewUseCaseDiagramDTO param) throws Exception {
        log.debug("Create usecase diagram: {}", param);

        IPackage astahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IUseCaseDiagram astahUseCaseDiagram = txnAstah.call( () -> {
            return useCaseDiagramEditor.createUseCaseDiagram(
                astahPackage,
                param.newUseCaseDiagramName());
        });

        return DiagramDTOAssembler.toDTO(astahUseCaseDiagram);
    }
}
