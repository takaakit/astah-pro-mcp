package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.scenario.inputdto.WorkType;

import java.nio.file.Path;
import java.util.List;

final class ArchitectureScenarioEvaluationSteps {

    private static final String SUBAGENT_NOTE = "If you are a subagent - an agent launched by another agent, not the one the user asked - stop here and do not carry out these steps. The agent that launched you carries them out once, after the work of all its subagents is in the project.";

    private static final String EXISTING_FILES_NOTE = "At least one of these files already exists. If step 1 finds that the scenario file is yours, these steps were interrupted partway: read what exists, and resume from the first step whose output is missing from them. If the evaluation results file already has \"# Changes After the Evaluation\", every step is done: do not evaluate again, but go on with the steps for after you have finished editing, recording their edits as step 6 says, and call this tool with the stage 'closing' when you have performed them.\n";

    // The edits recorded under "# Changes After the Evaluation", which the closing checks the scenarios against
    private static final String RECORDED_EDITS = "one line for each edit that adds, changes, deletes or renames a model element or a relationship - not one that only rewords a definition or a note, or only changes a presentation - saying what changed and giving the IDs of the elements it touches";

    // What the read_scenarios tool tells while the evaluation is under way, so that it is resumed after a context compression
    private static final String EVALUATION_UNDER_WAY = """
The architecture scenario evaluation and improvement against the scenarios has begun and has not been handed over yet.
- If you are a subagent - an agent launched by another agent, not the one the user asked - do nothing about it: the agent that launched you carries it out.
- If the Request under "# Work" is the request you are working on, and the steps that the architecture_scenario_evaluation_steps tool returned are no longer in your context - after a context compression, for example - call that tool with the stage 'evaluation' again, and resume its steps from the first one whose output is missing.
""";

    // What the read_scenarios tool tells once the evaluation has been handed over, so that the edits after it are still recorded, and it is still closed, after a context compression
    private static final String EVALUATION_HANDED_OVER = """
The architecture scenario evaluation and improvement against the scenarios has been handed over to the steps for after you have finished editing, and has not been closed.
- If you are a subagent - an agent launched by another agent, not the one the user asked - do nothing about it: the agent that launched you records your edits and closes the evaluation.
- If the Request under "# Work" is the request you are working on, then, until you close the evaluation, write under "# Changes After the Evaluation" of the evaluation results file at {RESULTS_FILE} {RECORDED_EDITS}, such as "- Renamed: BankService -> BankGateway (7e4d-...)". Record the edits of your subagents too. When you have performed the steps for after you have finished editing, call the architecture_scenario_evaluation_steps tool with the stage 'closing', unless you are closing the evaluation already, and perform the steps it returns.
""".replace("{RECORDED_EDITS}", RECORDED_EDITS);

    private static final String UNRESOLVED_TRADE_OFF = "   - Unresolved trade-off: the scenario is named under \"# Unresolved Trade-offs\", and resolving its NG would make the other scenario of the pair NG. Which of the two comes first is for the user to decide.\n";

    private static final String OUTSIDE_THE_REQUEST = "   - Outside the request: the NG could only be resolved by changing parts of the existing architecture that the Request under \"# Work\" does not concern. The request does not ask for that work; the scenario is reported as an existing issue.\n";

    private static final String NOT_EVALUATED_SUBAGENT_NOTE = "- If you are a subagent - an agent launched by another agent, not the one the user asked - do nothing.";

    private ArchitectureScenarioEvaluationSteps() {
    }

