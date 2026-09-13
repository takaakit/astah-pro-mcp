package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.MindMapDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.MindMapDiagramDTOAssembler;
import com.change_vision.jude.api.inf.model.IMindMapDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IMindMapDiagram.html
@Slf4j
public class MindMapDiagramTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public MindMapDiagramTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_mind_map_dgm_info",
                "Return model element information about the specified mind map diagram (specified by ID).",
                this::getInfo,
                IdDTO.class,
                MindMapDiagramDTO.class)
        );
    }

    private MindMapDiagramDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get mind map diagram information: {}", param);

        IMindMapDiagram astahMindMapDiagram = astahProToolSupport.getMindMapDiagram(param.id());

        return MindMapDiagramDTOAssembler.toDTO(astahMindMapDiagram);
    }
}
