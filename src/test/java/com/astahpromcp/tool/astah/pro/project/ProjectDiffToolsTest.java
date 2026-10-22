package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.astah.pro.project.outputdto.DiffStartDTO;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import com.astahpromcp.tool.projectdiff.ProjectDiffResultTool;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.DiffResultDTO;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStage;
import com.astahpromcp.tool.projectdiff.outputdto.DiffStatus;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectKind;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeKind;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

// Both tools end to end, outside Astah: the project open in this JVM stands for the project open in Astah, and the
// project files are written by real separate processes, started from a work folder whose path has characters that the
// system code page may not hold.
public class ProjectDiffToolsTest {

    private static final String MODEL = "src/test/resources/modelfile/model/ClassToolTest.asta";

    @TempDir
    Path dir;

    private ProjectAccessor projectAccessor;
    private ProjectDiffJobs projectDiffJobs;
    private ToolDefinition diffProject;
    private ToolDefinition getProjectDiffResult;
    private Path project;

    @BeforeEach
    void setUp() throws Exception {
        Path folder = dir.resolve("作業 é 한");
        Files.createDirectories(folder);
        project = folder.resolve("Model.asta");
        Files.copy(Path.of(MODEL), project);

        projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
        projectAccessor.open(project.toString(), false, false, true);

        projectDiffJobs = new ProjectDiffJobs(folder.resolve("project-diff"));
        diffProject = new ProjectDiffStartTool(projectAccessor, projectDiffJobs).createToolDefinitions().get(0);
        getProjectDiffResult = new ProjectDiffResultTool(projectDiffJobs).createToolDefinitions().get(0);
    }

    @AfterEach
    void tearDown() {
        if (projectAccessor != null && projectAccessor.hasProject()) {
            projectAccessor.close();
        }
        assertTrue(projectDiffJobs.shutdown());
    }