    // No scenario file: skip only when the work was nothing but work that does not need the architecture scenario analysis; otherwise develop the scenarios now, whether their development was left out or its files were lost
    static String notDeveloped(ArchitectureScenarioFiles files) {
        String contents = """
No scenario file exists at {SCENARIO_FILE}, so there is nothing to evaluate the architecture against. What to do depends on the work you have just done.
- If you are a subagent - an agent launched by another agent, not the one the user asked - do nothing: the agent that launched you carries out the evaluation.
- If the work was only one or more of the following, skip the architecture scenario evaluation and improvement, and go on with your work.
{WORK_WITHOUT_ANALYSIS}
- If the work created or changed an architecture, including a change to one you had recovered from source code, the scenarios are missing - either the architecture scenario development was left out, or the working directory was taken out of use: Astah or this MCP server restarted, or another piece of work set it aside. Do not skip the evaluation. Call the architecture_scenario_development_steps tool with the type of the work you have just done, and carry out its steps even though the architecture already exists, writing "After the architecture work" for "Developed". If you developed scenarios before the architecture work and still have them, write those same scenarios, and do not revise them to fit the architecture. If you have none, develop them from the sources that step 2 of those steps names, not from the architecture you have made. Then call this tool again with the stage 'evaluation'.
""".replace("{WORK_WITHOUT_ANALYSIS}", ArchitectureScenarioDevelopmentSteps.lines("  - ", ArchitectureScenarioDevelopmentSteps.WORK_WITHOUT_ANALYSIS))
            .replace("{SCENARIO_FILE}", files.scenarioFile().toString());

        return contents;
    }

    // A scenario file that does not hold everything: finish developing the scenarios first, unless the file is another's
    static String developmentInProgress(ArchitectureScenarioFiles files, ArchitectureScenarioFiles.Snapshot snapshot) {
        String contents = """
A scenario file exists at {SCENARIO_FILE}, but its scenarios are not fully developed yet. It was last modified {MINUTES} minute(s) ago, and it says the following about the piece of work it belongs to.

{HEAD}

What is missing, with the step of architecture scenario development that writes it:
{PROBLEMS}

- If you are a subagent - an agent launched by another agent, not the one the user asked - do nothing: the agent that launched you carries out the evaluation.
- If you wrote the file yourself in the piece of work you have just done, call the architecture_scenario_development_steps tool with the type of that work, finish developing the scenarios, and then call this tool again with the stage 'evaluation'.
- Otherwise it was left behind by another piece of work. {IF_LEFT_BEHIND}
""".replace("{IF_LEFT_BEHIND}", ArchitectureScenarioDevelopmentSteps.IF_LEFT_BEHIND)
            .replace("{MINUTES}", Long.toString(snapshot.minutesSinceLastModified()))
            .replace("{SCENARIO_FILE}", files.scenarioFile().toString())
            .replace("{PROBLEMS}", ArchitectureScenarioDevelopmentSteps.lines("- ", snapshot.check().problems()))
            .replace("{HEAD}", ArchitectureScenarioDevelopmentSteps.quoted(snapshot.check().head()));

        return contents;
    }

    // A finished scenario file: every step of the evaluation, for the type of work the file names
    static String developed(ArchitectureScenarioFiles files, ArchitectureScenarioFiles.Snapshot snapshot) {
        WorkType workType = snapshot.check().workType().orElseThrow();

        return creationOrChange(workType)
            .replace("{IF_LEFT_BEHIND}", ArchitectureScenarioDevelopmentSteps.IF_LEFT_BEHIND)
            .replace("{EXISTING_FILES_NOTE}", files.evaluationFileExists() ? EXISTING_FILES_NOTE : "")
            .replace("{MINUTES}", Long.toString(snapshot.minutesSinceLastModified()))
            .replace("{RESULTS_FILE}", files.evaluationResultsFile().toString())
            .replace("{IMPROVEMENT_FILE}", files.architectureImprovementsFile().toString());
    }

