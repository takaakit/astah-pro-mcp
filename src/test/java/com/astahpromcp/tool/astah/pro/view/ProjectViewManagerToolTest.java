package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementListDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IProjectViewManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

@Disabled("ProjectViewManager is not available in non-plugin")
public class ProjectViewManagerToolTest {

    private ProjectAccessor projectAccessor;
    private ProjectViewManagerTool tool;
    private Method getSelectedElements;
    private Method showInPropertyView;
    private Method showInStructureTree;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.open("src/test/resources/modelfile/view/ProjectViewManagerToolTest.asta");
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        IProjectViewManager projectViewManager = astahApi.getViewManager().getProjectViewManager();
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ProjectViewManagerTool(
            projectAccessor,
            projectViewManager,
            transactionSupport,
            astahProToolSupport,
            true);
        
        // getSelectedElements() method
        getSelectedElements = TestSupport.getAccessibleMethod(
            ProjectViewManagerTool.class,
            "getSelectedElements",
            NoInputDTO.class);

        // showInPropertyView() method
        showInPropertyView = TestSupport.getAccessibleMethod(
            ProjectViewManagerTool.class,
            "showInPropertyView",
            IdDTO.class);

        // showInStructureTree() method
        showInStructureTree = TestSupport.getAccessibleMethod(
            ProjectViewManagerTool.class,
            "showInStructureTree",
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getSelectedElements_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();
        
        // ----------------------------------------
        // Call getSelectedElements()
        // ----------------------------------------
        ElementListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getSelectedElements,
            tool,
            inputDTO,
            ElementListDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void showInPropertyView_ok() throws Exception {
        // Get element
        IElement element = TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(element.getId());

        // ----------------------------------------
        // Call showInPropertyView()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            showInPropertyView,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void showInStructureTree_ok() throws Exception {
        // Get element
        IElement element = TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(element.getId());

        // ----------------------------------------
        // Call showInStructureTree()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            showInStructureTree,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
