package com.astahpromcp.tool.scenario;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationFileCheckTest {

    // The scenarios of the complete scenario file of ScenarioFileCheckTest
    static final List<String> IDS = List.of("S-01", "S-02");

    // An evaluation results file that holds everything step 2 of the evaluation writes, for the complete scenario file
    static final String RESULTS = """
# Evaluation Results
OK: 2 / NG: 0 / NOT-EVALUABLE: 0

| ID | Stakeholder | Quality attribute | Verdict | Reason or missing information |
|---|---|---|---|---|
| S-01 | Member | Usability | OK | |
| S-02 | Developer | Modifiability | OK | |

## S-01
- Elements: LendingDesk (1a2b-0123456789abcdef0123456789abcdef), LoanRecord (3c4d-0123456789abcdef0123456789abcdef)
- Mapping: LendingDesk takes the request, and LoanRecord records the loan.

## S-02
- Elements: Notifier (5e6f-0123456789abcdef0123456789abcdef)
- Mapping: Replacing the reminder channel changes Notifier alone.
""";

    // The improvement file of an evaluation that found nothing to improve
    static final String NO_IMPROVEMENT = """
# Architecture Improvements
No improvement.
""";

    // The results with S-02 NG
    static final String RESULTS_WITH_NG = RESULTS
            .replace("OK: 2 / NG: 0", "OK: 1 / NG: 1")
            .replace("| S-02 | Developer | Modifiability | OK | |", "| S-02 | Developer | Modifiability | NG | Reminders are sent by LendingDesk itself. |");

    // An improvement that resolves S-02, as it is after step 4 of the evaluation
    static final String IMPROVEMENT = """
# Architecture Improvements
## I-01: Send reminders through a gateway
- Resolves: S-02
- Problem: Reminders are sent by LendingDesk itself.
- Change: Add ReminderGateway, used by LendingDesk (1a2b-0123456789abcdef0123456789abcdef)
- Changes existing architecture: No
- Effect on other scenarios: None
- Status: Applied
- Added: ReminderGateway (7a8b-0123456789abcdef0123456789abcdef)
""";

    private static List<String> problemsOf(String results, String improvements, boolean handedOver) {
        return EvaluationFileCheck.check(results, improvements, IDS, handedOver);
    }

    private static void assertOneProblem(List<String> problems, String expectedText) {
        assertEquals(1, problems.size(), problems.toString());
        assertTrue(problems.get(0).contains(expectedText), problems.toString());
    }

    @Test
    void check_ok_findsNothingWrongWithCompleteFiles() {
        assertEquals(List.of(), problemsOf(RESULTS, null, false));
        assertEquals(List.of(), problemsOf(RESULTS, NO_IMPROVEMENT, false));
        assertEquals(List.of(), problemsOf(RESULTS + "\n# Changes After the Evaluation\n- Renamed: Notifier -> ReminderSender (5e6f-0123456789abcdef0123456789abcdef)\n", NO_IMPROVEMENT, true));
        assertEquals(List.of(), problemsOf(RESULTS_WITH_NG, IMPROVEMENT, true));
        assertEquals(List.of(), problemsOf(null, null, false));
    }

    @Test
    void check_ng_reportsAMissingSectionAndAMissingRow() {
        assertOneProblem(problemsOf("Nothing yet.\n", null, false), "The evaluation results file has no \"# Evaluation Results\" section (step 2 of the evaluation).");

        String withoutS02 = """
# Evaluation Results
OK: 1 / NG: 0 / NOT-EVALUABLE: 0

| ID | Stakeholder | Quality attribute | Verdict | Reason or missing information |
|---|---|---|---|---|
| S-01 | Member | Usability | OK | |

## S-01
- Elements: LendingDesk (1a2b-0123456789abcdef0123456789abcdef)
- Mapping: LendingDesk takes the request.
""";
        assertOneProblem(problemsOf(withoutS02, null, false), "The table under \"# Evaluation Results\" has no row for these scenarios: S-02 (step 2 of the evaluation).");

        // A row whose verdict cannot be read is still a row
        List<String> withAnotherVerdict = problemsOf(RESULTS.replace("OK: 2 /", "OK: 1 /").replace("| S-02 | Developer | Modifiability | OK | |", "| S-02 | Developer | Modifiability | Fine | |"), null, false);
        assertOneProblem(withAnotherVerdict, "the verdict of these scenarios is not one of OK, NG and NOT-EVALUABLE: S-02");
    }

    @Test
    void check_ng_reportsCountsThatDisagreeWithTheTableOrAreMissing() {
        assertOneProblem(problemsOf(RESULTS.replace("OK: 2 / NG: 0", "OK: 1 / NG: 1"), null, false),
                "The line \"OK: 1 / NG: 1 / NOT-EVALUABLE: 0\" under \"# Evaluation Results\" does not agree with the verdicts in its table, which are OK: 2 / NG: 0 / NOT-EVALUABLE: 0");
        assertOneProblem(problemsOf(RESULTS.replace("OK: 2 / NG: 0 / NOT-EVALUABLE: 0\n", ""), null, false),
                "lacks the line \"OK: <count> / NG: <count> / NOT-EVALUABLE: <count>\"");
        // The counts are read in the forms an agent writes them
        assertEquals(List.of(), problemsOf(RESULTS.replace("OK: 2 / NG: 0 / NOT-EVALUABLE: 0", "**OK: 2 / NG: 0 / NOT EVALUABLE: 0**"), null, false));
    }

    @Test
    void check_ng_reportsAVerdictThatIsNoneOfTheThreeAndAnNgWithoutAReason() {
        assertTrue(problemsOf(RESULTS.replace("| S-01 | Member | Usability | OK | |", "| S-01 | Member | Usability | PASS | |"), null, false).stream()
                .anyMatch(problem -> problem.contains("the verdict of these scenarios is not one of OK, NG and NOT-EVALUABLE: S-01")));

        List<String> withoutReason = problemsOf(RESULTS
                .replace("OK: 2 / NG: 0", "OK: 1 / NG: 1")
                .replace("| S-02 | Developer | Modifiability | OK | |", "| S-02 | Developer | Modifiability | **NG** | |"), null, false);
        assertOneProblem(withoutReason, "these NG or NOT-EVALUABLE scenarios leave \"Reason or missing information\" empty: S-02");
    }

    @Test
    void check_ng_reportsARowForAScenarioThatDoesNotExistAndARowWrittenTwice() {
        String withUnknown = RESULTS.replace("| S-02 | Developer | Modifiability | OK | |", "| S-02 | Developer | Modifiability | OK | |\n| S-09 | Developer | Modifiability | OK | |\n| S-01 | Member | Usability | OK | |");

        List<String> problems = problemsOf(withUnknown, null, false);

        assertTrue(problems.stream().anyMatch(problem -> problem.contains("has rows for these scenario IDs, which \"# Concrete Scenarios\" does not have: S-09")), problems.toString());
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("has more than one row for these scenarios: S-01")), problems.toString());
    }

    @Test
    void check_ng_reportsAScenarioWithoutItsSectionElementsOrMapping() {
        String withoutSection = RESULTS.substring(0, RESULTS.indexOf("## S-02"));
        assertOneProblem(problemsOf(withoutSection, null, false), "these scenarios have a row in the table but no section such as \"## S-02\" after it with their \"Elements\" and \"Mapping\": S-02 (step 2 of the evaluation).");

        String withoutIds = RESULTS.replace("- Elements: Notifier (5e6f-0123456789abcdef0123456789abcdef)", "- Elements: Notifier, as S-02 (S-02) needs");
        assertOneProblem(problemsOf(withoutIds, null, false), "\"Elements\" of these scenarios is missing or names no element with its ID in parentheses, such as \"Idle (44rp-...)\": S-02");

        String withoutMapping = RESULTS.replace("- Mapping: Replacing the reminder channel changes Notifier alone.", "- Mapping: ...");
        assertOneProblem(problemsOf(withoutMapping, null, false), "\"Mapping\" of these scenarios is missing or empty: S-02");
    }

    @Test
    void check_ok_letsANotEvaluableScenarioNameNoElement() {
        String notEvaluable = RESULTS
                .replace("OK: 2 / NG: 0 / NOT-EVALUABLE: 0", "OK: 1 / NG: 0 / NOT-EVALUABLE: 1")
                .replace("| S-02 | Developer | Modifiability | OK | |", "| S-02 | Developer | Modifiability | NOT-EVALUABLE | The architecture says nothing about how reminders are sent. |")
                .replace("- Elements: Notifier (5e6f-0123456789abcdef0123456789abcdef)", "- Elements: None");

        assertEquals(List.of(), problemsOf(notEvaluable, null, false));
    }

    @Test
    void check_ok_readsTheSectionsInTheFormsAnAgentWritesThem() {
        String written = RESULTS
                .replace("## S-01\n", "## S-01: Borrowing at the counter\n")
                .replace("- Elements: LendingDesk (1a2b-0123456789abcdef0123456789abcdef), LoanRecord (3c4d-0123456789abcdef0123456789abcdef)",
                        "- **Elements**:\n  - LendingDesk (1a2b-0123456789abcdef0123456789abcdef)\n  - LoanRecord (3c4d-0123456789abcdef0123456789abcdef)")
                .replace("- Mapping: Replacing", "- **Mapping:** Replacing");

        assertEquals(List.of(), problemsOf(written, null, false));
    }

    @Test
    void check_ng_takesTheMappingInATableCellForAMissingSection() {
        // The format before the elements were given a section of their own
        String inTheTable = """
# Evaluation Results
OK: 2 / NG: 0 / NOT-EVALUABLE: 0

| ID | Stakeholder | Quality attribute | Verdict | Mapping (elements involved, each as Name (ID)) | Reason or missing information |
|---|---|---|---|---|---|
| S-01 | Member | Usability | OK | LendingDesk (1a2b-0123456789abcdef0123456789abcdef) | |
| S-02 | Developer | Modifiability | OK | Notifier (5e6f-0123456789abcdef0123456789abcdef) | |
""";

        assertOneProblem(problemsOf(inTheTable, null, false), "no section such as \"## S-01\" after it with their \"Elements\" and \"Mapping\": S-01, S-02");
    }

    @Test
    void check_ng_checksTheReEvaluationAndThePostEditingCheckLikeTheEvaluation() {
        String reEvaluation = RESULTS_WITH_NG + """

# Re-evaluation Results
| ID | Verdict in step 2 | Why re-evaluated | Verdict | Reason or missing information |
|---|---|---|---|---|
| S-02 | NG | Meant to be resolved by I-01 | OK | |
""";
        assertOneProblem(problemsOf(reEvaluation, IMPROVEMENT, true), "Under \"# Re-evaluation Results\", these scenarios have a row in the table but no section such as \"## S-02\" after it with their \"Elements\" and \"Mapping\": S-02 (step 5 of the evaluation).");
        assertEquals(List.of(), problemsOf(reEvaluation + "\n## S-02\n- Elements: ReminderGateway (7a8b-0123456789abcdef0123456789abcdef)\n- Mapping: LendingDesk sends reminders through ReminderGateway.\n", IMPROVEMENT, true));

        String untouched = RESULTS + "\n# Changes After the Evaluation\n\n# Post-Editing Check Results\nNo scenario was touched.\n";
        assertEquals(List.of(), problemsOf(untouched, NO_IMPROVEMENT, true));

        String checked = RESULTS + """

# Post-Editing Check Results
| ID | Why checked | Verdict | Reason or missing information |
|---|---|---|---|
| S-01 | LoanRecord (3c4d-...) changed | Fine | |
""";
        List<String> problems = problemsOf(checked, NO_IMPROVEMENT, true);
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("In the table under \"# Post-Editing Check Results\", the verdict of these scenarios is not one of OK, NG and NOT-EVALUABLE: S-01 (step 3 of the closing).")), problems.toString());
    }

    @Test
    void check_ng_reportsAnImprovementFileThatAccountsForNothingOrIsMissing() {
        assertOneProblem(problemsOf(RESULTS, "# Something Else\n", false), "The improvement file has no \"# Architecture Improvements\" section (step 3 of the evaluation).");
        assertOneProblem(problemsOf(RESULTS, "# Architecture Improvements\n\n", false), "holds neither an improvement such as \"## I-01: <title of the improvement>\" nor the line \"No improvement.\"");

        // Step 3 writes the improvement file, so it is missing only while the evaluation is under way
        assertEquals(List.of(), problemsOf(RESULTS, null, false));
        assertOneProblem(problemsOf(RESULTS, null, true), "The improvement file does not exist");
    }

    @Test
    void check_ng_reportsAnNgScenarioThatNoImprovementResolvesAndThatIsNotLeftWithAReason() {
        assertOneProblem(problemsOf(RESULTS_WITH_NG, NO_IMPROVEMENT, false), "These NG scenarios are neither named under \"Resolves\" of an improvement nor listed under \"# NG Scenarios Left Without an Improvement\": S-02 (step 3 of the evaluation).");

        String leftWithout = NO_IMPROVEMENT + "\n# NG Scenarios Left Without an Improvement\n- S-02: Unresolved trade-off with S-01\n";
        assertEquals(List.of(), problemsOf(RESULTS_WITH_NG, leftWithout, true));
    }

    @Test
    void check_ng_asksForTheStatusOfEachImprovementOnlyOnceTheEvaluationIsHandedOver() {
        String beforeStep4 = IMPROVEMENT
                .replace("- Status: Applied", "- Status: <filled in in step 4: Applied, or Not approved>")
                .replace("- Added: ReminderGateway (7a8b-0123456789abcdef0123456789abcdef)", "- Added: <filled in in step 4: the elements and relationships the applied improvement added, each as Name (ID), or None>");

        assertEquals(List.of(), problemsOf(RESULTS_WITH_NG, beforeStep4, false));
        assertOneProblem(problemsOf(RESULTS_WITH_NG, beforeStep4, true), "These improvements have no \"Status\" of Applied or Not approved: I-01 (step 4 of the evaluation).");

        String appliedWithoutAdded = IMPROVEMENT.replace("- Added: ReminderGateway (7a8b-0123456789abcdef0123456789abcdef)", "- Added:");
        assertOneProblem(problemsOf(RESULTS_WITH_NG, appliedWithoutAdded, true), "These applied improvements leave \"Added\" missing or empty");

        String notApproved = IMPROVEMENT.replace("- Status: Applied", "- Status: **Not approved**").replace("- Added: ReminderGateway (7a8b-0123456789abcdef0123456789abcdef)", "- Added: None");
        assertEquals(List.of(), problemsOf(RESULTS_WITH_NG, notApproved, true));
    }

    @Test
    void check_ng_reportsAHeadingWrittenTwice() {
        List<String> problems = problemsOf(RESULTS + "\n# Evaluation Results\n", null, false);

        assertTrue(problems.contains("The heading \"# Evaluation Results\" appears more than once: write the section once, under a single heading (step 2 of the evaluation)."), problems.toString());
    }
}