    // An architecture created or changed: it is improved where it falls short, and handed over to the steps for after editing, after which the evaluation is closed
    private static String creationOrChange(WorkType workType) {
        return """
# Architecture Scenario Evaluation and Improvement After Creating or Changing an Architecture
Follow these steps to evaluate the architecture you have just created or changed against the scenarios you have developed, and to improve it where it falls short. Do not skip, reorder, or merge steps.

{SUBAGENT_NOTE}

Working files. Write them in UTF-8 with your file-writing tool, never with shell redirection, and write all of their contents in English.
Use the paths exactly as they are given here. Do not reuse a path from earlier in the conversation.
- Evaluation results file: {RESULTS_FILE}
- Improvement file: {IMPROVEMENT_FILE}
{EXISTING_FILES_NOTE}
1. Load the scenarios and confirm that they are yours.
   Call the read_architecture_scenarios tool, even if you read the scenarios earlier.
   The scenario file was last modified {MINUTES} minute(s) ago. Confirm that you wrote it yourself in the piece of work you have just done, and that the Request under "# Work" is the request you worked on. If that is not so, it was left behind by another piece of work. Do not evaluate against it. {IF_LEFT_BEHIND}

2. Map the scenarios onto the architecture.
   Map each concrete scenario, one at a time, onto the architecture you created or changed: find the elements of the model that the scenario involves, and what each of them does in it. Read the model through the tools of this MCP server - what each element is responsible for, and how it is related to the others - and do NOT map a scenario by matching its words with element names or with diagram images.
   - A scenario in which the system is used is mapped by following it from its Situation to its Response: each part of it falls to an element that is responsible for that part, and it passes from each element to the next over a relationship, an interface or a message that the model has.
   - A scenario in which the system is changed - replacing a notification mechanism, for example - is mapped by listing what the change adds, changes or deletes - elements, relationships and interfaces - together with the elements that would have to change in turn because they depend on something that changes.
   Judge each scenario by its mapping, against its Measure: the mapping has to show that the Response is achieved to the extent the Measure states. For a scenario in which the system is changed, the list has to stay within the Measure.
   Assign exactly one verdict to each concrete scenario:
   - OK: the scenario can be mapped, and its mapping meets the Measure.
   - NG: the scenario cannot be mapped - a part of a scenario in which the system is used falls to no element responsible for it, or passes between two elements over nothing the model has - or its mapping does not meet the Measure. Give the reason.
   - NOT-EVALUABLE: the architecture does not contain the information the verdict needs - for example, a performance scenario against an architecture that says nothing about timing or deployment. NOT-EVALUABLE is allowed ONLY for missing information, never because the evaluation is laborious. State what information is missing.
   When only part of a scenario can be judged - it can be mapped, but its Measure sets a condition, on the number of screens or on the response time for example, that the mapping cannot show - the verdict is NG if the part that can be judged fails, and NOT-EVALUABLE otherwise. Write the mapping you could make under "Mapping" and what is missing under "Reason or missing information".
   Write the results to the evaluation results file in the format below: a table with a row for each concrete scenario, and after it a section for each. Every concrete scenario MUST have a row and a section; silently omitting one is forbidden. Keep the cells of the table short: the elements and the mapping belong in the section.
   Under "Elements", list every element the mapping involves - a class, a relationship, a lifeline, a message and the like - as its name followed by its ID in parentheses, as the tools of this MCP server return them, and a relationship without a name as what it connects followed by its ID. Under "Mapping", say what each of those elements does in the scenario - in the order in which the scenario goes through them, for a scenario in which the system is used, or what the change does to it, for one in which the system is changed - and how the Measure holds or fails.

   > # Evaluation Results
   > OK: <count> / NG: <count> / NOT-EVALUABLE: <count>
   >
   > | ID | Stakeholder | Quality attribute | Verdict | Reason or missing information |
   > |---|---|---|---|---|
   > | S-01 | ... | ... | OK | |
   >
   > ## S-01
   > - Elements: <every element the mapping involves, each as Name (ID)>
   > - Mapping: <what each element does in the scenario, in order, or what the change does to it, and how the Measure holds or fails>

3. Work out the improvements.
   Referring to the evaluation results file, decide for each NG scenario whether an improvement is to be worked out for it.
   Do not work out an improvement in the following cases. List these scenarios in the improvement file under "# NG Scenarios Left Without an Improvement", each with its reason.
{NO_IMPROVEMENT_CASES}   For every other NG scenario, work out the change to the architecture that resolves it, and write it to the improvement file in the format below, one section per improvement.
   An improvement must not turn an OK scenario into NG, and the improvements must not contradict one another. A NOT-EVALUABLE scenario is not a reason to change the architecture.
   For each improvement, state whether it changes or deletes anything of the existing architecture - an element, a relationship or a diagram that existed before you began the current work, as opposed to one you created in this work - and name what it changes or deletes. What you recovered from source code earlier in the same request counts as existing architecture, not as something you created.
   If you worked out no improvement, write the line "No improvement." under the "# Architecture Improvements" heading, and skip steps 4 and 5.

   > # Architecture Improvements
   > ## I-01: <title of the improvement>
   > - Resolves: S-02, S-05
   > - Problem: <where the current architecture fails the scenarios, and why>
   > - Change: <the elements, relationships and diagrams to add, change or delete, each existing one as Name (ID)>
   > - Changes existing architecture: <No, or Yes: the existing elements and diagrams it changes or deletes>
   > - Effect on other scenarios: <the effect on the scenarios that were OK, or None>
   > - Status: <filled in in step 4: Applied, or Not approved>
   > - Added: <filled in in step 4: the elements and relationships the applied improvement added, each as Name (ID), or None>
   >
   > # NG Scenarios Left Without an Improvement
   > - S-04: Unresolved trade-off with S-02
{OUTSIDE_THE_REQUEST_EXAMPLE}
4. Improve the architecture.
   An improvement that leaves the existing architecture as it is - one that only adds to, or only changes, what you created in the current work, which does not include what you recovered from source code - is applied without asking the user.
   An improvement that changes or deletes anything of the existing architecture MUST be confirmed by the user before it is applied: present it to the user with the scenarios it resolves and what it changes or deletes, and apply it only if the user approves it. If you cannot get an answer from the user, do not apply it.
   Record in the improvement file, for each improvement, whether it was applied or not approved, and, under "Added", what an applied one added, each as Name (ID).
   Change the architecture as the applied improvements say. These edits do not call for developing scenarios again, nor for running these steps again.
   If no improvement was applied, skip step 5.

5. Re-evaluate the scenarios that the improvements touched.
   Map each of the following scenarios, and no other, onto the improved architecture once more, as step 2 says. Make each mapping afresh, reading the model through the tools again, instead of adjusting the mapping of step 2.
   - A scenario whose verdict in step 2 was NG and that an applied improvement was meant to resolve.
   - A scenario whose verdict in step 2 was OK and that an applied improvement touched: one named under "Effect on other scenarios" of an applied improvement, or one whose Elements include an element that an applied improvement changed or deleted, or an element at either end of a relationship that an applied improvement added, changed or deleted. Tell these by the IDs under "Elements" and under "Change", not by names.
   Assign each the verdict OK, NG or NOT-EVALUABLE by the rules of step 2. Append the results to the evaluation results file in the format below, with a section for each scenario as in step 2.

   > # Re-evaluation Results
   > | ID | Verdict in step 2 | Why re-evaluated | Verdict | Reason or missing information |
   > |---|---|---|---|---|
   > | S-02 | NG | Meant to be resolved by I-01 | OK | |
   > | S-05 | OK | I-01 changes BankService (7e4d-...) | OK | |
   >
   > ## S-02
   > - Elements: <every element the mapping involves, each as Name (ID)>
   > - Mapping: <what each element does in the scenario now, in order, or what the change does to it, and how the Measure holds or fails>

   Do this once only. Do not improve the architecture again for a scenario that is still NG, nor for one that an improvement has turned from OK to NG: both are reported when you close the evaluation. Which comes first, a scenario turned NG or the scenarios the improvement resolves, is for the user to decide.

6. Hand over to the steps for after you have finished editing.
   Do not report the results to the user yet, and do not set the working directory aside: you do both when you close the evaluation.
   First call the read_architecture_scenarios tool, and correct the evaluation results file and the improvement file as each item under "evaluationProblems" says, until it lists none.
   Then add the heading "# Changes After the Evaluation" to the end of the evaluation results file. While you perform the steps for after you have finished editing, write under it {RECORDED_EDITS}, in the format below. Record the edits of your subagents too.

   > # Changes After the Evaluation
   > - Renamed: BankService -> BankGateway (7e4d-...)
   > - Added: RemoteBankGateway (9a0f-...), realizing BankGateway (7e4d-...), for CLSxSEQ-01
   > - Changed: the type of the lifeline bank (3c5e-...) from BankGateway (7e4d-...) to RemoteBankGateway (9a0f-...)

   Then go on with the steps for after you have finished editing. When you have performed them, call this tool with the stage 'closing'.
""".replace("{SUBAGENT_NOTE}", SUBAGENT_NOTE)
            .replace("{RECORDED_EDITS}", RECORDED_EDITS)
            .replace("{NO_IMPROVEMENT_CASES}", UNRESOLVED_TRADE_OFF + (workType == WorkType.CHANGE ? OUTSIDE_THE_REQUEST : ""))
            .replace("{OUTSIDE_THE_REQUEST_EXAMPLE}", workType == WorkType.CHANGE ? "   > - S-07: Outside the request - <the existing parts that would have to change>\n" : "");
    }

