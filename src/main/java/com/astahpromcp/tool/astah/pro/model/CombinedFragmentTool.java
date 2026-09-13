package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.CombinedFragmentWithKindDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionOperandIndexWithHeightDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NewInteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CombinedFragmentDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.CombinedFragmentDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO.Type;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.NodePresentationDTOAssembler;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyUtil;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ICombinedFragment.html
@Slf4j
public class CombinedFragmentTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public CombinedFragmentTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_combined_fragment_info",
                "Return model element information about the specified combined fragment (specified by ID).",
                this::getInfo,
                IdDTO.class,
                CombinedFragmentDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "add_interaction_operand",
                "Add an interaction operand to the specified combined fragment (specified by ID), and return the model element of the combined fragment after it is edited.",
                this::addInteractionOperand,
                NewInteractionOperandDTO.class,
                CombinedFragmentDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_combined_fragment_kind",
                "Set the kind (specified by string) of the specified combined fragment (specified by ID), and return the model element of the combined fragment after it is set.",
                this::setCombinedFragmentKind,
                CombinedFragmentWithKindDTO.class,
                CombinedFragmentDTO.class),

            // Note: To set the height of an interaction operand, the index of the operand known by the combined fragment is required. Therefore, this tool is defined as a tool for the combined fragment rather than for the interaction operand.
            ToolSupport.toolDefinitionReturningDto(
                "set_height_of_interaction_operand",
                "Set the height of the specified interaction operand (specified by 1-based index) of the specified combined fragment (specified by the ID of its node presentation), and return the node presentation of the combined fragment after it is set. Note that, since there is no node presentation for an interaction operand, the node presentation returned is that of the combined fragment containing the interaction operand.",
                this::setHeightOfInteractionOperand,
                InteractionOperandIndexWithHeightDTO.class,
                NodePresentationDTO.class)
        );
    }

    private CombinedFragmentDTO getInfo(IdDTO param) throws Exception {
        log.debug("Get combined fragment information: {}", param);

        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.id());

        return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);
    }

    private CombinedFragmentDTO addInteractionOperand(NewInteractionOperandDTO param) throws Exception {
        log.debug("Add interaction operand: {}", param);

        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.targetCombinedFragmentId());

        txnAstah.run( () -> {
            try {
                astahCombinedFragment.addInteractionOperand(param.newInteractionOperandName(), param.guard());
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                    "Failed to add an interaction operand (%s). Some interaction operators allow only one interaction operand. If multiple interaction operands are needed, change the interaction operator of the combined fragment to one that allows them.",
                    e.getMessage()), e);
            }
        });

        return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);
    }

    private CombinedFragmentDTO setCombinedFragmentKind(CombinedFragmentWithKindDTO param) throws Exception {
        log.debug("Set combined fragment kind: {}", param);

        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.targetCombinedFragmentId());

        txnAstah.run( () -> {
            astahCombinedFragment.setInteractionOperator(param.kind().astahValue);
        });

        return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);
    }

    private NodePresentationDTO setHeightOfInteractionOperand(InteractionOperandIndexWithHeightDTO param) throws Exception {
        log.debug("Set height of interaction operand: {}", param);

        INodePresentation astahCombinedFragmentPresentation = astahProToolSupport.getNodePresentation(param.targetCombinedFragmentNodePresentationId());
        if (!Type.COMBINED_FRAGMENT.matches(astahCombinedFragmentPresentation.getType())) {
            throw new IllegalArgumentException("Combined fragment node must be one of the following node presentation types: CombinedFragment.");
        }

        ICombinedFragment astahCombinedFragment = (ICombinedFragment) astahCombinedFragmentPresentation.getModel();

        // Validate the target interaction operand index
        if (param.targetInteractionOperandIndex() < 1 || param.targetInteractionOperandIndex() > astahCombinedFragment.getInteractionOperands().length) {
            throw new IllegalArgumentException("Invalid interaction operand index: " + param.targetInteractionOperandIndex());
        }
        // Get the key for the operand length property
        String key = PresentationPropertyUtil.createOperandLengthKey(param.targetInteractionOperandIndex());
        // Get the target interaction operand
        IInteractionOperand astahInteractionOperand = astahCombinedFragment.getInteractionOperands()[param.targetInteractionOperandIndex() - 1];

        txnAstah.run( () -> {
            astahCombinedFragmentPresentation.setProperty(key, String.valueOf(param.height()));
        });

        return NodePresentationDTOAssembler.toDTO(astahCombinedFragmentPresentation);
    }
}
