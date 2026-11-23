package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.scenario.inputdto.WorkType;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class ArchitectureScenarioDevelopmentSteps {

    // What to do with a scenario file that another piece of work left behind. The evaluation steps use the same wording.
    static final String IF_LEFT_BEHIND = "Call the set_aside_architecture_scenario_dir tool, and then call this tool again. Say in your report to the user that scenarios left behind for another request were set aside, quote that request, and give the path where the tool says they are kept.";

    // The work that does not need the architecture scenario analysis, one item each. The evaluation steps use the same wording, and so does the guide of this MCP server.
    static final List<String> WORK_WITHOUT_ANALYSIS = List.of(
            "Changing the implementation or the design alone, without changing the architecture.",
            "Reverse-engineering: recovering an architecture from source code as the code has it.",
            "Creating or changing models and diagrams at the conceptual level only: a conceptual model of the problem domain, which describes its concepts and their relationships and contains no element of the software, such as a component, an interface or a deployment.");

    // What the file names when it is not the work type the tool was given, which bears on whose the file is
    private static final String WORK_TYPE_MISMATCH = "The file names the type of work \"{NAMED}\", but you called this tool with the work type \"{GIVEN}\". Take that into account when you decide below whose it is. If it is yours, the type it names is the one that counts: the steps below and the evaluation go by it. If that type is not the work you are doing, correct \"Type\" under \"# Work\" first, and then call this tool again.\n\n";

    private static final String DRAW_INSTRUCTION = "Write SVG code for a single illustration that expresses the abstract scenario. Write colors as #RRGGBB. Call the overlay_svg_img_on_dgm_img tool with an empty string as the target diagram ID to see what the SVG code renders to, and revise the code until the image expresses the abstract scenario. Then write the SVG code to the abstract scenario image file.";

    // What the loaded scenarios are for from here on: the last step when every step is returned, and the last items when the scenarios are fully developed
    private static final List<String> LAST_STEP_SENTENCES = List.of(
            "Keep your attention on the loaded scenarios throughout your subsequent reasoning.",
            "Whenever you draft an edit plan that decides something about the architecture - which elements exist, what each is responsible for, how they depend on one another - name the IDs of the scenarios that decision serves. Reconsider a decision that serves none.",
            "Record each such decision you keep, and those of your subagents, under \"# Architecture Decisions\" at the end of the scenario file, one line each, in the form \"- D-01: <the decision, naming the elements it concerns, each as Name (ID) once it exists> (S-02, S-05)\", with \"(none)\" in place of the IDs for a decision that serves no scenario.");

    private ArchitectureScenarioDevelopmentSteps() {
    }

    // No scenario file: every step
    static String notDeveloped(ArchitectureScenarioFiles files, WorkType workType) {
        String contents = """
# Architecture Scenario Development Before Creating or Changing an Architecture
Follow these steps to develop the scenarios that {PURPOSE}. Do not skip, reorder, or merge steps: finish the thinking of each step before you begin the next.
Do not carry out the architecture scenario analysis for work that is only one or more of the following:
{WORK_WITHOUT_ANALYSIS}
If that is all you are about to do, stop here and go on with your work.

If you are a subagent - an agent launched by another agent, not the one the user asked - stop here and do not carry out these steps. Call the read_architecture_scenarios tool instead, and work with the scenarios it returns, or without any if it returns none.

Working files. Write them in UTF-8 with your file-writing tool, never with shell redirection, and write all of their contents in English.
Use the paths exactly as they are given here. Do not reuse a path from earlier in the conversation.
- Scenario file: {SCENARIO_FILE}
- Abstract scenario image file: {IMAGE_FILE}

1. Record the piece of work.
   Write the scenario file with these two sections at its top.

   > # Work
   > - Type: {WORK_TYPE}
   > - Request: <one line saying what the user asked for>
   > - Developed: Before the architecture work
   >
   > # System of Interest
   > <the name of the system of interest, and a one-line description of it>

   Write the request specifically enough that it cannot be taken for a different one.
   Write "After the architecture work" for "Developed" only when the architecture scenario evaluation steps sent you here after the architecture had already been created or changed.

2. Identify the stakeholders.
   {STAKEHOLDER_SCOPE}
   Take the stakeholders, and in the steps that follow the quality attributes and the scenarios, from these sources. Read the sources before you list anything.
   - The user's request.
   - Documents the user gave you or pointed you to.
   - The requirements, use cases and notes in the Astah project, when it has any.{CHANGE_SOURCE}
   - Scenarios you reported for the same system earlier in this conversation.
   Add a "# Concrete Scenarios" section to the scenario file that lists the stakeholders as a one-level list.

3. Identify the quality attributes.
   For each stakeholder, identify the quality attributes that stakeholder values in the system of interest{QUALITY_ATTRIBUTE_SCOPE}: maintainability, reliability, usability, performance, flexibility, extensibility, reusability, modifiability, adaptability, portability, robustness, security, availability, predictability, understandability, and so on. Different stakeholders may value the same quality attribute.
   Add them to the scenario file as the second level of the list, under their stakeholder.

4. Collect the concrete scenarios.
   Define at least one concrete scenario for each quality attribute of each stakeholder, and more than one where a single scenario cannot express what the stakeholder needs. A scenario describes a single interaction of a stakeholder with the system, in which the system is used or changed.
{REGRESSION_SCENARIOS}   Write each scenario in three parts:
   - Situation: who does what to the system, and under what condition.
   - Response: what the system does, or what changing the system takes.
   - Measure: the condition by which mapping the scenario onto the architecture - finding the elements it goes through, or the elements its change touches - can tell that the response is achieved. For a scenario in which the system is changed, this is the extent of change that is acceptable, such as which parts may be touched.
   Give each concrete scenario a serial ID: S-01, S-02, and so on.
   A stakeholder, a quality attribute or a scenario that none of the sources of step 2 supports is an assumption of yours. Keep it only when the architecture work needs it, and put "(assumed)" at the end of every scenario that rests on one.
   Add the scenarios to the scenario file as the third level of the list, under their quality attribute, in this form: "[S-01] Situation: ... Response: ... Measure: ...".

5. Settle the trade-offs between the concrete scenarios.
   Find the conflicts and contradictions between the concrete scenarios systematically, in the two ways below, not by reading the scenarios through.
   First, fill in a trade-off matrix.
   - Rows: every concrete scenario, by its ID.
   - Columns: every quality attribute identified in step 3, each once.
   - Cells: ask whether satisfying the scenario of the row makes it harder for the system to show the quality of the column. Enter "o" when the column is the quality attribute the scenario belongs to, "-" when it becomes harder, and "." when it does not. Consider every cell and leave none empty.
   For each cell that holds "-" or "o", compare the scenario of the row with every other concrete scenario that belongs to the quality attribute of the column, whichever stakeholder it is listed under. A pair that cannot both be satisfied as written is a conflict or contradiction.
   Second, compare every two concrete scenarios whose Situations can hold at the same moment, whatever their cells say: a pair whose Responses cannot both happen then is a conflict or contradiction as well.
   Resolve a conflict or contradiction only by revising the Situation of a scenario - stating a precondition, or the circumstances the scenario applies to - so that the two scenarios no longer meet. Never change a Response or a Measure to resolve one.
   For every revision, add an entry under "# Trade-off Resolutions" in the format below.

   > # Trade-off Resolutions
   > - S-06 (conflicted with S-01)
   >   - Before: <the Situation as it was>
   >   - After: <the Situation as revised>
   >   - No longer covers: <the circumstances taken out of the scenario>

   A conflict or contradiction that revising a Situation cannot resolve stays unresolved. Record it under "# Unresolved Trade-offs", naming the two scenarios and what cannot both hold. Which of the two comes first is for the user to decide, not for you.
   After revising, fill in the rows of the revised scenarios again, and look for conflicts and contradictions again. Repeat until none is left or three rounds have been run.
   Write the matrix as it stands after the last round to the scenario file under "# Trade-off Matrix", in the format below.

   > # Trade-off Matrix
   > | ID | <quality attribute> | <quality attribute> | <quality attribute> |
   > |---|---|---|---|
   > | S-01 | o | - | . |
   > | S-02 | . | o | . |

6. Develop the abstract scenario.
   Concrete scenarios tend to fix your ideas about the architecture on the particular events they describe. Developing an abstract scenario is how you abstract the scenarios and keep that from happening.
   Referring to all the concrete scenarios, write one abstract scenario that expresses them together, and add it to the scenario file under "# Abstract Scenario" in three parts:
   - "## Summary": one sentence saying what the system of interest guarantees to whom.
   - "## Narrative": one paragraph of three to six sentences saying who interacts with the system in what way and which qualities the system shows then, without figures or the names of individual functions.
   - "## Key Quality Attributes": one list item per quality attribute, saying in one sentence what the concrete scenarios for it ask of the system in common, followed by the IDs of those scenarios in parentheses.
   Every concrete scenario ID must appear under "## Key Quality Attributes" at least once. An ID that does not appear means that scenario was lost in the abstraction; revise the abstract scenario until every ID appears.

7. Draw the abstract scenario.
   Expressing again as a picture what the abstract scenario says in words makes you interpret the abstract scenario afresh; that is what this step is for.
   {DRAW_INSTRUCTION}

8. Show the scenarios to the user.
   Tell the user the following, in the language the user uses, and go on without waiting for an answer.
   - The Summary of the abstract scenario.
   - Every concrete scenario, with its ID, its stakeholder and its quality attribute, marking the ones that are assumed.
   - Every entry under "# Trade-off Resolutions" and under "# Unresolved Trade-offs".
   - That {USE_OF_SCENARIOS}, and that the user can correct the scenarios at any time.
   When the user corrects a scenario, now or later, revise the scenario file, and bring the trade-off matrix, the abstract scenario, the entries under "# Trade-off Resolutions" and the scenario IDs under "# Architecture Decisions" into line with it.

9. Keep your attention on the scenarios.
   Call the read_architecture_scenarios tool to load the abstract scenario and every concrete scenario into your working context. If it reports that the scenario file does not exist, the file is not where these steps say; if it lists problems, the file is not finished. Correct that and call it again.
{LAST_STEP_BODY}
""".replace("{PURPOSE}", purpose(workType))
            .replace("{WORK_WITHOUT_ANALYSIS}", lines("- ", WORK_WITHOUT_ANALYSIS))
            .replace("{WORK_TYPE}", workType.label())
            .replace("{STAKEHOLDER_SCOPE}", stakeholderScope(workType))
            .replace("{QUALITY_ATTRIBUTE_SCOPE}", qualityAttributeScope(workType))
            .replace("{CHANGE_SOURCE}", changeSource(workType))
            .replace("{REGRESSION_SCENARIOS}", regressionScenarios(workType))
            .replace("{DRAW_INSTRUCTION}", DRAW_INSTRUCTION)
            .replace("{USE_OF_SCENARIOS}", useOfScenarios(workType))
            .replace("{LAST_STEP_BODY}", lines("   ", LAST_STEP_SENTENCES))
            .replace("{SCENARIO_FILE}", files.scenarioFile().toString())
            .replace("{IMAGE_FILE}", files.abstractScenarioImageFile().toString());

        return contents;
    }

    // A scenario file exists: whose it is has to be decided before anything else.
    static String scenarioFileExists(ArchitectureScenarioFiles files, ArchitectureScenarioFiles.Snapshot snapshot, WorkType givenWorkType) {
        String contents = """
A scenario file already exists at {SCENARIO_FILE}. It was last modified {MINUTES} minute(s) ago, and it says the following about the piece of work it belongs to.

{HEAD}

{WORK_TYPE_MISMATCH}Decide first whose it is.
- It is yours when you wrote it yourself in the piece of work you are doing now - you are resuming after an interruption or a context compression, for example - and its Request is the request you are working on. In that case go on to "If it is yours" below.
- Otherwise it was left behind by another piece of work, even when it is about the same system: do not use its scenarios, nor any evaluation results beside it. {IF_LEFT_BEHIND}

If it is yours:
""".replace("{IF_LEFT_BEHIND}", IF_LEFT_BEHIND)
            .replace("{MINUTES}", Long.toString(snapshot.minutesSinceLastModified()))
            .replace("{SCENARIO_FILE}", files.scenarioFile().toString())
            .replace("{WORK_TYPE_MISMATCH}", workTypeMismatch(snapshot.check(), givenWorkType))
            .replace("{HEAD}", quoted(snapshot.check().head()));

        if (snapshot.state() == ArchitectureScenarioFiles.State.DEVELOPED) {
            return contents + developed(files);
        }

        // The work type the file names wins over the one the tool was given, because the file is what the evaluation reads
        WorkType workType = snapshot.check().workType().orElse(givenWorkType);
        return contents + developmentInProgress(snapshot.check().problems()) + "\n" + notDeveloped(files, workType);
    }

    // The scenario file does not hold everything yet: what is missing, before every step
    private static String developmentInProgress(List<String> problems) {
        return """
Its scenarios are not fully developed yet. What is missing:
{PROBLEMS}
Resume from the first step below whose output is missing.
""".replace("{PROBLEMS}", lines("- ", problems));
    }

    // The scenario file holds everything: only what is left to do with the finished scenarios
    private static String developed(ArchitectureScenarioFiles files) {
        String drawStep = files.abstractScenarioImageFileExists()
                ? ""
                : "- The abstract scenario has not been drawn yet. " + DRAW_INSTRUCTION + " The abstract scenario image file is " + files.abstractScenarioImageFile() + ".\n";

        return """
Its scenarios are fully developed, so do not develop them again. Do the following instead.
{DRAW_STEP}- If you have not shown the scenarios to the user yet, tell the user, in the language the user uses and without waiting for an answer: the Summary of the abstract scenario, every concrete scenario with its ID, its stakeholder and its quality attribute, marking the ones that are assumed, and every entry under "# Trade-off Resolutions" and "# Unresolved Trade-offs".
- Call the read_architecture_scenarios tool to load the abstract scenario and every concrete scenario into your working context.
{LAST_STEP_BODY}
""".replace("{DRAW_STEP}", drawStep)
            .replace("{LAST_STEP_BODY}", lines("- ", LAST_STEP_SENTENCES));
    }

    // A note for a file that names another work type than the tool was given; empty when the two agree, or when the file names none
    private static String workTypeMismatch(ScenarioFileCheck.Result check, WorkType givenWorkType) {
        return check.workType()
                .filter(named -> named != givenWorkType)
                .map(named -> WORK_TYPE_MISMATCH
                        .replace("{NAMED}", named.label())
                        .replace("{GIVEN}", givenWorkType.label().toLowerCase(Locale.ROOT)))
                .orElse("");
    }

    private static String purpose(WorkType workType) {
        return switch (workType) {
            case NEW -> "the architecture you are about to create has to satisfy";
            case CHANGE -> "the change you are about to make to the architecture has to satisfy";
        };
    }

    private static String stakeholderScope(WorkType workType) {
        return switch (workType) {
            case NEW -> "List the stakeholders of the system of interest: users, developers, administrators, testers, maintainers, and so on.";
            case CHANGE -> "List the stakeholders the requested change concerns - those it is meant to serve and those whose needs it puts at risk - and not every stakeholder of the system: users, developers, administrators, testers, maintainers, and so on.";
        };
    }

    private static String qualityAttributeScope(WorkType workType) {
        return switch (workType) {
            case NEW -> "";
            case CHANGE -> " and that the requested change bears on, because the change is meant to improve it or because the change puts it at risk";
        };
    }

    private static String changeSource(WorkType workType) {
        return switch (workType) {
            case NEW -> "";
            case CHANGE -> "\n   - The definitions of the model elements, packages and diagrams that the change touches, and the notes on those diagrams, as they were before the change: an existing architecture records there what it is meant to achieve, such as which parts may depend on which.";
        };
    }

    private static String regressionScenarios(WorkType workType) {
        return switch (workType) {
            case NEW -> "";
            case CHANGE -> "   For each quality that the existing architecture already secures and that the change puts at risk - which parts may depend on which, for example - define at least one scenario that the changed architecture must still satisfy, one whose Measure states what the existing architecture secures now, such as the parts that may not change.\n";
        };
    }

    private static String useOfScenarios(WorkType workType) {
        return switch (workType) {
            case NEW -> "the architecture will be evaluated against these scenarios once it has been created";
            case CHANGE -> "the changed architecture will be evaluated against these scenarios";
        };
    }

    // Each sentence on a line of its own, behind the given prefix
    static String lines(String prefix, List<String> sentences) {
        return sentences.stream()
                .map(sentence -> prefix + sentence)
                .collect(Collectors.joining("\n"));
    }

    // Text taken from a file, set off as a quotation so that it cannot be taken for part of these steps
    static String quoted(String text) {
        return text.lines()
                .map(line -> line.isEmpty() ? ">" : "> " + line)
                .collect(Collectors.joining("\n"));
    }
}