    // A finished scenario file and the results of its evaluation: the closing, for the type of work the file names
    static String closing(ArchitectureScenarioFiles files, ArchitectureScenarioFiles.Snapshot snapshot) {
        WorkType workType = snapshot.check().workType().orElseThrow();

        return """
# Closing the Architecture Scenario Evaluation and Improvement
Follow these steps to check the scenarios against the edits made after the evaluation, to set the working files aside, and to report the results. Do not skip, reorder, or merge steps. These steps do not edit the model or the diagrams.

{SUBAGENT_NOTE}

Working files. Write them in UTF-8 with your file-writing tool, never with shell redirection, and write all of their contents in English.
Use the paths exactly as they are given here. Do not reuse a path from earlier in the conversation.
- Scenario file: {SCENARIO_FILE}
- Evaluation results file: {RESULTS_FILE}
- Improvement file: {IMPROVEMENT_FILE}

1. Load the working files and confirm that they are yours.
   Call the read_architecture_scenarios tool. It returns the scenario file, the evaluation results file and the improvement file. Confirm that the Request under "# Work" is the request you have worked on. If it is not, these files were left behind by another piece of work, and the files of your own evaluation have been lost. Do not report from them. {IF_LEFT_BEHIND}
   If it lists anything under "evaluationProblems" or "architectureDecisionProblems", correct the files as each item says - mapping the scenario onto the model again where its result is missing - and call it again, until it lists nothing there.

2. Find the names the elements have now.
   Collect the IDs under "Elements" in the evaluation results file, under "Change" and "Added" in the improvement file, under "# Changes After the Evaluation", and under "# Architecture Decisions" in the scenario file. Read the elements of these IDs through the tools of this MCP server - one mcp tool script can read them all - and note the name each has now. An ID that the model no longer has is that of an element that has been deleted, or deleted and made again under another ID.

3. Check the scenarios against the changes made after the evaluation.
   Map onto the architecture once more, reading the model through the tools, each scenario whose verdict so far - that of the re-evaluation where there is one, and that of the first evaluation otherwise - is OK and whose Elements in that evaluation include an element that a line under "# Changes After the Evaluation" touches, or that step 2 found no longer in the model. Map it as the evaluation did, afresh rather than by adjusting the earlier mapping: follow a scenario in which the system is used from its Situation to its Response through the elements responsible for each part and the relationships, interfaces or messages between them, and list for a scenario in which the system is changed what the change adds, changes or deletes and what would have to change in turn. A rename alone does not call for mapping a scenario again, since its Elements name each element by its ID. If the evaluation results file has no "# Changes After the Evaluation", or you cannot be sure that it records every such edit - after a context compression, for example - map every scenario whose verdict so far is OK again.
   Assign each the verdict OK, NG or NOT-EVALUABLE by the rules of the evaluation. Append the results to the evaluation results file in the format below, with a section for each scenario as in the evaluation, or the heading followed by the line "No scenario was touched."

   > # Post-Editing Check Results
   > | ID | Why checked | Verdict | Reason or missing information |
   > |---|---|---|---|
   > | S-01 | The type of the lifeline bank (3c5e-...) changed | OK | |
   >
   > ## S-01
   > - Elements: <every element the mapping involves, each as Name (ID)>
   > - Mapping: <what each element does in the scenario now, in order, or what the change does to it, and how the Measure holds or fails>

   Do not change the architecture for a scenario that is NG here. It is reported with the change that made it NG, and whether to keep that change is for the user to decide.

4. Set the working files aside.
   Call the set_aside_architecture_scenario_dir tool. It returns under "keptDirectoryPath" where the working files are kept. If it reports an error, nothing has been changed: call it once more, and if it fails again, go on, and tell the user in your report what the error says and where the working directory is.

5. Report.
   Include all of the following in your report to the user, in the language the user uses.
   Give every element the name it has now, as step 2 found it. Where a scenario, a reason or an improvement uses an earlier name of an element, give the name it has now, followed by the earlier name in parentheses. For an element that has been deleted, give the name the files give it, and say that it was deleted.
   - Where the working files are kept: the path that step 4 returned, and that they are deleted when Astah or this MCP server stops.
   - What the scenarios were developed for: the Type and the Request under "# Work". When "Developed" says "After the architecture work", say that the scenarios were written after the architecture and are weaker evidence for that reason.
   - Every concrete scenario, with its ID, a one-line summary of it and its final verdict - the last verdict it was given, in step 3 of these steps, in the re-evaluation, or in the first evaluation - marking the ones that are assumed, and the number of scenarios of each final verdict.
   - For every scenario that is still NG: its reason, and which of these it is - {NG_KINDS}.
   - For every NOT-EVALUABLE scenario: the information that is missing.
   - Every improvement you applied: its title, the scenarios it resolves, and the elements and diagrams it added, changed or deleted.
   - The scenarios that step 3 mapped again, and why.
   - Every decision under "# Architecture Decisions" whose scenarios are all assumed, and every one that serves no scenario, with the elements it concerns. Say that these decisions rest on assumptions the user has not confirmed, or on no scenario at all. If the scenario file has no "# Architecture Decisions", say that no decision was recorded.
   - Every entry under "# Trade-off Resolutions" and under "# Unresolved Trade-offs". Say of the unresolved ones that the user has to decide which scenario comes first.
""".replace("{SUBAGENT_NOTE}", SUBAGENT_NOTE)
            .replace("{IF_LEFT_BEHIND}", ArchitectureScenarioDevelopmentSteps.IF_LEFT_BEHIND)
            .replace("{NG_KINDS}", ngKinds(workType))
            .replace("{SCENARIO_FILE}", files.scenarioFile().toString())
            .replace("{RESULTS_FILE}", files.evaluationResultsFile().toString())
            .replace("{IMPROVEMENT_FILE}", files.architectureImprovementsFile().toString());
    }

