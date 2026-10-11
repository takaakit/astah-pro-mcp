package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionOperandWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionOperandDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyUtil;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class InteractionOperandToolTest {

    private ProjectAccessor projectAccessor;
    private BasicModelEditor basicModelEditor;
    private TransactionSupport transactionSupport;
    private InteractionOperandTool tool;
    private Method getInfo;
    private Method setGuard;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/InteractionOperandToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new InteractionOperandTool(
            basicModelEditor,
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            InteractionOperandTool.class,
            "getInfo",
            IdDTO.class);

        // setGuard() method
        setGuard = TestSupport.getAccessibleMethod(
            InteractionOperandTool.class,
            "setGuard",
            InteractionOperandWithGuardDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok_matchesDiagram() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(interactionOperand.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        InteractionOperandDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            InteractionOperandDTO.class);

        // Check output DTO
        assertEquals(2, outputDTO.lifelines().size());
        assertEquals(1, outputDTO.messages().size());
        assertEquals(2, outputDTO.drawnLifelines().size());
        assertEquals(1, outputDTO.drawnMessages().size());
        assertEquals("Message0", outputDTO.drawnMessages().get(0).name());
        assertTrue(outputDTO.matchesDiagram());
    }

    @Test
    void getInfo_ok_drawnHeight() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");
        INodePresentation combinedFragmentPresentation = (INodePresentation) combinedFragment.getPresentations()[0];

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        InteractionOperandDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(interactionOperand.getId()),
            InteractionOperandDTO.class);

        // Check that the only operand is drawn as high as its combined fragment
        assertEquals(combinedFragmentPresentation.getHeight(), outputDTO.drawnHeight(), 0.1);

        // Change the height of the operand
        transactionSupport.run( () -> {
            combinedFragmentPresentation.setProperty(PresentationPropertyUtil.createOperandLengthKey(1), "150");
        });

        // ----------------------------------------
        // Call getInfo() again
        // ----------------------------------------
        outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(interactionOperand.getId()),
            InteractionOperandDTO.class);

        // Check that the drawn height follows
        assertEquals(150, outputDTO.drawnHeight(), 0.1);
    }

    @Test
    void getInfo_ok_doesNotMatchDiagram() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElementByClassAndName(
            ICombinedFragment.class,
            "");
        IPresentation combinedFragmentPresentation = combinedFragment.getPresentations()[0];

        // Make Astah's model lose its record of the lifelines and messages the operand holds, while the diagram still draws them:
        // Astah clears the record when the length of the last operand is set, even to the length it already has.
        transactionSupport.run( () -> {
            String lengthKey = PresentationPropertyUtil.createOperandLengthKey(1);
            combinedFragmentPresentation.setProperty(lengthKey, combinedFragmentPresentation.getProperty(lengthKey));
        });
        assertEquals(0, interactionOperand.getMessages().length);

        // Create input DTO
        IdDTO inputDTO = new IdDTO(interactionOperand.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        InteractionOperandDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            InteractionOperandDTO.class);

        // Check output DTO
        assertEquals(0, outputDTO.lifelines().size());
        assertEquals(0, outputDTO.messages().size());
        assertEquals(2, outputDTO.drawnLifelines().size());
        assertEquals(1, outputDTO.drawnMessages().size());
        assertEquals("Message0", outputDTO.drawnMessages().get(0).name());
        assertFalse(outputDTO.matchesDiagram());
    }

    @Test
    void setGuard_ok() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        // Create input DTO
        InteractionOperandWithGuardDTO inputDTO = new InteractionOperandWithGuardDTO(
            interactionOperand.getId(),
            "x > 0");

        // Check guard before setting
        assertNotEquals("x > 0", interactionOperand.getGuard());

        // ----------------------------------------
        // Call setGuard()
        // ----------------------------------------
        InteractionOperandDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            inputDTO,
            InteractionOperandDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check guard after setting
        assertEquals("x > 0", interactionOperand.getGuard());
    }

    @Test
    void setGuard_ok_replacesCurrentGuard() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), "x > 0"),
            InteractionOperandDTO.class);
        int constraintCountAfterFirstGuard = interactionOperand.getConstraints().length;

        // ----------------------------------------
        // Call setGuard() again on the operand that already has a guard
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), "y > 0"),
            InteractionOperandDTO.class);

        // Check that the guard was replaced rather than another guard constraint added
        assertEquals("y > 0", interactionOperand.getGuard());
        assertEquals(constraintCountAfterFirstGuard, interactionOperand.getConstraints().length);
    }

    @Test
    void setGuard_ok_emptyGuardRemovesGuard() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), "x > 0"),
            InteractionOperandDTO.class);
        int constraintCountWithGuard = interactionOperand.getConstraints().length;

        // ----------------------------------------
        // Call setGuard() with an empty guard
        // ----------------------------------------
        InteractionOperandDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), ""),
            InteractionOperandDTO.class);

        // Check that the guard constraint was deleted rather than an empty one added beside it
        assertEquals("", outputDTO.guard());
        assertEquals("", interactionOperand.getGuard());
        assertEquals(constraintCountWithGuard - 1, interactionOperand.getConstraints().length);
        for (IConstraint constraint : interactionOperand.getConstraints()) {
            assertNotEquals("x > 0", constraint.getSpecification());
        }
    }

    @Test
    void setGuard_ok_blankOrNullGuardRemovesGuard() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        for (String noGuard : new String[] {"   ", null}) {
            TestSupport.instance().invokeToolMethodReturningDto(
                setGuard,
                tool,
                new InteractionOperandWithGuardDTO(interactionOperand.getId(), "x > 0"),
                InteractionOperandDTO.class);
            int constraintCountWithGuard = interactionOperand.getConstraints().length;

            // ----------------------------------------
            // Call setGuard()
            // ----------------------------------------
            TestSupport.instance().invokeToolMethodReturningDto(
                setGuard,
                tool,
                new InteractionOperandWithGuardDTO(interactionOperand.getId(), noGuard),
                InteractionOperandDTO.class);

            // Check that the guard was removed just as with an empty guard
            assertEquals("", interactionOperand.getGuard());
            assertEquals(constraintCountWithGuard - 1, interactionOperand.getConstraints().length);
        }
    }

    @Test
    void setGuard_ok_surroundingWhitespaceRemoved() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        // ----------------------------------------
        // Call setGuard()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), " x > 0 "),
            InteractionOperandDTO.class);
        assertEquals("x > 0", interactionOperand.getGuard());

        TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), ""),
            InteractionOperandDTO.class);
        TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), " y > 0 "),
            InteractionOperandDTO.class);

        // Check that the whitespace is removed on both paths
        assertEquals("y > 0", interactionOperand.getGuard());
    }

    @Test
    void setGuard_ok_emptyGuardConstraintLeftUnlinked() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElementByClassAndName(
            IInteractionOperand.class,
            "");

        // Leave an empty guard constraint that is no longer linked as the guard, as a model edited before the workaround can hold:
        // an empty guard set through the Astah API, and then another guard constraint deleted, which clears the guard.
        transactionSupport.run( () -> {
            interactionOperand.setGuard("x > 0");
            interactionOperand.setGuard("");
        });
        transactionSupport.run( () -> {
            for (IConstraint constraint : interactionOperand.getConstraints()) {
                if (!constraint.getSpecification().isEmpty()) {
                    basicModelEditor.delete(constraint);
                }
            }
        });
        assertEquals("", interactionOperand.getGuard());

        // ----------------------------------------
        // Call setGuard()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            setGuard,
            tool,
            new InteractionOperandWithGuardDTO(interactionOperand.getId(), "y > 0"),
            InteractionOperandDTO.class);

        // Check that the guard is set rather than the unlinked constraint rewritten
        assertEquals("y > 0", interactionOperand.getGuard());
    }
}
