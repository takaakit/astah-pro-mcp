package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CommunicationDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.CommunicationDiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.InteractionDTOAssembler;
import com.change_vision.jude.api.inf.model.ICommunicationDiagram;
import com.change_vision.jude.api.inf.model.IInteraction;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ICommunicationDiagram.html
@Slf4j
public class CommunicationDiagramTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public CommunicationDiagramTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_communication_dgm_info",
                "Return model element information about the specified communication diagram (specified by ID).",
                this::getInfo,
                IdDTO.class,
                CommunicationDiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_interaction_of_communication_dgm",
                "Return model element information about the interaction of the specified communication diagram (specified by ID).",
                this::getInteraction,
                IdDTO.class,
                InteractionDTO.class)
        );
    }

    private CommunicationDiagramDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get communication diagram information: {}", param);

        ICommunicationDiagram astahCommunicationDiagram = astahProToolSupport.getCommunicationDiagram(param.id());

        return CommunicationDiagramDTOAssembler.toDTO(astahCommunicationDiagram);
    }

    private InteractionDTO getInteraction(IdDTO param) throws Exception {
        log.debug("Get interaction information of communication diagram: {}", param);

        ICommunicationDiagram astahCommunicationDiagram = astahProToolSupport.getCommunicationDiagram(param.id());
        IInteraction astahInteraction = astahCommunicationDiagram.getInteraction();

        return InteractionDTOAssembler.toDTO(astahInteraction);
    }
}
