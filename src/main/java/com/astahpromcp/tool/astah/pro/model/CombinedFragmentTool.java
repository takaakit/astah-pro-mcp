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
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyUtil;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/ICombinedFragment.html
@Slf4j
public class CombinedFragmentTool extends AstahToolProvider {

    private static final int MIN_INTERACTION_OPERAND_HEIGHT = 10;

    private final BasicModelEditor basicModelEditor;
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public CombinedFragmentTool(BasicModelEditor basicModelEditor, ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.basicModelEditor = basicModelEditor;
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
                "Set the height of the specified interaction operand (specified by 1-based index) of the specified combined fragment (specified by the ID of its node presentation), and return the node presentation of the combined fragment after it is set. The height must be at least 10, and, for an operand other than the last one, must keep every message drawn in the operand above its bottom; otherwise nothing is changed and an error is returned. Note that, since there is no node presentation for an interaction operand, the node presentation returned is that of the combined fragment containing the interaction operand.",
                this::setHeightOfInteractionOperand,
                InteractionOperandIndexWithHeightDTO.class,
                NodePresentationDTO.class)
        );
    }

    private CombinedFragmentDTO getInfo(IdDTO param) throws Exception {
        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.id());

        return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);
    }

    private CombinedFragmentDTO addInteractionOperand(NewInteractionOperandDTO param) throws Exception {
        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.targetCombinedFragmentId());

        // A null or blank guard means no guard, and surrounding whitespace is removed.
        String guard = param.guard() == null ? "" : param.guard().strip();

        txnAstah.run( () -> {
            IInteractionOperand addedInteractionOperand;
            try {
                addedInteractionOperand = astahCombinedFragment.addInteractionOperand(param.newInteractionOperandName(), guard);
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                    "Failed to add an interaction operand (%s). Some interaction operators allow only one interaction operand. If multiple interaction operands are needed, change the interaction operator of the combined fragment to one that allows them.",
                    e.getMessage()), e);
            }

            // Workaround: ICombinedFragment.addInteractionOperand() leaves a default "Guard" constraint beside the given guard, and deleting either one clears the guard. So delete them all and set the guard once. The operand has just been added, so nothing of the user's is lost.
            IConstraint[] constraints = addedInteractionOperand.getConstraints();
            if (constraints.length > 1) {
                for (IConstraint constraint : constraints) {
                    basicModelEditor.delete(constraint);
                }
                
                if (!guard.isEmpty()) {
                    addedInteractionOperand.setGuard(guard);
                }
            }
        });

        return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);
    }

    private CombinedFragmentDTO setCombinedFragmentKind(CombinedFragmentWithKindDTO param) throws Exception {
        ICombinedFragment astahCombinedFragment = astahProToolSupport.getCombinedFragment(param.targetCombinedFragmentId());

        txnAstah.run( () -> {
            astahCombinedFragment.setInteractionOperator(param.kind().astahValue);
        });

        return CombinedFragmentDTOAssembler.toDTO(astahCombinedFragment);
    }

    private NodePresentationDTO setHeightOfInteractionOperand(InteractionOperandIndexWithHeightDTO param) throws Exception {
        INodePresentation astahCombinedFragmentPresentation = astahProToolSupport.getNodePresentation(param.targetCombinedFragmentNodePresentationId());
        if (!Type.COMBINED_FRAGMENT.matches(astahCombinedFragmentPresentation.getType())) {
            throw new IllegalArgumentException("Combined fragment node must be one of the following node presentation types: CombinedFragment.");
        }

        ICombinedFragment astahCombinedFragment = (ICombinedFragment) astahCombinedFragmentPresentation.getModel();

        // Validate the target interaction operand index
        if (param.targetInteractionOperandIndex() < 1 || param.targetInteractionOperandIndex() > astahCombinedFragment.getInteractionOperands().length) {
            throw new IllegalArgumentException("Invalid interaction operand index: " + param.targetInteractionOperandIndex());
        }
        
        if (param.height() < MIN_INTERACTION_OPERAND_HEIGHT) {
            throw new IllegalArgumentException(String.format(
                "The height of an interaction operand must be at least %d: %d", MIN_INTERACTION_OPERAND_HEIGHT, param.height()));
        }
        
        // Get the key for the operand length property
        String key = PresentationPropertyUtil.createOperandLengthKey(param.targetInteractionOperandIndex());

        txnAstah.run( () -> {
            astahCombinedFragmentPresentation.setProperty(key, String.valueOf(param.height()));

            // Workaround: Astah does not apply a height that would leave a message drawn in the operand below its bottom, yet still moves the presentations below and resizes the combined fragment. So check the height it kept, and fail inside the transaction so that those changes are rolled back as well.
            String appliedHeight = astahCombinedFragmentPresentation.getProperty(key);
            if (!hasLength(appliedHeight, param.height())) {
                throw new IllegalArgumentException(String.format(
                    "Astah did not apply the height %d to interaction operand %d (it kept %s), so nothing was changed. Something drawn in the operand, such as a message, probably lies below that height. Specify a height that keeps everything drawn in the operand above its bottom.",
                    param.height(), param.targetInteractionOperandIndex(), appliedHeight));
            }
        });

        return NodePresentationDTOAssembler.toDTO(astahCombinedFragmentPresentation);
    }

    private static boolean hasLength(String length, int expected) {
        try {
            return length != null && Double.parseDouble(length) == expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
