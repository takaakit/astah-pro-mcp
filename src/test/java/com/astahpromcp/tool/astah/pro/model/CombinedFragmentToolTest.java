package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.CombinedFragmentKind;
import com.astahpromcp.tool.astah.pro.model.inputdto.CombinedFragmentWithKindDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.NewInteractionOperandDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CombinedFragmentDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class CombinedFragmentToolTest {

    private ProjectAccessor projectAccessor;
    private CombinedFragmentTool tool;
    private Method addInteractionOperand;
    private Method setCombinedFragmentKind;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/CombinedFragmentToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new CombinedFragmentTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // addInteractionOperand() method
        addInteractionOperand = TestSupport.getAccessibleMethod(
            CombinedFragmentTool.class,
            "addInteractionOperand",
            McpSyncServerExchange.class,
            NewInteractionOperandDTO.class);

        // setCombinedFragmentKind() method
        setCombinedFragmentKind = TestSupport.getAccessibleMethod(
            CombinedFragmentTool.class,
            "setCombinedFragmentKind",
            McpSyncServerExchange.class,
            CombinedFragmentWithKindDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void addInteractionOperand_ok() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElement(
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
        CombinedFragmentDTO outputDTO = TestSupport.instance().invokeToolMethod(
            addInteractionOperand,
            tool,
            inputDTO,
            CombinedFragmentDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(initialOperandCount + 1, outputDTO.interactionOperands().size());
        
        // Check that the operand was added
        assertEquals(initialOperandCount + 1, combinedFragment.getInteractionOperands().length);
    }

    @Test
    void setCombinedFragmentKind_ok() throws Exception {
        // Get combined fragment
        ICombinedFragment combinedFragment = (ICombinedFragment) TestSupport.instance().getNamedElement(
            ICombinedFragment.class,
            "");

        // Create input DTO
        CombinedFragmentWithKindDTO inputDTO = new CombinedFragmentWithKindDTO(
            combinedFragment.getId(),
            CombinedFragmentKind.loop);

        // Check combined fragment kind before setting
        assertFalse(combinedFragment.isLoop());

        // ----------------------------------------
        // Call setCombinedFragmentKind()
        // ----------------------------------------
        CombinedFragmentDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setCombinedFragmentKind,
            tool,
            inputDTO,
            CombinedFragmentDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check combined fragment kind after setting
        assertTrue(combinedFragment.isLoop());
    }
}
