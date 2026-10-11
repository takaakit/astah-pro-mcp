package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.UseCaseDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.UseCaseModelEditor;
import com.change_vision.jude.api.inf.model.IExtend;
import com.change_vision.jude.api.inf.model.IInclude;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.IUseCase;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UseCaseToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private UseCaseTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/UseCaseToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new UseCaseTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            UseCaseTool.class,
            "getInfo",
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get usecase
        IUseCase useCase = (IUseCase) TestSupport.instance().getNamedElementByClassAndName(
            IUseCase.class,
            "UseCase0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(useCase.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        UseCaseDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            UseCaseDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getInfo_ok_includesAndExtendsHoldThoseWhoseIncludingOrExtendingUseCaseIsThisUseCase() throws Exception {
        IModel project = projectAccessor.getProject();
        UseCaseModelEditor useCaseModelEditor = projectAccessor.getModelEditorFactory().getUseCaseModelEditor();

        // A base use case that includes one use case and is extended by another
        IUseCase baseUseCase = transactionSupport.call(() -> useCaseModelEditor.createUseCase(project, "BaseUseCase"));
        IUseCase includedUseCase = transactionSupport.call(() -> useCaseModelEditor.createUseCase(project, "IncludedUseCase"));
        IUseCase extendingUseCase = transactionSupport.call(() -> useCaseModelEditor.createUseCase(project, "ExtendingUseCase"));
        IInclude include = transactionSupport.call(() -> useCaseModelEditor.createInclude(baseUseCase, includedUseCase, ""));
        IExtend extend = transactionSupport.call(() -> useCaseModelEditor.createExtend(extendingUseCase, baseUseCase, ""));

        // The including and extending ends as Astah holds them
        assertEquals(baseUseCase.getId(), include.getIncludingCase().getId());
        assertEquals(includedUseCase.getId(), include.getAddition().getId());
        assertEquals(extendingUseCase.getId(), extend.getExtension().getId());
        assertEquals(baseUseCase.getId(), extend.getExtendedCase().getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        UseCaseDTO baseDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(baseUseCase.getId()),
            UseCaseDTO.class);
        UseCaseDTO includedDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(includedUseCase.getId()),
            UseCaseDTO.class);
        UseCaseDTO extendingDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            new IdDTO(extendingUseCase.getId()),
            UseCaseDTO.class);

        // Check output DTO
        assertEquals(List.of(include.getId()), idsOf(baseDTO.includes()));
        assertEquals(List.of(extend.getId()), idsOf(baseDTO.inverseExtends()));
        assertTrue(baseDTO.inverseIncludes().isEmpty());
        assertTrue(baseDTO.extends_().isEmpty());

        assertEquals(List.of(include.getId()), idsOf(includedDTO.inverseIncludes()));
        assertTrue(includedDTO.includes().isEmpty());

        assertEquals(List.of(extend.getId()), idsOf(extendingDTO.extends_()));
        assertTrue(extendingDTO.inverseExtends().isEmpty());
    }

    private static List<String> idsOf(List<NameIdTypeDTO> elements) {
        return elements.stream().map(NameIdTypeDTO::id).toList();
    }
}
