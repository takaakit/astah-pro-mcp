package com.astahpromcp.tool.astah.pro.review;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.ReportDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class DiagramLayoutLintToolTest {

    private ProjectAccessor projectAccessor;
    private DiagramLayoutLintTool tool;
    private Method detectOverlap;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/layout/DiagramLayoutLintToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new DiagramLayoutLintTool(
                projectAccessor,
                transactionSupport,
                astahProToolSupport,
                true);

        // detectOverlap() method
        detectOverlap = TestSupport.getAccessibleMethod(
                DiagramLayoutLintTool.class,
                "detectOverlap",
                IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void detectOverlap_ok_classDiagram() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
                IClassDiagram.class,
                "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call detectOverlap()
        // ----------------------------------------
        ReportDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
                detectOverlap,
                tool,
                inputDTO,
                ReportDTO.class);

        // Check output DTO
        // Check if overlaps are detected correctly, with none missing or extra.
        assertEquals(10, outputDTO.contents().lines().count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Class0") && line.contains("Class1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("asso1") && line.contains("dep0")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Class7") && line.contains("asso2")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("gen2") && line.contains("gen3")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("gen3") && line.contains("Class11")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("asso4") && line.contains("asso8")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("dep1") && line.contains("dep2")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("asso9") && line.contains("asso10")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("asso14") && line.contains("asso15")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Class4") && line.contains("Note0")).count());
    }

    @Test
    void detectOverlap_ok_usecaseDiagram() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
                IUseCaseDiagram.class,
                "UseCase Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call detectOverlap()
        // ----------------------------------------
        ReportDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
                detectOverlap,
                tool,
                inputDTO,
                ReportDTO.class);

        // Check output DTO
        // Check if overlaps are detected correctly, with none missing or extra.
        assertEquals(7, outputDTO.contents().lines().count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("UseCase0") && line.contains("UseCase1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("gen0") && line.contains("gen1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("gen1") && line.contains("Actor1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("asso2") && line.contains("asso5")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("inc0") && line.contains("ext0")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("asso6") && line.contains("dep0")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("UseCase3") && line.contains("Note1")).count());
    }

    @Test
    void detectOverlap_ok_sequenceDiagram() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
                ISequenceDiagram.class,
                "Sequence Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call detectOverlap()
        // ----------------------------------------
        ReportDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
                detectOverlap,
                tool,
                inputDTO,
                ReportDTO.class);

        // Check output DTO
        // Check if overlaps are detected correctly, with none missing or extra.
        assertEquals(3, outputDTO.contents().lines().count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Message0") && line.contains("Message1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Lifeline2") && line.contains("Lifeline3")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Lifeline3") && line.contains("Note2")).count());
    }

    @Test
    void detectOverlap_ok_activityDiagram() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
                IActivityDiagram.class,
                "Activity Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call detectOverlap()
        // ----------------------------------------
        ReportDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
                detectOverlap,
                tool,
                inputDTO,
                ReportDTO.class);

        // Check output DTO
        // Check if overlaps are detected correctly, with none missing or extra.
        assertEquals(11, outputDTO.contents().lines().count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Action0") && line.contains("Action1") && !line.contains("CallBehaviorAction0") && !line.contains("CallBehaviorAction1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("CallBehaviorAction0") && line.contains("CallBehaviorAction1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Decision Node & Merge Node0") && line.contains("Decision Node & Merge Node1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Object0") && line.contains("Object1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("InitialNode0") && line.contains("Note3")).count());
        assertEquals(2, outputDTO.contents().lines().filter(line -> line.matches(".*ControlFlow/ObjectFlow.*ControlFlow/ObjectFlow.*")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Decision Node & Merge Node0") && line.contains("ControlFlow/ObjectFlow")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("SendSignalAction") && line.contains("AcceptEventAction")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("AcceptEventAction") && line.contains("Process")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("ForkNode0") && line.contains("JoinNode0")).count());
    }

    @Test
    void detectOverlap_ok_stateMachineDiagram() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
                IStateMachineDiagram.class,
                "Statemachine Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call detectOverlap()
        // ----------------------------------------
        ReportDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
                detectOverlap,
                tool,
                inputDTO,
                ReportDTO.class);

        // Check output DTO
        // Check if overlaps are detected correctly, with none missing or extra.
        assertEquals(5, outputDTO.contents().lines().count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("Choice0") && line.contains("Choice1")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("ForkPseudostate0") && line.contains("JoinPseudostate0")).count());
        assertEquals(1, outputDTO.contents().lines().filter(line -> line.contains("InitialPseudostate0") && line.contains("Note4")).count());
        assertEquals(2, outputDTO.contents().lines().filter(line -> line.matches(".*Transition.*Transition.*")).count());
    }
}
