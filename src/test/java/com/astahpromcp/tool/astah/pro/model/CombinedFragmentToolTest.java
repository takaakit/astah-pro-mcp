package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.CombinedFragmentKind;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.CombinedFragmentWithKindDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionOperandIndexWithHeightDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NewInteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CombinedFragmentDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyUtil;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CombinedFragmentToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private CombinedFragmentTool tool;
    private Method getInfo;
    private Method addInteractionOperand;
    private Method setCombinedFragmentKind;
    private Method setHeightOfInteractionOperand;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        BasicModelEditor basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/CombinedFragmentToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new CombinedFragmentTool(
            basicModelEditor,
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            CombinedFragmentTool.class,
            "getInfo",
            IdDTO.class);

        // addInteractionOperand() method
        addInteractionOperand = TestSupport.getAccessibleMethod(
            CombinedFragmentTool.class,
            "addInteractionOperand",
            NewInteractionOperandDTO.class);

        // setCombinedFragmentKind() method
        setCombinedFragmentKind = TestSupport.getAccessibleMethod(
            CombinedFragmentTool.class,
            "setCombinedFragmentKind",
            CombinedFragmentWithKindDTO.class);

        // setHeightOfInteractionOperand() method
        setHeightOfInteractionOperand = TestSupport.getAccessibleMethod(
            CombinedFragmentTool.class,
            "setHeightOfInteractionOperand",
            InteractionOperandIndexWithHeightDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(combinedFragment.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        CombinedFragmentDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            CombinedFragmentDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(combinedFragment.getId(), outputDTO.namedElement().element().id());
    }

    @Test
    void addInteractionOperand_ok() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        NewInteractionOperandDTO inputDTO = new NewInteractionOperandDTO(
            combinedFragment.getId(),
            "TestOperand",
            "x > 0");

        // Get initial operand count
        int initialOperandCount = combinedFragment.getInteractionOperands().length;

        // ----------------------------------------
        // Call addInteractionOperand()
        // ----------------------------------------
        CombinedFragmentDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addInteractionOperand,
            tool,
            inputDTO,
            CombinedFragmentDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(initialOperandCount + 1, outputDTO.interactionOperands().size());

        // Check that the operand was added
        assertEquals(initialOperandCount + 1, combinedFragment.getInteractionOperands().length);

        // Check that the added operand holds the given guard alone, without the default "Guard" constraint beside it
        IInteractionOperand addedOperand = combinedFragment.getInteractionOperands()[initialOperandCount];
        assertEquals("x > 0", addedOperand.getGuard());
        assertEquals(1, addedOperand.getConstraints().length);
    }

    @Test
    void addInteractionOperand_ok_emptyGuard() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");
        int initialOperandCount = combinedFragment.getInteractionOperands().length;

        // ----------------------------------------
        // Call addInteractionOperand() with an empty guard
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            addInteractionOperand,
            tool,
            new NewInteractionOperandDTO(combinedFragment.getId(), "TestOperand", ""),
            CombinedFragmentDTO.class);

        // Check that the added operand has no guard, not even the default "Guard"
        IInteractionOperand addedOperand = combinedFragment.getInteractionOperands()[initialOperandCount];
        assertEquals("", addedOperand.getGuard());
        assertEquals(0, addedOperand.getConstraints().length);
    }

    @Test
    void addInteractionOperand_ok_blankOrNullGuard() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        for (String noGuard : new String[] {"   ", null}) {
            int initialOperandCount = combinedFragment.getInteractionOperands().length;

            // ----------------------------------------
            // Call addInteractionOperand()
            // ----------------------------------------
            TestSupport.instance().invokeToolMethodReturningDto(
                addInteractionOperand,
                tool,
                new NewInteractionOperandDTO(combinedFragment.getId(), "TestOperand", noGuard),
                CombinedFragmentDTO.class);

            // Check that the added operand has no guard, just as with an empty guard
            IInteractionOperand addedOperand = combinedFragment.getInteractionOperands()[initialOperandCount];
            assertEquals("", addedOperand.getGuard());
            assertEquals(0, addedOperand.getConstraints().length);
        }
    }

    @Test
    void addInteractionOperand_ok_surroundingWhitespaceRemoved() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");
        int initialOperandCount = combinedFragment.getInteractionOperands().length;

        // ----------------------------------------
        // Call addInteractionOperand()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            addInteractionOperand,
            tool,
            new NewInteractionOperandDTO(combinedFragment.getId(), "TestOperand", " x > 0 "),
            CombinedFragmentDTO.class);

        // Check that the whitespace is removed
        IInteractionOperand addedOperand = combinedFragment.getInteractionOperands()[initialOperandCount];
        assertEquals("x > 0", addedOperand.getGuard());
        assertEquals(1, addedOperand.getConstraints().length);
    }

    @Test
    void setCombinedFragmentKind_ok() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        CombinedFragmentWithKindDTO inputDTO = new CombinedFragmentWithKindDTO(
            combinedFragment.getId(),
            CombinedFragmentKind.LOOP);

        // Check combined fragment kind before setting
        assertFalse(combinedFragment.isLoop());

        // ----------------------------------------
        // Call setCombinedFragmentKind()
        // ----------------------------------------
        CombinedFragmentDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setCombinedFragmentKind,
            tool,
            inputDTO,
            CombinedFragmentDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check combined fragment kind after setting
        assertTrue(combinedFragment.isLoop());
    }

    @Test
    void setHeightOfInteractionOperand_ok_1() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            1,
            120);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check height of the interaction operand after setting
        assertEquals(120, operandHeight(combinedFragment, 1));
    }

    @Test
    void setHeightOfInteractionOperand_ok_2() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            3,
            120);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check height of the interaction operand after setting
        assertEquals(120, operandHeight(combinedFragment, 3));
    }

    @Test
    void setHeightOfInteractionOperand_ok_minimumHeight() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            1,
            10);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check height of the interaction operand after setting
        assertEquals(10, operandHeight(combinedFragment, 1));
    }

    @Test
    void setHeightOfInteractionOperand_ok_heightKeepsMessageInside() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Draw a message in the first interaction operand, 50 below its top
        drawMessageInFirstOperand(combinedFragment, 50);

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            1,
            51);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check height of the interaction operand after setting
        assertEquals(51, operandHeight(combinedFragment, 1));
    }

    @Test
    void setHeightOfInteractionOperand_ng_1() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            0,
            120);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));
    }

    @Test
    void setHeightOfInteractionOperand_ng_2() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            4,
            120);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));
    }

    @Test
    void setHeightOfInteractionOperand_ng_zeroHeight() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            1,
            0);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));
    }

    @Test
    void setHeightOfInteractionOperand_ng_negativeHeight() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getPresentations()[0].getID(),
            1,
            -50);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));
    }

    @Test
    void setHeightOfInteractionOperand_ng_belowMinimumHeight() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");
        INodePresentation combinedFragmentPresentation = (INodePresentation) combinedFragment.getPresentations()[0];
        double operandHeightBefore = operandHeight(combinedFragment, 1);
        double combinedFragmentHeightBefore = combinedFragmentPresentation.getHeight();

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragmentPresentation.getID(),
            1,
            9);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));

        // Check that the combined fragment is left as it was
        assertEquals(operandHeightBefore, operandHeight(combinedFragment, 1));
        assertEquals(combinedFragmentHeightBefore, combinedFragmentPresentation.getHeight());
    }

    @Test
    void setHeightOfInteractionOperand_ng_heightLeavesMessageOutside() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");
        INodePresentation combinedFragmentPresentation = (INodePresentation) combinedFragment.getPresentations()[0];

        // Draw a message in the first interaction operand, 50 below its top
        ILinkPresentation message = drawMessageInFirstOperand(combinedFragment, 50);
        double firstOperandHeightBefore = operandHeight(combinedFragment, 1);
        double secondOperandHeightBefore = operandHeight(combinedFragment, 2);
        double combinedFragmentHeightBefore = combinedFragmentPresentation.getHeight();
        double messageYBefore = message.getPoints()[0].getY();

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragmentPresentation.getID(),
            1,
            50);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));

        // Check that the exception tells that Astah did not apply the height
        assertTrue(exception.getCause().getMessage().contains("did not apply"));

        // Check that what Astah changed although it did not apply the height is rolled back
        assertEquals(firstOperandHeightBefore, operandHeight(combinedFragment, 1));
        assertEquals(secondOperandHeightBefore, operandHeight(combinedFragment, 2));
        assertEquals(combinedFragmentHeightBefore, combinedFragmentPresentation.getHeight());
        assertEquals(messageYBefore, message.getPoints()[0].getY());
    }

    @Test
    void setHeightOfInteractionOperand_ng_modelElementIdGiven() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            combinedFragment.getId(),
            1,
            120);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));
    }

    @Test
    void setHeightOfInteractionOperand_ng_otherNodePresentationType() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");

        // Get a node presentation on the same diagram that does not draw a combined fragment
        INodePresentation notCombinedFragmentPresentation = null;
        for (IPresentation presentation : combinedFragment.getPresentations()[0].getDiagram().getPresentations()) {
            if (presentation instanceof INodePresentation
                && !"CombinedFragment".equals(presentation.getType())) {
                notCombinedFragmentPresentation = (INodePresentation) presentation;
                break;
            }
        }
        assertNotNull(notCombinedFragmentPresentation);

        // Create input DTO
        InteractionOperandIndexWithHeightDTO inputDTO = new InteractionOperandIndexWithHeightDTO(
            notCombinedFragmentPresentation.getID(),
            1,
            120);

        // ----------------------------------------
        // Call setHeightOfInteractionOperand()
        // ----------------------------------------
        assertThrows(Exception.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            setHeightOfInteractionOperand,
            tool,
            inputDTO,
            NodePresentationDTO.class));
    }

    // The height the presentation of the combined fragment keeps for the given interaction operand (1-based index)
    private static double operandHeight(ICombinedFragment combinedFragment, int index) throws Exception {
        return Double.parseDouble(combinedFragment.getPresentations()[0].getProperty(PresentationPropertyUtil.createOperandLengthKey(index)));
    }

    // Draw a message between the two lifelines of the diagram, the given distance below the top of the first interaction operand
    private ILinkPresentation drawMessageInFirstOperand(ICombinedFragment combinedFragment, double distanceFromTop) throws Exception {
        INodePresentation combinedFragmentPresentation = (INodePresentation) combinedFragment.getPresentations()[0];
        INodePresentation[] lifelines = Arrays.stream(combinedFragmentPresentation.getDiagram().getPresentations())
            .filter(presentation -> "Lifeline".equals(presentation.getType()))
            .toArray(INodePresentation[]::new);
        assertEquals(2, lifelines.length);

        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        return transactionSupport.call( () -> {
            sequenceDiagramEditor.setDiagram(combinedFragmentPresentation.getDiagram());
            return sequenceDiagramEditor.createMessage("message", lifelines[0], lifelines[1], combinedFragmentPresentation.getLocation().getY() + distanceFromTop);
        });
    }
}
