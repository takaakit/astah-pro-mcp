package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.common.outputdto.GuideDTO;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.scenario.inputdto.WorkType;
import com.astahpromcp.tool.scenario.outputdto.SetAsideDirectoryDTO;
import com.astahpromcp.tool.scenario.outputdto.ArchitectureScenariosDTO;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.RecordComponent;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ArchitectureScenarioToolTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[A-Z_0-9]+}");

    private static final String DEVELOPMENT = "architecture_scenario_development_steps";
    private static final String EVALUATION = "architecture_scenario_evaluation_steps";
    private static final String READ = "read_architecture_scenarios";
    private static final String SET_ASIDE = "set_aside_architecture_scenario_dir";

    private static final String COMPLETE = ScenarioFileCheckTest.COMPLETE;

    // The file as it is after step 1 of the architecture scenario development steps: its scenarios are not fully developed
    private static final String ONLY_STEP_1 = COMPLETE.substring(0, COMPLETE.indexOf("# Concrete Scenarios"));

    // What the steps tell to do with scenarios left behind by another piece of work
    private static final String LEFT_BEHIND = ArchitectureScenarioDevelopmentSteps.IF_LEFT_BEHIND;

    // A tool function named as one to call: "the read_architecture_scenarios tool", "the 'open_dgm' tool"
    private static final Pattern NAMED_TOOL = Pattern.compile("\\b([a-z][a-z0-9]*(?:_[a-z0-9]+)+)'?\\s+tool\\b");
    // A heading the steps give in a template to write, as "   > # Trade-off Matrix", or name in quotes, as "# Work"
    private static final Pattern TEMPLATE_HEADING = Pattern.compile("^\\s*> (#{1,2}) (.+?)\\s*$");
    private static final Pattern QUOTED_HEADING = Pattern.compile("\"(#{1,2}) ([^\"\\n]+)\"");
    // The heading of the section of one scenario, improvement or decision, as "## S-01" and "## I-01: <title>"
    private static final Pattern ITEM_HEADING = Pattern.compile("^[SDI]-\\d+\\b.*");
    // A field of a template, as "   > - Elements: <...>", and a field of a file, as "- Elements: ..."
    private static final Pattern TEMPLATE_FIELD = Pattern.compile("^\\s*> - ([A-Za-z][A-Za-z ]*?):");
    private static final Pattern FILE_FIELD = Pattern.compile("^- ([A-Za-z][A-Za-z ]*?):");
    // A field of a result named in quotes, as "keptDirectoryPath"
    private static final Pattern QUOTED_FIELD = Pattern.compile("\"([a-z]+(?:[A-Z][a-z0-9]*)+)\"");

    @TempDir
    Path workspaceDir;

    private Path analysisDir() {
        return workspaceDir.resolve("architecture-scenario-analysis").toAbsolutePath();
    }

    private Path scenarioFile() {
        return analysisDir().resolve("scenarios.md");
    }

    private Path imageFile() {
        return analysisDir().resolve("abstract-scenario.svg");
    }

    private Path resultsFile() {
        return analysisDir().resolve("evaluation-results.md");
    }

    private ArchitectureScenarioFiles files() {
        return new ArchitectureScenarioFiles(workspaceDir);
    }

    private ArchitectureScenarioFiles.Snapshot snapshot() throws Exception {
        return files().snapshot();
    }

    private McpSchema.CallToolResult call(String name, Map<String, Object> arguments) {
        ToolDefinition definition = new ArchitectureScenarioTool(workspaceDir).createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no tool function named " + name));
        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(name, arguments, null));
    }

    private <T> T callOk(String name, Map<String, Object> arguments, Class<T> outputType) {
        McpSchema.CallToolResult result = call(name, arguments);
        assertFalse(Boolean.TRUE.equals(result.isError()), name + " failed: " + result.content());
        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), outputType);
    }

    private String callNg(String name, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = call(name, arguments);
        assertTrue(Boolean.TRUE.equals(result.isError()), name + " should have failed: " + result.content());
        return String.valueOf(result.content());
    }

    private String guide(String name, Map<String, Object> arguments) {
        String contents = callOk(name, arguments, GuideDTO.class).contents();
        assertFalse(PLACEHOLDER.matcher(contents).find(), "a placeholder is left unfilled in " + name + ": " + contents);
        return contents;
    }

    private String developmentSteps(String workType) {
        return guide(DEVELOPMENT, Map.of("workType", workType));
    }

    private String evaluationSteps() {
        return guide(EVALUATION, Map.of("stage", "evaluation"));
    }

    private String closingSteps() {
        return guide(EVALUATION, Map.of("stage", "closing"));
    }

    private Path improvementFile() {
        return analysisDir().resolve("architecture-improvements.md");
    }

    private void writeResultsFile() throws Exception {
        Files.createDirectories(analysisDir());
        Files.writeString(resultsFile(), "# Evaluation Results\n", StandardCharsets.UTF_8);
    }

    private ArchitectureScenariosDTO readScenarios() {
        return callOk(READ, Map.of(), ArchitectureScenariosDTO.class);
    }

    private void writeScenarioFile(String contents) throws Exception {
        Files.createDirectories(analysisDir());
        Files.writeString(scenarioFile(), contents, StandardCharsets.UTF_8);
    }

    private void writeImageFile() throws Exception {
        Files.createDirectories(analysisDir());
        Files.writeString(imageFile(), "<svg xmlns=\"http://www.w3.org/2000/svg\"/>", StandardCharsets.UTF_8);
    }

    private List<String> workspaceEntries() throws Exception {
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(workspaceDir)) {
            entries.forEach(entry -> names.add(entry.getFileName().toString()));
        }
        return names;
    }

    // Every text the four tool functions return, from each state the working files go through, and their descriptions
    private List<String> everyStepText() throws Exception {
        List<String> texts = new ArrayList<>();
        texts.add(evaluationSteps());
        texts.add(closingSteps());
        texts.add(developmentSteps("new"));
        texts.add(developmentSteps("change"));
        texts.addAll(readScenarios().problems());

        writeScenarioFile(ONLY_STEP_1.replace("Type: New", "Type: Change"));
        texts.add(developmentSteps("new"));
        texts.add(evaluationSteps());
        texts.add(closingSteps());
        texts.addAll(readScenarios().problems());

        writeScenarioFile(COMPLETE);
        texts.add(developmentSteps("new"));
        writeImageFile();
        texts.add(developmentSteps("change"));
        texts.add(evaluationSteps());
        texts.add(closingSteps());

        writeScenarioFile(COMPLETE.replace("Type: New", "Type: Change"));
        texts.add(evaluationSteps());
        writeResultsFile();
        texts.add(evaluationSteps());
        texts.add(closingSteps());
        texts.add(readScenarios().evaluationGuidance());
        Files.writeString(resultsFile(), "# Evaluation Results\n\n# Changes After the Evaluation\n", StandardCharsets.UTF_8);
        texts.add(readScenarios().evaluationGuidance());

        new ArchitectureScenarioTool(workspaceDir).createToolDefinitions().forEach(definition -> texts.add(definition.toolSchema().description()));
        return texts;
    }

    // The titles of the headings a check reads, taken from the check itself, in lower case as the checks compare them
    @SuppressWarnings("unchecked")
    private static Set<String> headingTitles(Class<?> check, String... fieldNames) throws Exception {
        Set<String> titles = new TreeSet<>();
        for (String fieldName : fieldNames) {
            for (ScenarioFileCheck.KnownHeading heading : (List<ScenarioFileCheck.KnownHeading>) TestSupport.getAccessibleField(check, fieldName).get(null)) {
                titles.add(heading.title().toLowerCase(Locale.ROOT));
            }
        }
        return titles;
    }

    private static Set<String> fieldsOf(String contents) {
        return contents.lines()
                .map(FILE_FIELD::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static String headerRowOf(String contents) {
        return contents.lines().filter(line -> line.startsWith("| ID |")).findFirst().orElseThrow();
    }

    @Test
    void createToolDefinitions_ok_registersTheFourToolFunctions() {
        List<String> names = new ArchitectureScenarioTool(workspaceDir).createToolDefinitions().stream()
                .map(definition -> definition.toolSchema().name())
                .toList();

        assertEquals(List.of(DEVELOPMENT, READ, EVALUATION, SET_ASIDE), names);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createToolDefinitions_ok_takesTheWorkTypeOfTheScenarioDevelopment() {
        Map<String, McpSchema.Tool> tools = new ArchitectureScenarioTool(workspaceDir).createToolDefinitions().stream()
                .map(ToolDefinition::toolSchema)
                .collect(Collectors.toMap(McpSchema.Tool::name, tool -> tool));

        McpSchema.Tool development = tools.get(DEVELOPMENT);
        Map<String, Object> properties = (Map<String, Object>) development.inputSchema().get("properties");
        Map<String, Object> workType = (Map<String, Object>) properties.get("workType");
        assertEquals(Set.of("new", "change"), Set.copyOf((List<String>) workType.get("enum")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createToolDefinitions_ok_tellsWhenToCallTheEvaluationAtEachStage() {
        McpSchema.Tool evaluation = new ArchitectureScenarioTool(workspaceDir).createToolDefinitions().stream()
                .map(ToolDefinition::toolSchema)
                .filter(tool -> tool.name().equals(EVALUATION))
                .findFirst()
                .orElseThrow();

        Map<String, Object> properties = (Map<String, Object>) evaluation.inputSchema().get("properties");
        Map<String, Object> stage = (Map<String, Object>) properties.get("stage");
        assertEquals(Set.of("evaluation", "closing"), Set.copyOf((List<String>) stage.get("enum")));
        assertEquals(List.of("stage"), evaluation.inputSchema().get("required"));
    }

    @Test
    void getScenarioDevelopmentSteps_ok_returnsEveryStepAndCreatesTheDirectoryWhenNotDeveloped() {
        assertFalse(Files.exists(analysisDir()));

        String steps = developmentSteps("new");

        assertEquals(ArchitectureScenarioDevelopmentSteps.notDeveloped(files(), WorkType.NEW), steps);
        assertTrue(steps.contains("scenarios that the architecture you are about to create has to satisfy."), steps);
        assertTrue(steps.contains(scenarioFile().toString()), steps);
        assertTrue(steps.contains(imageFile().toString()), steps);
        assertTrue(steps.contains("   > - Type: New\n"), steps);
        assertTrue(Files.isDirectory(analysisDir()), "the working directory is created in advance");
    }

    @Test
    void getScenarioDevelopmentSteps_ok_tellsHowToFillInAndSettleTheTradeOffMatrix() {
        String steps = developmentSteps("new");

        assertTrue(steps.contains("# Trade-off Resolutions"), steps);
        assertFalse(steps.contains("\"+\""), steps);
    }

    @Test
    void getScenarioDevelopmentSteps_ok_narrowsTheScopeForAChange() {
        String steps = developmentSteps("change");

        assertTrue(steps.contains("scenarios that the change you are about to make to the architecture has to satisfy."), steps);
        assertTrue(steps.contains("   > - Type: Change\n"), steps);
        assertTrue(steps.contains("List the stakeholders the requested change concerns - those it is meant to serve and those whose needs it puts at risk - and not every stakeholder of the system"), steps);
        assertTrue(steps.contains("in the system of interest and that the requested change bears on, because the change is meant to improve it or because the change puts it at risk: maintainability,"), steps);
    }

    @Test
    void getScenarioDevelopmentSteps_ok_takesWhatTheExistingArchitectureSecuresIntoTheScenariosOfAChange() {
        String change = developmentSteps("change");

        assertTrue(change.contains("   - The requirements, use cases and notes in the Astah project, when it has any.\n   - The definitions of the model elements, packages and diagrams that the change touches, and the notes on those diagrams, as they were before the change: an existing architecture records there what it is meant to achieve, such as which parts may depend on which.\n   - Scenarios you reported for the same system earlier in this conversation.\n"), change);
        assertTrue(change.contains("in which the system is used or changed.\n   For each quality that the existing architecture already secures and that the change puts at risk - which parts may depend on which, for example - define at least one scenario that the changed architecture must still satisfy, one whose Measure states what the existing architecture secures now, such as the parts that may not change.\n   Write each scenario in three parts:"), change);

        // No scenario file has been written, so every step is returned again
        String anew = developmentSteps("new");
        assertTrue(anew.contains("   - The requirements, use cases and notes in the Astah project, when it has any.\n   - Scenarios you reported for the same system earlier in this conversation.\n"), anew);
        assertTrue(anew.contains("in which the system is used or changed.\n   Write each scenario in three parts:"), anew);
        assertFalse(anew.contains("existing architecture already secures"), anew);
    }

    @Test
    void getScenarioDevelopmentSteps_ng_needsTheWorkType() {
        assertTrue(callNg(DEVELOPMENT, Map.of()).contains("workType"));
        assertTrue(callNg(DEVELOPMENT, Map.of("workType", "refactoring")).contains("workType"));
        // Reverse-engineering is not a work type the architecture scenario analysis takes
        assertTrue(callNg(DEVELOPMENT, Map.of("workType", "reverse_engineering")).contains("workType"));
        assertFalse(Files.exists(analysisDir()), "a call that is refused creates nothing");
    }

    @Test
    void getScenarioDevelopmentSteps_ok_asksWhoseFileItIsAndTellsToResumeWhenTheScenariosAreNotFullyDeveloped() throws Exception {
        writeScenarioFile(ONLY_STEP_1);
        // The image file is no sign that the scenarios are fully developed
        writeImageFile();

        String steps = developmentSteps("new");

        assertTrue(steps.contains(scenarioFile().toString()), steps);
        // The sections that say whose file it is are quoted from the file
        assertTrue(steps.contains("Create the architecture of a library lending system."), steps);
        assertTrue(steps.contains("Library lending system - lends book copies to members."), steps);
        assertTrue(steps.contains(LEFT_BEHIND), steps);
        assertTrue(steps.contains("If it is yours:\nIts scenarios are not fully developed yet. What is missing:\n- The \"# Concrete Scenarios\" section is missing or holds no concrete scenario"), steps);
    }

    @Test
    void getScenarioDevelopmentSteps_ok_returnsTheStepsOfTheWorkTypeTheFileNames() throws Exception {
        writeScenarioFile(ONLY_STEP_1.replace("Type: New", "Type: Change"));

        String steps = developmentSteps("new");

        assertTrue(steps.contains("   > - Type: Change\n"), steps);
        assertTrue(steps.contains("List the stakeholders the requested change concerns"), steps);
        assertFalse(steps.contains("   > - Type: New\n"), steps);
    }

    @Test
    void getScenarioDevelopmentSteps_ok_tellsWhenTheFileNamesAnotherWorkType() throws Exception {
        writeScenarioFile(ONLY_STEP_1.replace("Type: New", "Type: Change"));

        String steps = developmentSteps("new");

        assertTrue(steps.contains("The file names the type of work \"Change\", but you called this tool with the work type \"new\". Take that into account when you decide below whose it is."), steps);
        // The steps are still those of the work type the file names
        assertTrue(steps.contains("List the stakeholders the requested change concerns"), steps);

        writeScenarioFile(COMPLETE);
        writeImageFile();
        String developed = developmentSteps("change");
        assertTrue(developed.contains("The file names the type of work \"New\", but you called this tool with the work type \"change\"."), developed);
    }

    @Test
    void getScenarioDevelopmentSteps_ok_saysNothingOfTheWorkTypeWhenTheFileAgreesOrNamesNone() throws Exception {
        writeScenarioFile(COMPLETE);
        assertFalse(developmentSteps("new").contains("but you called this tool with"));

        writeScenarioFile(ONLY_STEP_1.replace("Type: New", "Type: Refactoring"));
        assertFalse(developmentSteps("change").contains("but you called this tool with"));
    }

    @Test
    void getScenarioDevelopmentSteps_ok_doesNotDevelopAgainWhenTheScenariosAreFullyDeveloped() throws Exception {
        writeScenarioFile(COMPLETE);
        writeImageFile();

        String steps = developmentSteps("new");

        assertEquals(ArchitectureScenarioDevelopmentSteps.scenarioFileExists(files(), snapshot(), WorkType.NEW), steps);
        assertTrue(steps.contains(LEFT_BEHIND), steps);
        assertTrue(steps.contains("If it is yours:\nIts scenarios are fully developed, so do not develop them again."), steps);
        assertTrue(steps.contains(READ), steps);
        assertFalse(steps.contains("has not been drawn yet"), steps);
        assertEquals(ArchitectureScenarioFiles.State.DEVELOPED, snapshot().state());
    }

    @Test
    void getScenarioDevelopmentSteps_ok_takesTheScenariosForFullyDevelopedWhateverTheDecisionsSay() throws Exception {
        writeScenarioFile(ScenarioFileCheckTest.withDecisions("- D-01: Record loans alone\n"));
        writeImageFile();

        assertEquals(ArchitectureScenarioDevelopmentSteps.scenarioFileExists(files(), snapshot(), WorkType.NEW), developmentSteps("new"));
        assertEquals(ArchitectureScenarioEvaluationSteps.developed(files(), snapshot()), evaluationSteps());
    }

    @Test
    void getScenarioDevelopmentSteps_ok_takesTheScenariosForFullyDevelopedWithoutTheImageAndTellsToDrawIt() throws Exception {
        writeScenarioFile(COMPLETE);

        String steps = developmentSteps("new");

        assertTrue(steps.contains("Its scenarios are fully developed, so do not develop them again."), steps);
        assertTrue(steps.contains("- The abstract scenario has not been drawn yet."), steps);
        assertTrue(steps.contains(imageFile().toString()), steps);
    }

    @Test
    void getScenarioDevelopmentSteps_ok_saysHowLongAgoTheFileWasModified() throws Exception {
        writeScenarioFile(COMPLETE);
        Files.setLastModifiedTime(scenarioFile(), FileTime.from(Instant.now().minus(90, ChronoUnit.MINUTES)));

        String steps = developmentSteps("new");

        assertEquals(90, snapshot().minutesSinceLastModified());
        assertTrue(Pattern.compile("\\b90\\b").matcher(steps).find(), steps);
    }

    @Test
    void getScenarioDevelopmentSteps_ok_takesAFileThatIsNotUtf8ForUnfinished() throws Exception {
        Files.createDirectories(analysisDir());
        // "シナリオ" in Shift_JIS, which is not valid UTF-8
        Files.write(scenarioFile(), new byte[] {(byte) 0x83, (byte) 0x56, (byte) 0x83, (byte) 0x69, (byte) 0x83, (byte) 0x8A, (byte) 0x83, (byte) 0x49});

        String steps = developmentSteps("change");

        assertTrue(steps.contains("Its scenarios are not fully developed yet. What is missing:\n- The scenario file is not valid UTF-8"), steps);
        assertTrue(steps.contains("   > - Type: Change\n"), "the work type the tool was given is used while the file names none: " + steps);
    }

    @Test
    void getScenarioEvaluationSteps_ok_tellsToDevelopTheScenariosFirstUnlessTheWorkTouchedNoArchitecture() throws Exception {
        String steps = evaluationSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.notDeveloped(files()), steps);
        assertTrue(steps.contains(DEVELOPMENT), steps);
        // The value of "Developed" that the scenario file check takes for scenarios developed after the architecture work
        assertTrue(steps.contains(ScenarioFileCheck.DEVELOPED_AFTER), steps);
        assertTrue(steps.contains("'evaluation'"), steps);
        assertFalse(Files.exists(analysisDir()), "asking creates nothing");
    }

    @Test
    void getScenarioEvaluationSteps_ng_needsTheStage() {
        assertTrue(callNg(EVALUATION, Map.of()).contains("stage"));
        assertTrue(callNg(EVALUATION, Map.of("stage", "report")).contains("stage"));
        assertFalse(Files.exists(analysisDir()), "a call that is refused creates nothing");
    }

    @Test
    void getScenarioEvaluationSteps_ok_saysWhatIsMissingWhenTheScenariosAreNotFullyDeveloped() throws Exception {
        writeScenarioFile(ONLY_STEP_1);
        writeImageFile();

        String steps = evaluationSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.developmentInProgress(files(), snapshot()), steps);
        assertTrue(steps.contains("Create the architecture of a library lending system."), steps);
        assertTrue(steps.contains("- The \"# Concrete Scenarios\" section is missing or holds no concrete scenario"), steps);
        assertTrue(steps.contains(DEVELOPMENT), steps);
        assertTrue(steps.contains("'evaluation'"), steps);
        assertTrue(steps.contains(LEFT_BEHIND), steps);
    }

    @Test
    void getScenarioEvaluationSteps_ok_returnsEveryStepForANewArchitectureWithoutTheImage() throws Exception {
        writeScenarioFile(COMPLETE);

        String steps = evaluationSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.developed(files(), snapshot()), steps);
        assertTrue(steps.contains(resultsFile().toString()), steps);
        assertTrue(steps.contains(improvementFile().toString()), steps);
        assertTrue(steps.contains(LEFT_BEHIND), steps);
        assertFalse(steps.contains("Outside the request"), steps);
        assertFalse(steps.contains("outside the request"), steps);
        assertFalse(steps.contains("At least one of these files already exists."), steps);
    }

    @Test
    void getScenarioEvaluationSteps_ok_handsOverWithoutReportingOrDeleting() throws Exception {
        writeScenarioFile(COMPLETE);

        String steps = evaluationSteps();

        // What the closing relies on is checked before the hand-over, while the evaluation is still fresh
        assertTrue(steps.contains(READ), steps);
        assertTrue(steps.contains("'closing'"), steps);
    }

    @Test
    void getScenarioEvaluationSteps_ok_keepsAChangeWithinTheRequest() throws Exception {
        writeScenarioFile(COMPLETE.replace("Type: New", "Type: Change"));

        String steps = evaluationSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.developed(files(), snapshot()), steps);
        assertTrue(steps.contains("   - Outside the request: the NG could only be resolved by changing parts of the existing architecture that the Request under \"# Work\" does not concern."), steps);
        assertTrue(steps.contains("   > - S-07: Outside the request - <the existing parts that would have to change>\n\n4. Improve the architecture."), steps);
    }

    @Test
    void getScenarioEvaluationSteps_ok_takesAFileNamingReverseEngineeringForUnfinished() throws Exception {
        writeScenarioFile(COMPLETE.replace("Type: New", "Type: Reverse-engineering"));

        String steps = evaluationSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.developmentInProgress(files(), snapshot()), steps);
        assertTrue(steps.contains("- \"Type\" under \"# Work\" is missing or is neither New nor Change (step 1)."), steps);
    }

    @Test
    void getScenarioEvaluationSteps_ok_tellsToResumeOnlyWhenAnEvaluationLeftAFile() throws Exception {
        writeScenarioFile(COMPLETE);
        Files.writeString(resultsFile(), "# Evaluation Results\n", StandardCharsets.UTF_8);

        String steps = evaluationSteps();

        assertTrue(steps.contains("At least one of these files already exists. If step 1 finds that the scenario file is yours, these steps were interrupted partway"), steps);
        assertTrue(steps.contains("interrupted partway: read what exists, and resume from the first step whose output is missing from them. If the evaluation results file already has \"# Changes After the Evaluation\", every step is done: do not evaluate again, but go on with the steps for after you have finished editing, recording their edits as step 6 says, and call this tool with the stage 'closing' when you have performed them.\n\n1. Load the scenarios"), steps);
    }

    @Test
    void closing_ok_saysThatThereIsNoEvaluationToCloseWithoutAScenarioFile() throws Exception {
        String steps = closingSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.nothingToClose(files()), steps);
        assertTrue(steps.endsWith("after the evaluation.\n\nNo working directory set aside earlier is left.\n"), steps);
        assertFalse(Files.exists(analysisDir()), "asking creates nothing");
    }

    @Test
    void closing_ok_namesTheWorkingDirectoriesSetAsideWhenThereIsNoScenarioFile() throws Exception {
        writeScenarioFile(COMPLETE);
        writeResultsFile();
        String kept = callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class).keptDirectoryPath();

        String steps = closingSteps();

        assertTrue(steps.contains(kept), steps);
    }

    @Test
    void closing_ok_tellsToEvaluateFirstWhenNoEvaluationResultsExist() throws Exception {
        writeScenarioFile(COMPLETE);
        writeImageFile();

        String steps = closingSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.notEvaluated(files(), snapshot()), steps);
        assertTrue(steps.contains("Create the architecture of a library lending system."), steps);
        assertTrue(steps.contains("'evaluation'"), steps);
        assertTrue(steps.contains("'closing'"), steps);
        assertTrue(steps.contains(LEFT_BEHIND), steps);
    }

    @Test
    void closing_ok_tellsToFinishDevelopingTheScenariosFirstWhileTheyAreNotFullyDeveloped() throws Exception {
        writeScenarioFile(ONLY_STEP_1);
        // Evaluation results beside an unfinished scenario file are no evaluation to close
        writeResultsFile();

        String steps = closingSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.developmentInProgress(files(), snapshot()), steps);
        assertTrue(steps.contains("- The \"# Concrete Scenarios\" section is missing or holds no concrete scenario"), steps);
        assertTrue(steps.contains("'evaluation'"), steps);
        assertFalse(steps.contains("there is no evaluation results file"), steps);
    }

    @Test
    void closing_ok_returnsTheClosingStepsOnceTheEvaluationHasWrittenItsResults() throws Exception {
        writeScenarioFile(COMPLETE);
        writeResultsFile();

        String steps = closingSteps();

        assertEquals(ArchitectureScenarioEvaluationSteps.closing(files(), snapshot()), steps);
        assertTrue(steps.contains(scenarioFile().toString()), steps);
        assertTrue(steps.contains(resultsFile().toString()), steps);
        assertTrue(steps.contains(improvementFile().toString()), steps);
        assertTrue(steps.contains(LEFT_BEHIND), steps);
        // The working files come back from one call, with what is wrong with them
        assertTrue(steps.contains(READ), steps);
        assertTrue(steps.contains("\"evaluationProblems\""), steps);
        assertTrue(steps.contains("\"architectureDecisionProblems\""), steps);
        // The working files are set aside before the report, so that the report can say where they are kept
        assertTrue(steps.contains(SET_ASIDE), steps);
        assertTrue(steps.contains("\"keptDirectoryPath\""), steps);
        assertTrue(steps.contains("NG again in step 5 of the evaluation, turned NG by an improvement, turned NG after the evaluation, improvement not approved, or unresolved trade-off."), steps);
        assertFalse(steps.contains("outside the request"), steps);
    }

    @Test
    void closing_ok_namesTheKindsOfNgOfAChange() throws Exception {
        writeScenarioFile(COMPLETE.replace("Type: New", "Type: Change"));
        writeResultsFile();

        String steps = closingSteps();

        assertTrue(steps.contains("NG again in step 5 of the evaluation, turned NG by an improvement, turned NG after the evaluation, improvement not approved, unresolved trade-off, or outside the request."), steps);
    }

    @Test
    void readScenarios_ok_saysThatThereIsNoScenarioFileInsteadOfFailing() {
        ArchitectureScenariosDTO scenarios = readScenarios();

        assertFalse(scenarios.scenarioFileExists());
        assertEquals("", scenarios.contents());
        assertEquals(1, scenarios.problems().size());
        assertTrue(scenarios.problems().get(0).startsWith("No scenario file exists at " + scenarioFile()), scenarios.problems().toString());
        assertFalse(scenarios.problems().get(0).contains(DEVELOPMENT), "a subagent, which must not develop scenarios, reads this too");
        assertEquals(-1, scenarios.minutesSinceLastModified());
        assertEquals(scenarioFile().toString(), scenarios.scenarioFilePath());
        assertEquals(resultsFile().toString(), scenarios.evaluationResultsFilePath());
        assertFalse(scenarios.evaluationResultsFileExists());
        assertEquals("", scenarios.evaluationResultsContents());
        assertEquals(improvementFile().toString(), scenarios.architectureImprovementsFilePath());
        assertEquals("", scenarios.architectureImprovementsContents());
        assertEquals(List.of(), scenarios.evaluationProblems());
        assertEquals(List.of(), scenarios.architectureDecisionProblems());
        assertEquals("", scenarios.evaluationGuidance());
        assertEquals(List.of(), scenarios.setAsideDirectoryPaths());
    }

    @Test
    void readScenarios_ok_tellsWhetherTheEvaluationHasWrittenItsResults() throws Exception {
        writeScenarioFile(COMPLETE);

        ArchitectureScenariosDTO beforeEvaluation = readScenarios();
        assertEquals(resultsFile().toString(), beforeEvaluation.evaluationResultsFilePath());
        assertFalse(beforeEvaluation.evaluationResultsFileExists());
        assertEquals("", beforeEvaluation.evaluationGuidance(), "no evaluation has begun");
        assertEquals(List.of(), beforeEvaluation.evaluationProblems(), "no evaluation has begun");

        writeResultsFile();

        assertTrue(readScenarios().evaluationResultsFileExists());
    }

    @Test
    void readScenarios_ok_returnsTheEvaluationFilesWithoutAProblemWhenTheyHoldWhatTheClosingReliesOn() throws Exception {
        writeScenarioFile(COMPLETE);
        Files.writeString(resultsFile(), EvaluationFileCheckTest.RESULTS + "\n# Changes After the Evaluation\n", StandardCharsets.UTF_8);
        Files.writeString(improvementFile(), EvaluationFileCheckTest.NO_IMPROVEMENT, StandardCharsets.UTF_8);

        ArchitectureScenariosDTO scenarios = readScenarios();

        assertEquals(EvaluationFileCheckTest.RESULTS + "\n# Changes After the Evaluation\n", scenarios.evaluationResultsContents());
        assertEquals(EvaluationFileCheckTest.NO_IMPROVEMENT, scenarios.architectureImprovementsContents());
        assertEquals(List.of(), scenarios.evaluationProblems());
    }

    @Test
    void readScenarios_ok_listsWhatIsWrongWithTheEvaluationFiles() throws Exception {
        writeScenarioFile(COMPLETE);
        // The results of S-02 are missing, and the improvement file is missing although the evaluation has been handed over
        Files.writeString(resultsFile(), EvaluationFileCheckTest.RESULTS.replace("| S-02 | Developer | Modifiability | OK | |\n", "") + "\n# Changes After the Evaluation\n", StandardCharsets.UTF_8);

        List<String> problems = readScenarios().evaluationProblems();

        assertTrue(problems.stream().anyMatch(problem -> problem.contains("has no row for these scenarios: S-02")), problems.toString());
        assertTrue(problems.stream().anyMatch(problem -> problem.startsWith("The improvement file does not exist")), problems.toString());
    }

    @Test
    void readScenarios_ok_reportsAnEvaluationFileThatIsNotUtf8InsteadOfFailing() throws Exception {
        writeScenarioFile(COMPLETE);
        // "シナリオ" in Shift_JIS, which is not valid UTF-8
        Files.write(resultsFile(), new byte[] {(byte) 0x83, (byte) 0x56, (byte) 0x83, (byte) 0x69, (byte) 0x83, (byte) 0x8A, (byte) 0x83, (byte) 0x49});

        ArchitectureScenariosDTO scenarios = readScenarios();

        assertEquals(COMPLETE, scenarios.contents(), "the scenarios are returned all the same");
        assertEquals("", scenarios.evaluationResultsContents());
        assertEquals(1, scenarios.evaluationProblems().size(), scenarios.evaluationProblems().toString());
        assertTrue(scenarios.evaluationProblems().get(0).startsWith("The evaluation results file is not valid UTF-8: " + resultsFile()), scenarios.evaluationProblems().toString());
    }

    @Test
    void readScenarios_ok_listsWhatIsWrongWithTheDecisions() throws Exception {
        writeScenarioFile(ScenarioFileCheckTest.withDecisions("- D-01: Keep the counter screen in one diagram (S-09)\n"));

        ArchitectureScenariosDTO scenarios = readScenarios();

        assertEquals(List.of(), scenarios.problems(), "the decisions do not keep the scenarios from being fully developed");
        assertEquals(List.of("The decisions under \"# Architecture Decisions\" name these scenario IDs, which \"# Concrete Scenarios\" does not have: S-09 (step 9)."), scenarios.architectureDecisionProblems());
    }

    @Test
    void readScenarios_ok_returnsTheFilesWithoutAByteOrderMark() throws Exception {
        writeScenarioFile("﻿" + COMPLETE);
        Files.writeString(resultsFile(), "﻿" + EvaluationFileCheckTest.RESULTS, StandardCharsets.UTF_8);

        ArchitectureScenariosDTO scenarios = readScenarios();

        assertEquals(COMPLETE, scenarios.contents());
        assertEquals(EvaluationFileCheckTest.RESULTS, scenarios.evaluationResultsContents());
        assertEquals(List.of(), scenarios.problems());
        assertEquals(List.of(), scenarios.evaluationProblems());
    }

    @Test
    void readScenarios_ok_listsTheWorkingDirectoriesSetAsideNewestFirst() throws Exception {
        Files.createDirectories(workspaceDir.resolve("architecture-scenario-analysis-set-aside-20261120-101500"));
        Files.createDirectories(workspaceDir.resolve("architecture-scenario-analysis-set-aside-20261121-090000"));
        Files.createDirectories(workspaceDir.resolve("architecture-scenario-analysis-set-aside-20261121-090000-2"));
        // Neither a directory deleted at once nor a file is a working directory set aside
        Files.createDirectories(workspaceDir.resolve("architecture-scenario-analysis-deleted-20261121-090000"));
        Files.writeString(workspaceDir.resolve("architecture-scenario-analysis-set-aside-note.md"), "note", StandardCharsets.UTF_8);

        List<String> closed = readScenarios().setAsideDirectoryPaths();

        assertEquals(List.of(
                workspaceDir.resolve("architecture-scenario-analysis-set-aside-20261121-090000-2").toAbsolutePath().toString(),
                workspaceDir.resolve("architecture-scenario-analysis-set-aside-20261121-090000").toAbsolutePath().toString(),
                workspaceDir.resolve("architecture-scenario-analysis-set-aside-20261120-101500").toAbsolutePath().toString()), closed);

        writeScenarioFile(COMPLETE);
        assertEquals(closed, readScenarios().setAsideDirectoryPaths(), "they are listed beside scenarios under way as well");
    }

    @Test
    void readScenarios_ok_tellsToResumeTheEvaluationWhileItIsUnderWay() throws Exception {
        writeScenarioFile(COMPLETE);
        writeResultsFile();

        String guidance = readScenarios().evaluationGuidance();

        assertFalse(PLACEHOLDER.matcher(guidance).find(), guidance);
        assertFalse(guidance.isEmpty(), "the evaluation has begun");
        assertFalse(files().evaluationHandedOver());
        assertFalse(guidance.contains("'closing'"), guidance);
    }

    @Test
    void readScenarios_ok_takesAnImprovementFileAloneForAnEvaluationUnderWay() throws Exception {
        writeScenarioFile(COMPLETE);
        Files.writeString(improvementFile(), "# Architecture Improvements\n", StandardCharsets.UTF_8);

        String guidance = readScenarios().evaluationGuidance();

        assertFalse(guidance.isEmpty(), "the evaluation has begun");
        assertFalse(files().evaluationHandedOver());
    }

    @Test
    void readScenarios_ok_tellsToRecordTheEditsAndToCloseOnceTheEvaluationIsHandedOver() throws Exception {
        writeScenarioFile(COMPLETE);
        Files.writeString(resultsFile(), "# Evaluation Results\n\n# Changes After the Evaluation\n", StandardCharsets.UTF_8);

        String guidance = readScenarios().evaluationGuidance();

        assertFalse(PLACEHOLDER.matcher(guidance).find(), guidance);
        assertTrue(files().evaluationHandedOver());
        assertTrue(guidance.contains(EVALUATION), guidance);
        assertTrue(guidance.contains("'closing'"), guidance);
        assertFalse(guidance.contains("'evaluation'"), guidance);
    }

    @Test
    void readScenarios_ok_findsTheHeadingOfTheHandOverInTheFormsAnAgentWritesIt() throws Exception {
        writeScenarioFile(COMPLETE);

        for (String contents : List.of(
                "# Evaluation Results\r\n\r\n# Changes After the Evaluation\r\n- Renamed: A -> B (1)\r\n",
                "# Evaluation Results\n\n#   changes after the evaluation  \n",
                "# Evaluation Results\n\n# Changes After the Evaluation #")) {
            Files.writeString(resultsFile(), contents, StandardCharsets.UTF_8);

            assertTrue(files().evaluationHandedOver(), contents);
        }

        // "シナリオ" in Shift_JIS, which is not valid UTF-8, before the heading: only the heading is looked for
        byte[] heading = "\n# Changes After the Evaluation\n".getBytes(StandardCharsets.US_ASCII);
        byte[] contents = new byte[8 + heading.length];
        System.arraycopy(new byte[] {(byte) 0x83, (byte) 0x56, (byte) 0x83, (byte) 0x69, (byte) 0x83, (byte) 0x8A, (byte) 0x83, (byte) 0x49}, 0, contents, 0, 8);
        System.arraycopy(heading, 0, contents, 8, heading.length);
        Files.write(resultsFile(), contents);

        assertTrue(files().evaluationHandedOver());
    }

    @Test
    void readScenarios_ok_doesNotTakeAnythingButALevelOneHeadingForTheHandOver() throws Exception {
        writeScenarioFile(COMPLETE);

        for (String contents : List.of(
                "# Evaluation Results\n\n## Changes After the Evaluation\n",
                "# Evaluation Results\n\n> # Changes After the Evaluation\n",
                "# Evaluation Results\n\n#\nChanges After the Evaluation\n",
                "# Evaluation Results\n\nAdd the heading \"# Changes After the Evaluation\" later.\n",
                "# Evaluation Results\n\n# Changes After the Evaluation of S-01\n")) {
            Files.writeString(resultsFile(), contents, StandardCharsets.UTF_8);

            assertFalse(files().evaluationHandedOver(), contents);
            assertFalse(readScenarios().evaluationGuidance().isEmpty(), "the evaluation has begun: " + contents);
        }
    }

    @Test
    void readScenarios_ok_tellsNothingAboutTheEvaluationOnceItsWorkingDirectoryIsSetAside() throws Exception {
        writeScenarioFile(COMPLETE);
        Files.writeString(resultsFile(), "# Evaluation Results\n\n# Changes After the Evaluation\n", StandardCharsets.UTF_8);

        callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class);

        assertEquals("", readScenarios().evaluationGuidance());
    }

    @Test
    void readScenarios_ok_returnsTheContentsAndWhetherTheImageExists() throws Exception {
        writeScenarioFile(COMPLETE);

        ArchitectureScenariosDTO beforeImage = readScenarios();
        assertTrue(beforeImage.scenarioFileExists());
        assertEquals(COMPLETE, beforeImage.contents());
        assertEquals(List.of(), beforeImage.problems());
        assertEquals(0, beforeImage.minutesSinceLastModified());
        assertEquals(scenarioFile().toString(), beforeImage.scenarioFilePath());
        assertEquals(imageFile().toString(), beforeImage.abstractScenarioImageFilePath());
        assertFalse(beforeImage.abstractScenarioImageExists());

        writeImageFile();

        assertTrue(readScenarios().abstractScenarioImageExists());
    }

    @Test
    void readScenarios_ok_listsWhatKeepsTheScenariosFromBeingFullyDeveloped() throws Exception {
        writeScenarioFile(COMPLETE.replace("| S-01 | o | . |", "| S-01 | o | |"));

        ArchitectureScenariosDTO scenarios = readScenarios();

        assertTrue(scenarios.scenarioFileExists());
        assertEquals(1, scenarios.problems().size(), scenarios.problems().toString());
        assertTrue(scenarios.problems().get(0).contains("empty cells"), scenarios.problems().toString());
    }

    @Test
    void readScenarios_ng_rejectsAFileThatIsNotUtf8() throws Exception {
        Files.createDirectories(analysisDir());
        // "シナリオ" in Shift_JIS, which is not valid UTF-8
        Files.write(scenarioFile(), new byte[] {(byte) 0x83, (byte) 0x56, (byte) 0x83, (byte) 0x69, (byte) 0x83, (byte) 0x8A, (byte) 0x83, (byte) 0x49});

        String message = callNg(READ, Map.of());

        assertTrue(message.contains("not valid UTF-8"), message);
        assertTrue(message.contains(scenarioFile().toString()), message);
    }

    @Test
    void setDirectoryAside_ok_setsOnlyTheWorkingDirectoryAsideWithItsFilesAndSucceedsTwice() throws Exception {
        writeScenarioFile(COMPLETE);
        writeImageFile();
        Files.writeString(resultsFile(), "# Evaluation Results\n", StandardCharsets.UTF_8);
        Path otherFile = Files.writeString(workspaceDir.resolve("smells-on-architecture-and-design.md"), "smells", StandardCharsets.UTF_8);
        Path otherDir = Files.createDirectories(workspaceDir.resolve("preliminary-layout"));

        SetAsideDirectoryDTO first = callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class);

        assertEquals(analysisDir().toString(), first.directoryPath());
        assertTrue(first.setAside());
        assertFalse(Files.exists(analysisDir()));
        Path kept = Path.of(first.keptDirectoryPath());
        assertEquals(workspaceDir.toAbsolutePath(), kept.getParent());
        assertTrue(kept.getFileName().toString().matches("architecture-scenario-analysis-set-aside-\\d{8}-\\d{6}"), kept.toString());
        assertEquals(COMPLETE, Files.readString(kept.resolve("scenarios.md"), StandardCharsets.UTF_8), "the files are kept as they were");
        assertTrue(Files.exists(kept.resolve("abstract-scenario.svg")));
        assertTrue(Files.exists(kept.resolve("evaluation-results.md")));
        assertTrue(Files.exists(otherFile), "the rest of the workspace is left as it is");
        assertTrue(Files.isDirectory(otherDir), "the rest of the workspace is left as it is");
        assertEquals(List.of(kept.toString()), readScenarios().setAsideDirectoryPaths());

        SetAsideDirectoryDTO second = callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class);

        assertEquals(analysisDir().toString(), second.directoryPath());
        assertFalse(second.setAside());
        assertEquals("", second.keptDirectoryPath());
    }

    @Test
    void setDirectoryAside_ok_keepsEachDirectorySetAsideUnderANameOfItsOwn() throws Exception {
        writeScenarioFile(COMPLETE);
        String first = callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class).keptDirectoryPath();
        writeScenarioFile(ONLY_STEP_1);
        String second = callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class).keptDirectoryPath();

        assertNotEquals(first, second);
        assertEquals(COMPLETE, Files.readString(Path.of(first, "scenarios.md"), StandardCharsets.UTF_8));
        assertEquals(ONLY_STEP_1, Files.readString(Path.of(second, "scenarios.md"), StandardCharsets.UTF_8));
        assertEquals(2, readScenarios().setAsideDirectoryPaths().size());
    }

    @Test
    void setDirectoryAside_ok_bringsTheStateBackToNotDeveloped() throws Exception {
        writeScenarioFile(COMPLETE);
        writeImageFile();

        callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class);

        assertEquals(ArchitectureScenarioDevelopmentSteps.notDeveloped(files(), WorkType.NEW), developmentSteps("new"));
        callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class);
        assertEquals(ArchitectureScenarioEvaluationSteps.notDeveloped(files()), evaluationSteps());
    }

    // Windows refuses to rename a directory while a file in it is open, which is what makes the deletion all or nothing there.
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void setDirectoryAside_ng_changesNothingWhileAFileIsOpen() throws Exception {
        writeScenarioFile(COMPLETE);
        writeImageFile();
        Files.writeString(resultsFile(), "# Evaluation Results\n", StandardCharsets.UTF_8);

        try (FileChannel ignored = FileChannel.open(scenarioFile(), StandardOpenOption.READ)) {
            String message = callNg(SET_ASIDE, Map.of());

            assertTrue(message.contains("Nothing was changed."), message);
            assertTrue(message.contains(analysisDir().toString()), message);
            assertTrue(message.contains("Cause: "), message);
            assertTrue(Files.exists(scenarioFile()), "the open file is still there");
            assertTrue(Files.exists(imageFile()), "and so is every other file: none was deleted ahead of the failure");
            assertTrue(Files.exists(resultsFile()), "and so is every other file: none was deleted ahead of the failure");
            assertEquals(List.of("architecture-scenario-analysis"), workspaceEntries());
            assertEquals(ArchitectureScenarioEvaluationSteps.developed(files(), snapshot()), evaluationSteps(), "the state is what it was before the failure");
        }

        SetAsideDirectoryDTO setAside = callOk(SET_ASIDE, Map.of(), SetAsideDirectoryDTO.class);
        assertTrue(setAside.setAside(), "once the file is closed, the directory can be set aside");
        assertEquals(List.of(Path.of(setAside.keptDirectoryPath()).getFileName().toString()), workspaceEntries());
    }

    // The steps are what an agent writes the working files from, and the checks of those files read what it wrote.
    @Test
    void stepTexts_ok_nameOnlyToolFunctionsThatExist() throws Exception {
        Set<String> named = new TreeSet<>();
        for (String text : everyStepText()) {
            Matcher matcher = NAMED_TOOL.matcher(text);
            while (matcher.find()) {
                named.add(matcher.group(1));
            }
        }

        assertTrue(named.containsAll(Set.of(DEVELOPMENT, READ, EVALUATION, SET_ASIDE)), "the search for named tool functions finds too little: " + named);
        Set<String> unknown = new TreeSet<>(named);
        unknown.removeAll(ToolManifest.load().allNames());
        assertEquals(Set.of(), unknown, "the steps name tool functions that have no row in the manifest");
    }

    // The headings, table columns, matrix cells and fields that the steps tell an agent to write have to be ones the checks read, and the fields of a result that the steps point at have to be ones the result has.
    @Test
    void stepTexts_ok_referToHeadingsColumnsAndFieldsTheChecksRead() throws Exception {
        List<String> texts = everyStepText();

        Set<String> sectionHeadings = new TreeSet<>(headingTitles(ScenarioFileCheck.class, "SECTION_HEADINGS", "DECISION_HEADINGS"));
        sectionHeadings.addAll(headingTitles(EvaluationFileCheck.class, "RESULTS_HEADINGS", "IMPROVEMENT_HEADINGS"));
        Set<String> partHeadings = headingTitles(ScenarioFileCheck.class, "ABSTRACT_SCENARIO_PART_HEADINGS");
        @SuppressWarnings("unchecked")
        Set<String> matrixCellValues = (Set<String>) TestSupport.getAccessibleField(ScenarioFileCheck.class, "MATRIX_CELL_VALUES").get(null);

        Set<String> headings = new TreeSet<>();
        Map<String, String> headerRows = new LinkedHashMap<>();
        Map<String, Set<String>> templateFields = new LinkedHashMap<>();
        int matrixRows = 0;
        for (String text : texts) {
            Matcher quoted = QUOTED_HEADING.matcher(text);
            while (quoted.find()) {
                headings.add(quoted.group(1) + " " + quoted.group(2));
            }
            String heading = null;
            for (String line : text.split("\n")) {
                Matcher template = TEMPLATE_HEADING.matcher(line);
                if (template.matches()) {
                    headings.add(template.group(1) + " " + template.group(2));
                    heading = template.group(2);
                    continue;
                }
                String trimmed = line.trim();
                if (!trimmed.startsWith(">")) {
                    heading = null;
                    continue;
                }
                if (heading == null) {
                    continue;
                }
                String kind = ITEM_HEADING.matcher(heading).matches() ? heading.substring(0, 1) : heading;
                Matcher field = TEMPLATE_FIELD.matcher(line);
                if (field.find()) {
                    templateFields.computeIfAbsent(kind, key -> new TreeSet<>()).add(field.group(1));
                }
                if (trimmed.startsWith("> |")) {
                    String row = trimmed.substring(1).trim();
                    List<String> cells = ScenarioFileCheck.cells(row);
                    if (cells.get(0).equals("ID")) {
                        headerRows.putIfAbsent(heading, row);
                    } else if (heading.equals("Trade-off Matrix") && cells.get(0).matches("S-\\d+")) {
                        matrixRows++;
                        for (String cell : cells.subList(1, cells.size())) {
                            assertTrue(matrixCellValues.contains(cell), "the matrix template holds a cell the scenario file check does not take: " + row);
                        }
                    }
                }
            }
        }

        // Headings
        assertTrue(headings.size() > 10, "the search for headings finds too little: " + headings);
        for (String heading : headings) {
            String title = heading.substring(heading.indexOf(' ') + 1);
            if (ITEM_HEADING.matcher(title).matches()) {
                continue;
            }
            Set<String> known = heading.startsWith("## ") ? partHeadings : sectionHeadings;
            assertTrue(known.contains(title.toLowerCase(Locale.ROOT)), "the steps give a heading the checks do not read: " + heading);
        }

        // Cells of the trade-off matrix
        assertTrue(matrixRows > 0, "no row of the matrix template was found");

        // Columns: each results table, with the header row of its template, is still read without a problem
        assertEquals(Set.of("Trade-off Matrix", "Evaluation Results", "Re-evaluation Results", "Post-Editing Check Results"), headerRows.keySet());
        String results = EvaluationFileCheckTest.RESULTS.replace(headerRowOf(EvaluationFileCheckTest.RESULTS), headerRows.get("Evaluation Results"));
        assertEquals(List.of(), EvaluationFileCheck.check(results, null, EvaluationFileCheckTest.IDS, false));
        String separator = "|---|---|---|---|---|";
        String reEvaluated = EvaluationFileCheckTest.RESULTS_WITH_NG + "\n# Re-evaluation Results\n" + headerRows.get("Re-evaluation Results") + "\n" + separator + "\n"
                + "| S-02 | NG | Meant to be resolved by I-01 | OK | |\n\n## S-02\n- Elements: ReminderGateway (7a8b-0123456789abcdef0123456789abcdef)\n- Mapping: LendingDesk sends reminders through ReminderGateway.\n";
        assertEquals(List.of(), EvaluationFileCheck.check(reEvaluated, EvaluationFileCheckTest.IMPROVEMENT, EvaluationFileCheckTest.IDS, true));
        String checkedAfterEditing = EvaluationFileCheckTest.RESULTS + "\n# Changes After the Evaluation\n\n# Post-Editing Check Results\n" + headerRows.get("Post-Editing Check Results") + "\n|---|---|---|---|\n"
                + "| S-01 | LoanRecord (3c4d-0123456789abcdef0123456789abcdef) changed | OK | |\n\n## S-01\n- Elements: LoanRecord (3c4d-0123456789abcdef0123456789abcdef)\n- Mapping: LoanRecord still records the loan.\n";
        assertEquals(List.of(), EvaluationFileCheck.check(checkedAfterEditing, EvaluationFileCheckTest.NO_IMPROVEMENT, EvaluationFileCheckTest.IDS, true));

        // Fields: the fields of each template are fields of a file that the checks read without a problem
        assertTrue(fieldsOf(COMPLETE).containsAll(templateFields.get("Work")), "Work: " + templateFields);
        assertTrue(fieldsOf(EvaluationFileCheckTest.RESULTS).containsAll(templateFields.get("S")), "scenario sections: " + templateFields);
        assertTrue(fieldsOf(EvaluationFileCheckTest.IMPROVEMENT).containsAll(templateFields.get("I")), "improvements: " + templateFields);

        // Fields of the results the steps point at
        Set<String> resultFields = Stream.of(ArchitectureScenariosDTO.class, SetAsideDirectoryDTO.class)
                .flatMap(type -> Arrays.stream(type.getRecordComponents()))
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
        Set<String> pointedAt = new TreeSet<>();
        for (String text : texts) {
            Matcher matcher = QUOTED_FIELD.matcher(text);
            while (matcher.find()) {
                pointedAt.add(matcher.group(1));
            }
        }
        assertTrue(pointedAt.containsAll(Set.of("evaluationProblems", "architectureDecisionProblems", "keptDirectoryPath")), "the search for fields finds too little: " + pointedAt);
        Set<String> unknownFields = new TreeSet<>(pointedAt);
        unknownFields.removeAll(resultFields);
        assertEquals(Set.of(), unknownFields, "the steps point at fields the results do not have");
    }
}