    // No scenario file at the close: there is nothing to close, or the files of the evaluation have been taken out of use
    static String nothingToClose(ArchitectureScenarioFiles files) {
        List<Path> setAside = files.setAsideDirectories();
        String setAsideDirectories = setAside.isEmpty()
                ? "No working directory set aside earlier is left.\n"
                : "The working directories set aside earlier, newest first. Yours is the one whose scenario file names under \"# Work\" the request you worked on.\n"
                        + ArchitectureScenarioDevelopmentSteps.lines("- ", setAside.stream().map(Path::toString).toList()) + "\n";

        return """
No scenario file exists at {SCENARIO_FILE}, so there is no evaluation to close.
{SUBAGENT_NOTE}
- If you have not evaluated an architecture against scenarios in this work, go on with your work.
- If you have, its working files are no longer in use: you set them aside yourself in step 4 of the closing, or another piece of work set them aside, or Astah or this MCP server restarted and they were lost. If you set them aside yourself, only the report of the closing is left: make it from the files where they are kept. Otherwise report the results you still have, as fully as you can, reading them from your files where they are kept if they are, and say that the working files were taken out of use before the closing and that the scenarios were not checked against the edits made after the evaluation.

{SET_ASIDE_DIRECTORIES}""".replace("{SUBAGENT_NOTE}", NOT_EVALUATED_SUBAGENT_NOTE)
            .replace("{SET_ASIDE_DIRECTORIES}", setAsideDirectories)
            .replace("{SCENARIO_FILE}", files.scenarioFile().toString());
    }

