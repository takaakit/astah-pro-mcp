package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.scenario.inputdto.WorkType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ScenarioFileCheckTest {

    // A scenario file that holds everything the architecture scenario development steps write
    static final String COMPLETE = """
# Work
- Type: New
- Request: Create the architecture of a library lending system.
- Developed: Before the architecture work

# System of Interest
Library lending system - lends book copies to members.

# Concrete Scenarios
- Member
  - Usability
    - [S-01] Situation: A member borrows a book copy at the counter. Response: The loan is recorded. Measure: One element takes the request and one records the loan.
- Developer
  - Modifiability
    - [S-02] Situation: A developer replaces the reminder channel. Response: Reminders go out through the new channel. Measure: Only the notification component is changed. (assumed)

# Trade-off Matrix
| ID | Usability | Modifiability |
|---|---|---|
| S-01 | o | . |
| S-02 | - | o |

# Abstract Scenario
## Summary
The system guarantees to members and developers that lending stays simple and open to change.

## Narrative
Members borrow what they need with little effort. Developers exchange a part of the system without disturbing the rest. The system stays simple to use while it changes.

## Key Quality Attributes
- Usability: Lending takes the fewest steps. (S-01)
- Modifiability: A part can be exchanged alone. (S-02)
""";

    // The trade-off matrix of the complete file, without its heading
    private static final String MATRIX = "| ID | Usability | Modifiability |\n|---|---|---|\n| S-01 | o | . |\n| S-02 | - | o |\n";

    private static List<String> problemsOf(String contents) {
        return ScenarioFileCheck.check(contents).problems();
    }

    // The complete file with another trade-off matrix
    private static String withMatrix(String matrix) {
        assertTrue(COMPLETE.contains(MATRIX), "the complete file has changed its matrix");
        return COMPLETE.replace(MATRIX, matrix);
    }

    // Exactly one problem, which holds the given text
    private static void assertOneProblem(String contents, String expectedText) {
        List<String> problems = problemsOf(contents);
        assertEquals(1, problems.size(), problems.toString());
        assertTrue(problems.get(0).contains(expectedText), problems.toString());
    }

    // The Situation of S-02, narrowed to settle its conflict with S-01
    private static final String NARROWED = "A developer replaces the reminder channel while no reminder is being sent.";

    // The entry that records the narrowing, as the steps show it
    private static final String ENTRY = """
- S-02 (conflicted with S-01)
  - Before: A developer replaces the reminder channel.
  - After: A developer replaces the reminder channel while no reminder is being sent.
  - No longer covers: Replacing the channel while reminders are going out.
""";

    // The complete file with S-02 narrowed, and the given section under "# Trade-off Resolutions"
    private static String withResolutions(String section) {
        assertTrue(COMPLETE.contains("Situation: A developer replaces the reminder channel."), "the complete file has changed the Situation of S-02");
        return COMPLETE
                .replace("Situation: A developer replaces the reminder channel.", "Situation: " + NARROWED)
                .replace("# Abstract Scenario\n", "# Trade-off Resolutions\n" + section + "\n# Abstract Scenario\n");
    }

    @Test
    void check_ok_findsNothingMissingInACompleteFile() {
        ScenarioFileCheck.Result result = ScenarioFileCheck.check(COMPLETE);

        assertEquals(List.of(), result.problems());
        assertTrue(result.complete());
        assertEquals(Optional.of(WorkType.NEW), result.workType());
    }

    @Test
    void check_ok_returnsTheSectionsThatSayWhoseFileItIs() {
        String head = ScenarioFileCheck.check(COMPLETE).head();

        assertEquals("""
# Work
- Type: New
- Request: Create the architecture of a library lending system.
- Developed: Before the architecture work

# System of Interest
Library lending system - lends book copies to members.""", head);
    }

    @Test
    void check_ok_readsEveryWorkTypeWithoutRegardToCase() {
        assertEquals(Optional.of(WorkType.CHANGE), ScenarioFileCheck.check(COMPLETE.replace("Type: New", "Type: change")).workType());
        assertEquals(List.of(), problemsOf(COMPLETE.replace("Type: New", "Type: CHANGE")));
        assertEquals(List.of(), problemsOf(COMPLETE.replace("Before the architecture work", "After the architecture work")));
    }

    @Test
    void check_ng_doesNotTakeReverseEngineeringForAWorkType() {
        String reverseEngineering = COMPLETE.replace("Type: New", "Type: Reverse-engineering");

        assertEquals(Optional.empty(), ScenarioFileCheck.check(reverseEngineering).workType());
        assertOneProblem(reverseEngineering, "\"Type\" under \"# Work\" is missing or is neither New nor Change (step 1).");
    }

    @Test
    void check_ok_toleratesAByteOrderMarkAndHeadingsInAnotherCase() {
        assertEquals(List.of(), problemsOf("﻿" + COMPLETE));
        assertEquals(List.of(), problemsOf(COMPLETE.replace("# Trade-off Matrix", "# Trade-Off matrix").replace("## Key Quality Attributes", "## Key quality attributes")));
    }

    @Test
    void check_ng_reportsAMissingWorkSection() {
        String withoutWork = COMPLETE.substring(COMPLETE.indexOf("# System of Interest"));

        ScenarioFileCheck.Result result = ScenarioFileCheck.check(withoutWork);

        assertEquals(Optional.empty(), result.workType());
        assertEquals(1, result.problems().size(), result.problems().toString());
        assertTrue(result.problems().get(0).contains("The \"# Work\" section is missing"), result.problems().toString());
    }

    @Test
    void check_ng_reportsWhatTheWorkSectionLacks() {
        assertOneProblem(COMPLETE.replace("Type: New", "Type: Refactoring"), "\"Type\" under \"# Work\"");
        assertEquals(Optional.empty(), ScenarioFileCheck.check(COMPLETE.replace("Type: New", "Type: Refactoring")).workType());

        assertOneProblem(COMPLETE.replace("- Request: Create the architecture of a library lending system.\n", ""), "\"Request\" under \"# Work\"");
        // The placeholder of the steps left as it is
        assertOneProblem(COMPLETE.replace("Request: Create the architecture of a library lending system.", "Request: <one line saying what the user asked for>"), "\"Request\" under \"# Work\"");

        assertOneProblem(COMPLETE.replace("Developed: Before the architecture work", "Developed: Yes"), "\"Developed\" under \"# Work\"");
    }

    @Test
    void check_ng_reportsAnEmptySystemOfInterest() {
        assertOneProblem(COMPLETE.replace("Library lending system - lends book copies to members.\n", ""), "\"# System of Interest\" section is missing or empty");
    }

    @Test
    void check_ng_reportsAFileWithoutAScenario() {
        String withoutScenarios = COMPLETE.substring(0, COMPLETE.indexOf("- Member")) + "- Member\n- Developer\n";

        List<String> problems = problemsOf(withoutScenarios);

        assertTrue(problems.stream().anyMatch(problem -> problem.contains("holds no concrete scenario")), problems.toString());
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("\"# Trade-off Matrix\" section is missing")), problems.toString());
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("\"# Abstract Scenario\" section is missing")), problems.toString());
    }

    @Test
    void check_ng_reportsAScenarioIdUsedTwice() {
        List<String> problems = problemsOf(COMPLETE.replace("[S-02] Situation", "[S-01] Situation"));

        assertTrue(problems.stream().anyMatch(problem -> problem.contains("used more than once") && problem.contains("S-01")), problems.toString());
    }

    @Test
    void check_ng_reportsAScenarioWithoutItsThreeParts() {
        assertOneProblem(COMPLETE.replace(" Measure: Only the notification component is changed.", ""), "lack \"Situation:\", \"Response:\" or \"Measure:\", or do not give them in this order: S-02");
    }

    @Test
    void check_ng_reportsAScenarioWhosePartsAreOutOfOrder() {
        String outOfOrder = COMPLETE.replace(
                "Situation: A member borrows a book copy at the counter. Response: The loan is recorded.",
                "Response: The loan is recorded. Situation: A member borrows a book copy at the counter.");

        assertOneProblem(outOfOrder, "lack \"Situation:\", \"Response:\" or \"Measure:\", or do not give them in this order: S-01 (step 4).");
    }

    @Test
    void check_ng_reportsAScenarioThatLeavesAPartEmpty() {
        assertOneProblem(COMPLETE.replace("Response: The loan is recorded.", "Response:"), "leave \"Situation:\", \"Response:\" or \"Measure:\" empty: S-01 (step 4).");
        // The placeholder of the steps left as it is, and nothing but the mark of an assumption, are empty as well
        assertOneProblem(COMPLETE.replace("Response: The loan is recorded.", "Response: ..."), "empty: S-01");
        assertOneProblem(COMPLETE.replace("Measure: Only the notification component is changed. (assumed)", "Measure: (assumed)"), "empty: S-02");
    }

    @Test
    void check_ok_readsLabelsWithEmphasisMarks() {
        String emphasized = COMPLETE
                .replace("Situation: A member", "**Situation**: A member")
                .replace("Response: The loan", "**Response:** The loan")
                .replace("Measure: One element", "__Measure__: One element");

        assertEquals(List.of(), problemsOf(emphasized));
    }

    @Test
    void check_ng_reportsAScenarioThatIsNotUnderAQualityAttribute() {
        assertOneProblem(COMPLETE.replace("    - [S-02]", "  - [S-02]"), "not at the third level of the list, under a quality attribute under a stakeholder: S-02 (steps 2 to 4).");
    }

    @Test
    void check_ok_readsAListIndentedWithTabs() {
        String tabs = COMPLETE.replace("\n  - ", "\n\t- ").replace("\n    - ", "\n\t\t- ");

        assertEquals(List.of(), problemsOf(tabs));
    }

    @Test
    void check_ok_readsAScenarioWrittenOverSeveralLines() {
        assertEquals(List.of(), problemsOf(COMPLETE.replace(" Response: The loan is recorded. Measure:", "\n      Response: The loan is recorded.\n      Measure:")));
    }

    @Test
    void check_ng_reportsAScenarioWithoutAMatrixRow() {
        assertOneProblem(COMPLETE.replace("| S-02 | - | o |\n", ""), "no row for these scenarios: S-02");
    }

    @Test
    void check_ng_reportsAnEmptyMatrixCellAsOneThatHasNotBeenConsidered() {
        assertOneProblem(COMPLETE.replace("| S-01 | o | . |", "| S-01 | o | |"), "empty cells, which are cells that have not been considered, in the rows of these scenarios: S-01");
        // A row that stops short of the header leaves cells out as well
        assertOneProblem(COMPLETE.replace("| S-02 | - | o |", "| S-02 | - |"), "empty cells, which are cells that have not been considered, in the rows of these scenarios: S-02");
    }

    @Test
    void check_ng_reportsAMatrixCellThatHoldsAnotherValue() {
        assertOneProblem(COMPLETE.replace("| S-02 | - | o |", "| S-02 | + | o |"), "something other than \"o\", \"-\" and \".\" in the rows of these scenarios: S-02");
    }

    @Test
    void check_ng_reportsAMatrixWithoutAHeaderRow() {
        assertOneProblem(withMatrix("|---|---|---|\n| S-01 | o | . |\n| S-02 | - | o |\n"), "no header row that names the quality attributes (step 5).");
    }

    @Test
    void check_ng_reportsColumnsThatDoNotMatchTheQualityAttributes() {
        assertOneProblem(withMatrix("| ID | Usability |\n|---|---|\n| S-01 | o |\n| S-02 | - |\n"),
                "no column for these quality attributes listed under \"# Concrete Scenarios\": Modifiability (step 5).");
        assertOneProblem(withMatrix("| ID | Usability | Modifiability | Portability |\n|---|---|---|---|\n| S-01 | o | . | . |\n| S-02 | - | o | . |\n"),
                "columns that name no quality attribute listed under \"# Concrete Scenarios\": Portability (step 5).");
        assertOneProblem(withMatrix("| ID | Usability | Modifiability | usability |\n|---|---|---|---|\n| S-01 | o | . | . |\n| S-02 | - | o | - |\n"),
                "more than one column for these quality attributes: usability (step 5).");
    }

    @Test
    void check_ng_reportsRowsThatDoNotMatchTheScenarios() {
        assertOneProblem(withMatrix(MATRIX + "| S-02 | - | o |\n"), "more than one row for these scenarios: S-02 (step 5).");
        assertOneProblem(withMatrix(MATRIX + "| S-99 | . | . |\n"), "rows for these scenario IDs, which \"# Concrete Scenarios\" does not have: S-99 (step 5).");
        assertOneProblem(withMatrix(MATRIX.replace("| S-01 | o | . |", "| S-01 | o | . | . |")), "more cells than columns in the rows of these scenarios: S-01 (step 5).");
    }

    @Test
    void check_ng_reportsAnOThatIsNotInTheColumnOfTheScenario() {
        assertOneProblem(withMatrix(MATRIX.replace("| S-02 | - | o |", "| S-02 | o | - |")),
                "does not hold \"o\" in the column of the quality attribute the scenario belongs to, or holds it in another column as well: S-02 (step 5).");
        assertOneProblem(withMatrix(MATRIX.replace("| S-01 | o | . |", "| S-01 | o | o |")), "or holds it in another column as well: S-01 (step 5).");
    }

    @Test
    void check_ok_takesAResolutionWhoseAfterIsTheSituation() {
        assertEquals(List.of(), problemsOf(withResolutions(ENTRY)));
        // A section that records no revision
        assertEquals(List.of(), problemsOf(withResolutions("- None. No conflict or contradiction was found.\n")));
    }

    @Test
    void check_ng_reportsAResolutionWhoseAfterIsNotTheSituation() {
        assertOneProblem(withResolutions(ENTRY.replace("while no reminder is being sent.", "at night.")),
                "the \"After:\" of the last entry for each of these scenarios is not the Situation the scenario has under \"# Concrete Scenarios\", and the two have to be the same: S-02 (step 5).");
        // The scenario corrected later, and the entry left as it was
        assertOneProblem(withResolutions(ENTRY).replace("Situation: " + NARROWED, "Situation: A developer replaces the reminder channel at night."),
                "and the two have to be the same: S-02");
    }

    @Test
    void check_ng_reportsAResolutionThatNamesAScenarioThatDoesNotExist() {
        assertOneProblem(withResolutions(ENTRY.replace("conflicted with S-01", "conflicted with S-09")),
                "The entries under \"# Trade-off Resolutions\" name these scenario IDs, which \"# Concrete Scenarios\" does not have: S-09 (step 5).");
        assertOneProblem(withResolutions(ENTRY.replace("- S-02 (", "- S-07 (")), "does not have: S-07 (step 5).");
    }

    @Test
    void check_ng_reportsAResolutionWithoutAnAfter() {
        String after = "  - After: " + NARROWED + "\n";

        assertOneProblem(withResolutions(ENTRY.replace(after, "")), "the last entry for each of these scenarios lacks \"After:\" or leaves it empty: S-02 (step 5).");
        // The placeholder of the steps left as it is
        assertOneProblem(withResolutions(ENTRY.replace(after, "  - After: <the Situation as revised>\n")), "lacks \"After:\" or leaves it empty: S-02");
    }

    @Test
    void check_ok_readsResolutionsInTheFormsAnAgentWritesThem() {
        for (String section : List.of(
                // Emphasis marks
                ENTRY.replace("- S-02 (", "- **[S-02]** (").replace("  - After:", "  - **After:**"),
                // Quotation marks, another case and no full stop
                ENTRY.replace("After: " + NARROWED, "After: \"a developer replaces the reminder channel while NO reminder is being sent\""),
                // On one line, without indentation, and with tabs
                ENTRY.replace("\n  - ", " "),
                ENTRY.replace("\n  - ", "\n- "),
                ENTRY.replace("\n  - ", "\n\t- "),
                // A part written over two lines
                ENTRY.replace("channel while no reminder", "channel while\n    no reminder"))) {
            assertEquals(List.of(), problemsOf(withResolutions(section)), section);
        }
    }

    @Test
    void check_ok_comparesOnlyTheLastEntryOfAScenarioRevisedTwice() {
        String first = """
- S-02 (conflicted with S-01)
  - Before: A developer replaces the reminder channel.
  - After: A developer replaces the reminder channel at night.
  - No longer covers: Replacing the channel in the daytime.
""";
        String second = ENTRY.replace("Before: A developer replaces the reminder channel.", "Before: A developer replaces the reminder channel at night.");

        assertEquals(List.of(), problemsOf(withResolutions(first + second)));
        assertOneProblem(withResolutions(second + first), "and the two have to be the same: S-02");
    }

    @Test
    void check_ng_reportsTheMissingPartsOfTheAbstractScenario() {
        assertOneProblem(COMPLETE.replace("## Narrative\n", ""), "lacks these parts: \"## Narrative\"");
    }

    @Test
    void check_ng_reportsAScenarioIdTheKeyQualityAttributesLeaveOut() {
        // Naming the ID in the narrative does not count
        String leftOut = COMPLETE
                .replace("The system stays simple to use while it changes.", "The system stays simple to use while it changes (S-02).")
                .replace("- Modifiability: A part can be exchanged alone. (S-02)\n", "");

        assertOneProblem(leftOut, "do not appear under \"## Key Quality Attributes\": S-02");
    }

    @Test
    void check_ng_reportsAnEmptyPartOfTheAbstractScenario() {
        assertOneProblem(COMPLETE.replace("The system guarantees to members and developers that lending stays simple and open to change.\n", ""),
                "These parts of the \"# Abstract Scenario\" section are empty: \"## Summary\" (step 6).");
        assertOneProblem(COMPLETE.replace("Members borrow what they need with little effort. Developers exchange a part of the system without disturbing the rest. The system stays simple to use while it changes.", "<one paragraph>"),
                "are empty: \"## Narrative\" (step 6).");
    }

    @Test
    void check_ng_takesThePlaceholdersOfTheStepsForEmptyValues() {
        assertOneProblem(COMPLETE.replace("Request: Create the architecture of a library lending system.", "Request: ..."), "\"Request\" under \"# Work\"");
        assertOneProblem(COMPLETE.replace("Library lending system - lends book copies to members.", "<the name of the system of interest, and a one-line description of it>"),
                "\"# System of Interest\" section is missing or empty");
    }

    @Test
    void check_ng_reportsAHeadingWrittenTwiceAndReadsTheSectionsUnderItAsOne() {
        // The stakeholders under one heading and the scenarios under another, as an agent may write them: the scenarios are read all the same
        String twice = COMPLETE.replace("# Concrete Scenarios\n- Member\n", "# Concrete Scenarios\n- Member\n\n# Concrete Scenarios\n- Member\n");

        assertOneProblem(twice, "The heading \"# Concrete Scenarios\" appears more than once: write the section once, under a single heading (steps 2 to 4).");
        assertOneProblem(COMPLETE.replace("## Narrative\n", "## Narrative\nMembers come first.\n\n## Narrative\n"), "The heading \"## Narrative\" appears more than once");
    }

    @Test
    void check_ng_reportsEveryDefectOfAFileThatOnceWentThrough() {
        // The check once took this file for finished, defects and all
        String defects = """
# Work
- Type: New
- Request: Review test of the scenario file check (dummy)
- Developed: Before the architecture work

# System of Interest
TestSys - a dummy system for checking the scenario file check

# Concrete Scenarios
- Operator
  - performance
    - [S-01] Situation: Response: Measure:
  - security
    - [S-02] Situation: x Response: y Measure: z

# Trade-off Matrix
| ID | usability | portability |
|---|---|---|
| S-01 | . | . | o | o |
| S-02 | - | - |
| S-02 | o | . |
| S-99 | o | o |

# Abstract Scenario
## Summary

## Narrative

## Key Quality Attributes
- performance (S-01)
- security (S-02)
""";

        List<String> problems = problemsOf(defects);

        for (String expected : List.of(
                "leave \"Situation:\", \"Response:\" or \"Measure:\" empty: S-01",
                "no column for these quality attributes listed under \"# Concrete Scenarios\": performance, security",
                "columns that name no quality attribute listed under \"# Concrete Scenarios\": usability, portability",
                "more than one row for these scenarios: S-02",
                "rows for these scenario IDs, which \"# Concrete Scenarios\" does not have: S-99",
                "more cells than columns in the rows of these scenarios: S-01",
                "are empty: \"## Summary\", \"## Narrative\"")) {
            assertTrue(problems.stream().anyMatch(problem -> problem.contains(expected)), expected + " is not among " + problems);
        }
    }

    @Test
    void check_ok_doesNotTakeAHeadingInACodeBlockForASection() {
        String onlyInCodeBlock = "```markdown\n" + COMPLETE + "```\n";

        List<String> problems = problemsOf(onlyInCodeBlock);

        assertTrue(problems.stream().anyMatch(problem -> problem.contains("The \"# Work\" section is missing")), problems.toString());
    }

    @Test
    void unreadable_ok_carriesTheProblemAndNoWorkType() {
        ScenarioFileCheck.Result result = ScenarioFileCheck.unreadable("The scenario file is not valid UTF-8.");

        assertFalse(result.complete());
        assertEquals(List.of("The scenario file is not valid UTF-8."), result.problems());
        assertEquals(Optional.empty(), result.workType());
        assertEquals(List.of(), result.scenarioIds());
        assertEquals(List.of(), result.decisionProblems());
    }

    // The complete file with the given section under "# Architecture Decisions" at its end, as step 9 of the architecture scenario development has it written
    static String withDecisions(String section) {
        return COMPLETE + "\n# Architecture Decisions\n" + section;
    }

    @Test
    void check_ok_returnsTheIdsOfTheConcreteScenarios() {
        assertEquals(List.of("S-01", "S-02"), ScenarioFileCheck.check(COMPLETE).scenarioIds());
    }

    @Test
    void check_ok_takesWellFormedDecisionsWithoutAProblem() {
        ScenarioFileCheck.Result result = ScenarioFileCheck.check(withDecisions("""
- D-01: Record loans in LoanRecord (3c4d-0123456789abcdef0123456789abcdef) alone (S-01)
- **D-02**: Send reminders through a Notifier interface, so that the channel can be replaced (S-01, S-02).
- D-03: Keep the counter screen in one diagram (none)
- D-04: Split the catalog
  into a search part and a holding part (S-02)
"""));

        assertEquals(List.of(), result.problems());
        assertEquals(List.of(), result.decisionProblems());
    }

    @Test
    void check_ng_reportsWhatIsWrongWithTheDecisionsWithoutKeepingTheScenariosFromBeingFullyDeveloped() {
        ScenarioFileCheck.Result result = ScenarioFileCheck.check(withDecisions("""
- D-01: Record loans in LoanRecord alone (S-01)
- D-01: Record returns in LoanRecord too (S-01)
- D-02: Send reminders through a Notifier interface
- D-03: Keep the counter screen in one diagram (S-09)
- D-04: (S-02)
- Split the catalog (S-02)
"""));

        assertTrue(result.complete(), "the decisions are written after the scenarios are fully developed: " + result.problems());
        List<String> problems = result.decisionProblems();
        for (String expected : List.of(
                "are not decisions in the form \"- D-01: <the decision> (S-01, S-02)\": Split the catalog (S-02) (step 9).",
                "These decision IDs are used more than once under \"# Architecture Decisions\": D-01 (step 9).",
                "do not end with the IDs of the scenarios they serve in parentheses, or with \"(none)\": D-02 (step 9).",
                "name these scenario IDs, which \"# Concrete Scenarios\" does not have: S-09 (step 9).",
                "do not say what was decided: D-04 (step 9).")) {
            assertTrue(problems.stream().anyMatch(problem -> problem.contains(expected)), expected + " is not among " + problems);
        }
        assertEquals(5, problems.size(), problems.toString());
    }

    @Test
    void check_ng_reportsADecisionHeadingWrittenTwice() {
        List<String> problems = ScenarioFileCheck.check(withDecisions("- D-01: Record loans alone (S-01)\n\n# Architecture Decisions\n- D-02: Notify through an interface (S-02)\n")).decisionProblems();

        assertEquals(List.of("The heading \"# Architecture Decisions\" appears more than once: write the section once, under a single heading (step 9)."), problems);
    }
}