    private static McpSchema.CallToolResult call(ToolDefinition definition, Map<String, Object> arguments) {
        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(definition.toolSchema().name(), arguments, null));
    }

    private DiffStartDTO diff(String base, String target, String requestId) {
        McpSchema.CallToolResult answer = call(diffProject, Map.of("baseProjectPath", base, "targetProjectPath", target, "requestId", requestId, "isProjectDiffGuideCalled", true));
        assertNotEquals(Boolean.TRUE, answer.isError(), () -> answer.content().toString());
        return JsonSupport.OBJ_MAPPER.convertValue(answer.structuredContent(), DiffStartDTO.class);
    }

    private DiffResultDTO await(String diffId) {
        for (int i = 0; i < 10; i++) {
            McpSchema.CallToolResult answer = call(getProjectDiffResult, Map.of("diffId", diffId, "startIndex", 0, "startPropertyIndex", 0));
            assertNotEquals(Boolean.TRUE, answer.isError(), () -> answer.content().toString());
            DiffResultDTO result = JsonSupport.OBJ_MAPPER.convertValue(answer.structuredContent(), DiffResultDTO.class);
            if (result.status() != DiffStatus.RUNNING) {
                return result;
            }
        }
        return fail("the comparison did not finish");
    }

    @Test
    void diffProject_ok_findsNothingBetweenAFileAndTheSameProjectOpen() {
        DiffStartDTO started = diff(project.toString(), "", "req-1");

        assertEquals(DiffStatus.RUNNING, started.status());
        assertEquals(ProjectKind.OPEN_PROJECT, started.target().kind());
        assertFalse(started.target().captureStartedAt().isEmpty());

        DiffResultDTO done = await(started.diffId());

        assertEquals(DiffStatus.DONE, done.status(), () -> done.failure().toString());
        assertEquals(0, done.summary().total(), () -> done.changes().toString());
        assertTrue(done.normalization().applied());
        // The model file was saved by an older Astah; both sides report the version it was saved with, and the result says so
        assertTrue(done.base().projectModelVersion() > 0);
        assertEquals(done.base().projectModelVersion(), done.target().projectModelVersion());
        assertTrue(done.warnings().stream().anyMatch(warning -> warning.contains("older Astah")), () -> done.warnings().toString());
        assertTrue(Files.isRegularFile(Path.of(done.fullDiffPath())));
    }

    @Test
    void diffProject_ok_findsARenameMadeInTheOpenProject() throws Exception {
        IClass renamed = null;
        for (INamedElement element : projectAccessor.findElements(IClass.class)) {
            if (element instanceof IClass astahClass && !astahClass.getName().isEmpty()) {
                renamed = astahClass;
                break;
            }
        }
        assertNotNull(renamed);
        String oldName = renamed.getName();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        transactionManager.beginTransaction();
        renamed.setName(oldName + " Renamed");
        transactionManager.endTransaction();

        DiffResultDTO done = await(diff(project.toString(), "", "").diffId());

        assertEquals(DiffStatus.DONE, done.status(), () -> done.failure().toString());
        String id = renamed.getId();
        ChangeDTO change = done.changes().stream().filter(candidate -> candidate.xmiId().equals(id)).findFirst()
                .orElseThrow(() -> new AssertionError("the renamed class is not among " + done.changes()));
        assertEquals(oldName, change.properties().stream().filter(property -> property.path().equals("@name")).findFirst().orElseThrow().before());
        assertEquals(oldName + " Renamed", change.name());
    }

    @Test
    void diffProject_ok_comparesTwoFiles() throws Exception {
        Path other = project.resolveSibling("Other.asta");
        Files.copy(project, other);

        DiffStartDTO started = diff(project.toString(), other.toString(), "");
        DiffResultDTO done = await(started.diffId());

        assertEquals(ProjectKind.FILE, done.target().kind());
        assertEquals("Other", done.target().projectName());
        assertEquals(DiffStatus.DONE, done.status(), () -> done.failure().toString());
        assertEquals(0, done.summary().total(), () -> done.changes().toString());
    }

    // Every change of a result, read page by page, with the parts of a change split across pages joined
    private List<ChangeDTO> allChanges(DiffResultDTO first) {
        List<ChangeDTO> changes = new ArrayList<>();
        DiffResultDTO page = first;
        while (true) {
            for (ChangeDTO part : page.changes()) {
                if (part.propertyStart() == 0) {
                    changes.add(part);
                    continue;
                }
                ChangeDTO begun = changes.removeLast();
                assertEquals(begun.xmiId(), part.xmiId());
                List<PropertyChangeDTO> joined = new ArrayList<>(begun.properties());
                joined.addAll(part.properties());
                changes.add(new ChangeDTO(part.kind(), part.xmiId(), part.baseXmiId(), part.type(), part.stereotypes(), part.name(), part.xmlContextOwner(),
                        part.beforeContext(), part.afterContext(), part.diagram(), part.model(), 0, part.propertyCount(), joined));
            }
            int next = page.nextIndex();
            int nextProperty = page.nextPropertyIndex();
            if (next == -1) {
                break;
            }
            McpSchema.CallToolResult answer = call(getProjectDiffResult, Map.of("diffId", first.diffId(), "startIndex", next, "startPropertyIndex", nextProperty));
            assertNotEquals(Boolean.TRUE, answer.isError(), () -> answer.content().toString());
            page = JsonSupport.OBJ_MAPPER.convertValue(answer.structuredContent(), DiffResultDTO.class);
            assertEquals(next, page.startIndex());
            assertEquals(nextProperty, page.startPropertyIndex());
        }
        assertEquals(first.summary().total(), changes.size());
        assertTrue(changes.stream().allMatch(change -> change.properties().size() == change.propertyCount()), "every change is whole");
        return changes;
    }

    @Test
    void getProjectDiffResult_ng_needsTheStartPropertyIndex() {
        McpSchema.CallToolResult answer = call(getProjectDiffResult, Map.of("diffId", "d1-000000", "startIndex", 0));

        assertEquals(Boolean.TRUE, answer.isError());
        assertTrue(answer.content().toString().contains("startPropertyIndex"), () -> answer.content().toString());
    }

    // In the XMI Astah writes, the elements that refer to a deleted class and its associations list them among other references.
    @Test
    void diffProject_ok_showsTheReferencesADeletedClassTakesAlongAsRemovedOnly() throws Exception {
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        BasicModelEditor editor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        transactionManager.beginTransaction();
        IPackage owner = editor.createPackage(projectAccessor.getProject(), "DiffLists");
        IClass hub = editor.createClass(owner, "Hub");
        List<IClass> spokes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            IClass spoke = editor.createClass(owner, "Spoke" + i);
            editor.createAssociation(hub, spoke, "toSpoke" + i, "hub", "spoke" + i);
            spokes.add(spoke);
        }
        transactionManager.endTransaction();
        Path base = project.resolveSibling("Lists.asta");
        projectAccessor.saveAs(base.toString());

        transactionManager.beginTransaction();
        editor.delete(spokes.get(1));
        transactionManager.endTransaction();
        Path target = project.resolveSibling("ListsWithoutSpoke1.asta");
        projectAccessor.saveAs(target.toString());

        DiffResultDTO done = await(diff(base.toString(), target.toString(), "").diffId());
        assertEquals(DiffStatus.DONE, done.status(), () -> done.failure().toString());
        List<ChangeDTO> changes = allChanges(done);

        String removedId = spokes.get(1).getId();
        assertTrue(changes.stream().anyMatch(change -> change.xmiId().equals(removedId) && change.kind() == ChangeKind.REMOVED), () -> changes.toString());
        // No member of a list of references shows as modified, which is how a shifted list used to show
        Pattern member = Pattern.compile("\\[\\d+]/@xmi\\.idref$");
        List<PropertyChangeDTO> shifted = changes.stream().flatMap(change -> change.properties().stream())
                .filter(property -> property.change() == PropertyChangeKind.MODIFIED && member.matcher(property.path()).find())
                .toList();
        assertTrue(shifted.isEmpty(), () -> shifted.toString());
        // and what the deletion took out of a list shows as removed
        assertTrue(changes.stream().filter(change -> change.kind() == ChangeKind.MODIFIED).flatMap(change -> change.properties().stream())
                .anyMatch(property -> property.change() == PropertyChangeKind.REMOVED && !property.path().contains("[")), () -> changes.toString());
    }

    @Test
    void diffProject_ok_returnsTheSameComparisonForTheSameRequestId() {
        DiffStartDTO first = diff(project.toString(), "", "req-again");
        DiffStartDTO again = diff(project.toString(), "", "req-again");

        assertEquals(first.diffId(), again.diffId());
        await(first.diffId());
    }

    @Test
    void diffProject_ok_returnsTheSameComparisonForTheSameRequestIdAfterItsFilesAreDeleted() throws Exception {
        Path base = project.resolveSibling("Base.asta");
        Path target = project.resolveSibling("Target.asta");
        Files.copy(project, base);
        Files.copy(project, target);
        DiffStartDTO first = diff(base.toString(), target.toString(), "req-deleted");
        Files.delete(base);
        Files.delete(target);

        DiffStartDTO again = diff(base.toString(), target.toString(), "req-deleted");

        assertEquals(first.diffId(), again.diffId());
        assertEquals(DiffStatus.DONE, await(first.diffId()).status());
        // A new request for the same files is still refused
        McpSchema.CallToolResult refused = call(diffProject, Map.of("baseProjectPath", base.toString(), "targetProjectPath", target.toString(), "requestId", "req-new", "isProjectDiffGuideCalled", true));
        assertEquals(Boolean.TRUE, refused.isError());
        assertTrue(refused.content().toString().contains("does not exist"), refused.content().toString());
    }

    @Test
    void diffProject_ok_returnsTheSameComparisonForTheSameRequestIdAfterTheProjectIsClosed() {
        DiffStartDTO first = diff(project.toString(), "", "req-closed");
        projectAccessor.close();

        DiffStartDTO again = diff(project.toString(), "", "req-closed");

        assertEquals(first.diffId(), again.diffId());
        await(first.diffId());
    }

    @Test
    void diffProject_ok_doesNotSayAProjectNeverSavedWasSavedByAnOlderAstah() throws Exception {
        projectAccessor.close();
        projectAccessor.create();

        DiffResultDTO done = await(diff(project.toString(), "", "").diffId());

        assertEquals(DiffStatus.DONE, done.status(), () -> done.failure().toString());
        assertEquals("", done.target().path());
        // Only the base, a file saved by an older Astah, calls for the note
        assertEquals(1, done.warnings().size(), () -> done.warnings().toString());
        assertTrue(done.warnings().get(0).startsWith("The base, 'Model', was saved by an older Astah"), done.warnings().get(0));
    }

    @Test
    void diffProject_ng_failsWithTheStageWhenAFileCannotBeRead() throws Exception {
        Path broken = project.resolveSibling("Broken.asta");
        Files.writeString(broken, "not a project");

        DiffResultDTO failed = await(diff(broken.toString(), project.toString(), "").diffId());

        assertEquals(DiffStatus.FAILED, failed.status());
        assertEquals(DiffStage.FILE_EXPORT, failed.failure().stage());
        assertFalse(failed.failure().message().isEmpty());
        assertFalse(failed.expiresAt().isEmpty());
        // Only the logs of the processes are kept; the copies and the XMIs are gone
        Path jobDirectory = project.resolveSibling("project-diff").resolve(failed.diffId());
        assertFalse(Files.exists(jobDirectory.resolve("base")));
        assertFalse(Files.exists(jobDirectory.resolve("target")));
        assertTrue(Files.exists(jobDirectory.resolve("base.log")));
    }

    @Test
    void diffProject_ng_refusesAPathThatIsNotAbsolute() {
        McpSchema.CallToolResult answer = call(diffProject, Map.of("baseProjectPath", "Model.asta", "targetProjectPath", "", "requestId", "", "isProjectDiffGuideCalled", true));

        assertEquals(Boolean.TRUE, answer.isError());
        assertTrue(answer.content().toString().contains("absolute"), answer.content().toString());
    }

    @Test
    void diffProject_ng_startsNothingUntilTheGuideIsCalled() {
        // Refused before anything else is looked at, even a path that is not absolute
        for (String base : List.of(project.toString(), "Model.asta")) {
            McpSchema.CallToolResult answer = call(diffProject, Map.of("baseProjectPath", base, "targetProjectPath", "", "requestId", "req-guide", "isProjectDiffGuideCalled", false));

            assertEquals(Boolean.TRUE, answer.isError());
            String message = answer.content().toString();
            assertTrue(message.contains("Call the project diff guide tool first"), message);
            assertTrue(message.contains("again with 'isProjectDiffGuideCalled' set to true"), message);
        }

        // Nothing was taken in: diffIds are numbered as comparisons are taken in, and the one started next, with the same requestId, is the first
        DiffStartDTO started = diff(project.toString(), "", "req-guide");
        assertTrue(started.diffId().startsWith("d1-"), started.diffId());
        await(started.diffId());
    }
}
