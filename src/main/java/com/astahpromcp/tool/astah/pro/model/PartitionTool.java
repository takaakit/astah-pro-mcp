package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.PartitionWithRepresentsDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PartitionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.PartitionDTOAssembler;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPartition;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IPartition.html
@Slf4j
public class PartitionTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public PartitionTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_partition_info",
                "Return model element information about the specified partition (specified by ID).",
                this::getInfo,
                IdDTO.class,
                PartitionDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "set_represents_of_partition",
                "Set the element (specified by ID) that the specified partition (specified by ID) represents, and return the model element of the partition after it is set.",
                this::setRepresents,
                PartitionWithRepresentsDTO.class,
                PartitionDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "remove_represents_from_partition",
                "Remove the represented element from the specified partition (specified by ID), and return the model element of the partition after it is removed.",
                this::removeRepresents,
                IdDTO.class,
                PartitionDTO.class)
        );
    }

    private PartitionDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get partition information: {}", param);

        IPartition astahPartition = astahProToolSupport.getPartition(param.id());

        return PartitionDTOAssembler.toDTO(astahPartition);
    }

    private PartitionDTO setRepresents(PartitionWithRepresentsDTO param) throws Exception {
        log.debug("Set represents of partition: {}", param);

        IPartition astahPartition = astahProToolSupport.getPartition(param.targetPartitionId());
        INamedElement astahRepresents = astahProToolSupport.getNamedElement(param.representsId());

        txnAstah.run( () -> {
            astahPartition.setRepresents(astahRepresents);
        });

        return PartitionDTOAssembler.toDTO(astahPartition);
    }

    private PartitionDTO removeRepresents(IdDTO param) throws Exception {
        log.debug("Remove represents from partition: {}", param);

        IPartition astahPartition = astahProToolSupport.getPartition(param.id());

        txnAstah.run( () -> {
            astahPartition.setRepresents(null);
        });

        return PartitionDTOAssembler.toDTO(astahPartition);
    }
}