    // A finished scenario file but no evaluation results at the close: the evaluation was left out, unless the file is another's
    static String notEvaluated(ArchitectureScenarioFiles files, ArchitectureScenarioFiles.Snapshot snapshot) {
        return """
A scenario file exists at {SCENARIO_FILE}, but there is no evaluation results file at {RESULTS_FILE}: no architecture has been evaluated against these scenarios. The scenario file was last modified {MINUTES} minute(s) ago, and it says the following about the piece of work it belongs to.

{HEAD}

{SUBAGENT_NOTE}
- If you wrote the file yourself in the piece of work you are doing now, the evaluation was left out. Call this tool with the stage 'evaluation' and carry out its steps, perform the steps for after you have finished editing again for the edits its improvements make, and then call this tool with the stage 'closing' again.
- Otherwise it was left behind by another piece of work. {IF_LEFT_BEHIND}
""".replace("{SUBAGENT_NOTE}", NOT_EVALUATED_SUBAGENT_NOTE)
            .replace("{IF_LEFT_BEHIND}", ArchitectureScenarioDevelopmentSteps.IF_LEFT_BEHIND)
            .replace("{MINUTES}", Long.toString(snapshot.minutesSinceLastModified()))
            .replace("{SCENARIO_FILE}", files.scenarioFile().toString())
            .replace("{RESULTS_FILE}", files.evaluationResultsFile().toString())
            .replace("{HEAD}", ArchitectureScenarioDevelopmentSteps.quoted(snapshot.check().head()));
    }

    // What the read_scenarios tool tells about the evaluation, by how far it has got; empty before it has begun
    static String evaluationGuidance(ArchitectureScenarioFiles files) {
        if (!files.evaluationFileExists()) {
            return "";
        }

        String guidance = files.evaluationHandedOver() ? EVALUATION_HANDED_OVER : EVALUATION_UNDER_WAY;
        return guidance.replace("{RESULTS_FILE}", files.evaluationResultsFile().toString());
    }

    // The kinds of scenario that are still NG at the close, which differ with the type of work
    private static String ngKinds(WorkType workType) {
        return switch (workType) {
            case NEW -> "NG again in step 5 of the evaluation, turned NG by an improvement, turned NG after the evaluation, improvement not approved, or unresolved trade-off";
            case CHANGE -> "NG again in step 5 of the evaluation, turned NG by an improvement, turned NG after the evaluation, improvement not approved, unresolved trade-off, or outside the request";
        };
    }
}
