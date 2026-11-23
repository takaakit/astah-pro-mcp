package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.scenario.ScenarioFileCheck.KnownHeading;
import com.astahpromcp.tool.scenario.ScenarioFileCheck.Section;
import com.astahpromcp.tool.scenario.ScenarioFileCheck.Sections;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Checks whether the evaluation results file and the improvement file hold what the later steps of the evaluation and its closing rely on
final class EvaluationFileCheck {

    private static final Pattern COUNTS = Pattern.compile("OK\\s*:\\s*(\\d+)\\s*/\\s*NG\\s*:\\s*(\\d+)\\s*/\\s*NOT[- ]EVALUABLE\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    // An element ID in parentheses, as the tools of this MCP server return it, such as "(44rp-e75a4d1972be7e8157f0837cf6adf774)"
    private static final Pattern ELEMENT_ID = Pattern.compile("\\(([^()\\s]+-[^()\\s]+)\\)");
    // The ID of a scenario, a decision or an improvement, which is no element ID
    private static final Pattern OTHER_ID = Pattern.compile("[SDI]-\\d+", Pattern.CASE_INSENSITIVE);
    // A "- Label: value" item, with the emphasis marks that Markdown may put around the label
    private static final Pattern FIELD = Pattern.compile("^(\\s*)[-*+]\\s+[*_]{0,2}([A-Za-z][A-Za-z ]*?)[*_]{0,2}\\s*:[*_]{0,2}\\s*(.*)$");
    // The scenario or the improvement at the head of a section title, as in "S-01", "S-01: Borrowing" and "I-01: Notify through a gateway"
    private static final Pattern SCENARIO_TITLE = Pattern.compile("^[*_`\\[]*(s-\\d+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMPROVEMENT_TITLE = Pattern.compile("^[*_`\\[]*(i-\\d+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NO_IMPROVEMENT = Pattern.compile("^\\s*[*_]*no improvements?\\.?[*_]*\\s*$", Pattern.CASE_INSENSITIVE);
    private static final List<String> VERDICTS = List.of("OK", "NG", "NOT-EVALUABLE");

    // The sections the evaluation and its closing write, with the steps that write them
    private static final List<KnownHeading> RESULTS_HEADINGS = List.of(
            new KnownHeading("Evaluation Results", "step 2 of the evaluation"),
            new KnownHeading("Re-evaluation Results", "step 5 of the evaluation"),
            new KnownHeading("Changes After the Evaluation", "step 6 of the evaluation"),
            new KnownHeading("Post-Editing Check Results", "step 3 of the closing"));
    private static final List<KnownHeading> IMPROVEMENT_HEADINGS = List.of(
            new KnownHeading("Architecture Improvements", "step 3 of the evaluation"),
            new KnownHeading("NG Scenarios Left Without an Improvement", "step 3 of the evaluation"));

    private EvaluationFileCheck() {
    }

    // A row of a table of results: the verdict, in upper case, as written; null when it is none of the three
    private record Row(String verdict) {
    }

    // What is wrong with the files. A file that does not exist is null. The scenario IDs are those of "# Concrete Scenarios"; the improvements need a status only once the evaluation has been handed over, since step 4 fills it in.
    static List<String> check(String results, String improvements, List<String> scenarioIds, boolean handedOver) {
        List<String> problems = new ArrayList<>();
        Set<String> ngIds = new LinkedHashSet<>();

        if (results != null) {
            checkResults(results, scenarioIds, ngIds, problems);
        }
        if (improvements != null) {
            checkImprovements(improvements, ngIds, handedOver, problems);
        } else if (handedOver) {
            problems.add("The improvement file does not exist: step 3 of the evaluation writes it, with the line \"No improvement.\" under \"# Architecture Improvements\" when it works out none.");
        }
        return List.copyOf(problems);
    }

    private static void checkResults(String contents, List<String> scenarioIds, Set<String> ngIds, List<String> problems) {
        Sections sections = ScenarioFileCheck.split(lines(contents), 1);
        ScenarioFileCheck.checkRepeatedHeadings(sections, "# ", RESULTS_HEADINGS, problems);

        Section evaluation = sections.get("evaluation results");
        if (evaluation == null) {
            problems.add("The evaluation results file has no \"# Evaluation Results\" section (step 2 of the evaluation).");
        } else {
            Map<String, Row> rows = checkTable(evaluation, "# Evaluation Results", scenarioIds, true, "step 2 of the evaluation", problems);
            List<String> withoutRow = scenarioIds.stream().filter(id -> !rows.containsKey(id)).toList();
            if (!rows.isEmpty() && !withoutRow.isEmpty()) {
                problems.add("The table under \"# Evaluation Results\" has no row for these scenarios: " + String.join(", ", withoutRow) + " (step 2 of the evaluation).");
            }
            checkCounts(evaluation, rows, problems);
            rows.forEach((id, row) -> {
                if ("NG".equals(row.verdict())) {
                    ngIds.add(id);
                }
            });
        }

        Section reEvaluation = sections.get("re-evaluation results");
        if (reEvaluation != null) {
            checkTable(reEvaluation, "# Re-evaluation Results", scenarioIds, true, "step 5 of the evaluation", problems);
        }

        // The closing writes "No scenario was touched." in place of a table when it maps no scenario again
        Section postEditingCheck = sections.get("post-editing check results");
        if (postEditingCheck != null) {
            checkTable(postEditingCheck, "# Post-Editing Check Results", scenarioIds, false, "step 3 of the closing", problems);
        }
    }

    // The table of a section of results, and the section for each of its scenarios. Returns the rows whose verdicts can be read, by scenario ID.
    private static Map<String, Row> checkTable(Section section, String heading, List<String> scenarioIds, boolean tableRequired, String step, List<String> problems) {
        Map<String, Row> rows = new LinkedHashMap<>();
        List<List<String>> table = beforeSubsections(section.body()).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("|"))
                .map(ScenarioFileCheck::cells)
                .filter(row -> !ScenarioFileCheck.isSeparatorRow(row))
                .toList();

        List<String> header = table.stream()
                .filter(row -> !row.isEmpty() && !isScenarioId(row.get(0)))
                .findFirst()
                .orElse(null);
        if (header == null) {
            if (tableRequired || !table.isEmpty()) {
                problems.add("The \"" + heading + "\" section holds no table with a header row (" + step + ").");
            }
            return rows;
        }

        int verdictColumn = columnOf(header, name -> name.equals("verdict"));
        int reasonColumn = columnOf(header, name -> name.startsWith("reason"));
        if (verdictColumn < 0) {
            problems.add("The table under \"" + heading + "\" has no \"Verdict\" column (" + step + ").");
            return rows;
        }

        Set<String> unknown = new LinkedHashSet<>();
        Set<String> twice = new LinkedHashSet<>();
        Set<String> withAnotherVerdict = new LinkedHashSet<>();
        Set<String> withoutReason = new LinkedHashSet<>();
        for (List<String> row : table) {
            if (row.isEmpty() || !isScenarioId(row.get(0))) {
                continue;
            }

            String id = scenarioIdOf(row.get(0));
            if (!scenarioIds.isEmpty() && !scenarioIds.contains(id)) {
                unknown.add(id);
            }
            if (rows.containsKey(id)) {
                twice.add(id);
                continue;
            }

            String verdict = cellAt(row, verdictColumn).replaceAll("[*_`]", "").trim().replaceAll("\\s+", "-").toUpperCase(Locale.ROOT);
            if (!VERDICTS.contains(verdict)) {
                withAnotherVerdict.add(id);
                rows.put(id, new Row(null));
                continue;
            }
            if (!verdict.equals("OK") && (reasonColumn < 0 || ScenarioFileCheck.isEmptyValue(cellAt(row, reasonColumn)))) {
                withoutReason.add(id);
            }
            rows.put(id, new Row(verdict));
        }

        if (rows.isEmpty() && tableRequired) {
            problems.add("The table under \"" + heading + "\" has no row for a scenario (" + step + ").");
        }
        if (!unknown.isEmpty()) {
            problems.add("The table under \"" + heading + "\" has rows for these scenario IDs, which \"# Concrete Scenarios\" does not have: " + String.join(", ", unknown) + " (" + step + ").");
        }
        if (!twice.isEmpty()) {
            problems.add("The table under \"" + heading + "\" has more than one row for these scenarios: " + String.join(", ", twice) + " (" + step + ").");
        }
        if (!withAnotherVerdict.isEmpty()) {
            problems.add("In the table under \"" + heading + "\", the verdict of these scenarios is not one of OK, NG and NOT-EVALUABLE: " + String.join(", ", withAnotherVerdict) + " (" + step + ").");
        }
        if (!withoutReason.isEmpty()) {
            problems.add("In the table under \"" + heading + "\", these NG or NOT-EVALUABLE scenarios leave \"Reason or missing information\" empty: " + String.join(", ", withoutReason) + " (" + step + ").");
        }

        checkScenarioSections(section, heading, rows, step, problems);
        return rows;
    }

    // The section "## S-01" after the table for each scenario that has a row: its elements, each with its ID, and its mapping
    private static void checkScenarioSections(Section section, String heading, Map<String, Row> rows, String step, List<String> problems) {
        Sections subsections = ScenarioFileCheck.split(section.body(), 2);
        Map<String, Section> byId = new LinkedHashMap<>();
        Set<String> twice = new LinkedHashSet<>();
        subsections.byTitle().forEach((title, subsection) -> {
            Matcher scenario = SCENARIO_TITLE.matcher(title);
            if (scenario.lookingAt()) {
                String id = scenario.group(1).toUpperCase(Locale.ROOT);
                if (byId.putIfAbsent(id, subsection) != null || subsections.repeated().contains(title)) {
                    twice.add(id);
                }
            }
        });

        List<String> withoutSection = new ArrayList<>();
        List<String> withoutElementIds = new ArrayList<>();
        List<String> withoutMapping = new ArrayList<>();
        rows.forEach((id, row) -> {
            Section subsection = byId.get(id);
            if (subsection == null) {
                withoutSection.add(id);
                return;
            }
            Map<String, String> fields = fields(subsection.body());
            String mapping = fields.get("mapping");
            if (mapping == null || ScenarioFileCheck.isEmptyValue(mapping)) {
                withoutMapping.add(id);
            }
            // A scenario the architecture says too little about may have no element to name
            if (!"NOT-EVALUABLE".equals(row.verdict()) && !hasElementId(fields.get("elements"))) {
                withoutElementIds.add(id);
            }
        });

        if (!withoutSection.isEmpty()) {
            problems.add("Under \"" + heading + "\", these scenarios have a row in the table but no section such as \"## " + withoutSection.get(0) + "\" after it with their \"Elements\" and \"Mapping\": " + String.join(", ", withoutSection) + " (" + step + ").");
        }
        if (!twice.isEmpty()) {
            problems.add("Under \"" + heading + "\", these scenarios have more than one section: " + String.join(", ", twice) + " (" + step + ").");
        }
        if (!withoutElementIds.isEmpty()) {
            problems.add("Under \"" + heading + "\", \"Elements\" of these scenarios is missing or names no element with its ID in parentheses, such as \"Idle (44rp-...)\": " + String.join(", ", withoutElementIds) + " (" + step + ").");
        }
        if (!withoutMapping.isEmpty()) {
            problems.add("Under \"" + heading + "\", \"Mapping\" of these scenarios is missing or empty: " + String.join(", ", withoutMapping) + " (" + step + ").");
        }
    }

    // The line of counts under "# Evaluation Results" against the verdicts in its table
    private static void checkCounts(Section evaluation, Map<String, Row> rows, List<String> problems) {
        Matcher counts = COUNTS.matcher(String.join("\n", beforeSubsections(evaluation.body())).replaceAll("[*_`]", ""));
        if (!counts.find()) {
            problems.add("The \"# Evaluation Results\" section lacks the line \"OK: <count> / NG: <count> / NOT-EVALUABLE: <count>\" (step 2 of the evaluation).");
            return;
        }

        List<Long> written = List.of(Long.parseLong(counts.group(1)), Long.parseLong(counts.group(2)), Long.parseLong(counts.group(3)));
        List<Long> inTable = VERDICTS.stream()
                .map(verdict -> rows.values().stream().filter(row -> verdict.equals(row.verdict())).count())
                .toList();
        if (!written.equals(inTable)) {
            problems.add("The line \"OK: " + written.get(0) + " / NG: " + written.get(1) + " / NOT-EVALUABLE: " + written.get(2) + "\" under \"# Evaluation Results\" does not agree with the verdicts in its table, which are OK: " + inTable.get(0) + " / NG: " + inTable.get(1) + " / NOT-EVALUABLE: " + inTable.get(2) + " (step 2 of the evaluation).");
        }
    }

    private static void checkImprovements(String contents, Set<String> ngIds, boolean handedOver, List<String> problems) {
        Sections sections = ScenarioFileCheck.split(lines(contents), 1);
        ScenarioFileCheck.checkRepeatedHeadings(sections, "# ", IMPROVEMENT_HEADINGS, problems);

        Map<String, Map<String, String>> improvements = new LinkedHashMap<>();
        Section section = sections.get("architecture improvements");
        if (section == null) {
            problems.add("The improvement file has no \"# Architecture Improvements\" section (step 3 of the evaluation).");
        } else {
            ScenarioFileCheck.split(section.body(), 2).byTitle().forEach((title, improvement) -> {
                Matcher id = IMPROVEMENT_TITLE.matcher(title);
                if (id.lookingAt()) {
                    improvements.putIfAbsent(id.group(1).toUpperCase(Locale.ROOT), fields(improvement.body()));
                }
            });
            boolean noImprovement = section.body().stream().anyMatch(line -> NO_IMPROVEMENT.matcher(line).matches());
            if (improvements.isEmpty() && !noImprovement) {
                problems.add("The \"# Architecture Improvements\" section holds neither an improvement such as \"## I-01: <title of the improvement>\" nor the line \"No improvement.\" (step 3 of the evaluation).");
            }
        }

        Set<String> accountedFor = new LinkedHashSet<>();
        improvements.values().forEach(fields -> accountedFor.addAll(scenarioIdsIn(fields.get("resolves"))));
        Section leftWithout = sections.get("ng scenarios left without an improvement");
        if (leftWithout != null) {
            accountedFor.addAll(scenarioIdsIn(String.join("\n", leftWithout.body())));
        }
        List<String> unaccounted = ngIds.stream().filter(id -> !accountedFor.contains(id)).toList();
        if (!unaccounted.isEmpty()) {
            problems.add("These NG scenarios are neither named under \"Resolves\" of an improvement nor listed under \"# NG Scenarios Left Without an Improvement\": " + String.join(", ", unaccounted) + " (step 3 of the evaluation).");
        }

        if (!handedOver) {
            return;
        }
        List<String> withoutStatus = new ArrayList<>();
        List<String> withoutAdded = new ArrayList<>();
        improvements.forEach((id, fields) -> {
            String status = fields.get("status");
            String normalized = status == null || ScenarioFileCheck.isEmptyValue(status) ? "" : ScenarioFileCheck.normalize(status);
            if (normalized.startsWith("applied")) {
                String added = fields.get("added");
                if (added == null || ScenarioFileCheck.isEmptyValue(added)) {
                    withoutAdded.add(id);
                }
            } else if (!normalized.startsWith("not approved")) {
                withoutStatus.add(id);
            }
        });
        if (!withoutStatus.isEmpty()) {
            problems.add("These improvements have no \"Status\" of Applied or Not approved: " + String.join(", ", withoutStatus) + " (step 4 of the evaluation).");
        }
        if (!withoutAdded.isEmpty()) {
            problems.add("These applied improvements leave \"Added\" missing or empty; give the elements and relationships they added, each as Name (ID), or None: " + String.join(", ", withoutAdded) + " (step 4 of the evaluation).");
        }
    }

    // The "- Label: value" items of a list, keyed by the label in lower case, each with the lines that continue it, such as a list of elements written one per line
    private static Map<String, String> fields(List<String> lines) {
        Map<String, StringBuilder> fields = new LinkedHashMap<>();
        Integer topIndent = null;
        StringBuilder current = null;
        for (String line : lines) {
            Matcher field = FIELD.matcher(line);
            if (field.matches()) {
                int indent = ScenarioFileCheck.indentOf(field.group(1));
                if (topIndent == null) {
                    topIndent = indent;
                }
                if (indent <= topIndent) {
                    current = fields.computeIfAbsent(field.group(2).trim().toLowerCase(Locale.ROOT), label -> new StringBuilder());
                    current.append(' ').append(field.group(3));
                    continue;
                }
            }
            if (current != null && !line.isBlank()) {
                current.append(' ').append(line.trim());
            }
        }

        Map<String, String> values = new LinkedHashMap<>();
        fields.forEach((label, value) -> values.put(label, value.toString().trim()));
        return values;
    }

    // The lines of a section above its first subsection, where its table is
    private static List<String> beforeSubsections(List<String> body) {
        List<String> lines = new ArrayList<>();
        for (String line : body) {
            Matcher heading = ScenarioFileCheck.HEADING.matcher(line);
            if (heading.matches() && heading.group(1).length() >= 2) {
                break;
            }
            lines.add(line);
        }
        return lines;
    }

    private static boolean hasElementId(String elements) {
        return elements != null && ELEMENT_ID.matcher(elements).results()
                .map(id -> id.group(1))
                .anyMatch(id -> !OTHER_ID.matcher(id).matches());
    }

    private static boolean isScenarioId(String cell) {
        return ScenarioFileCheck.SCENARIO_ID.matcher(cell.replaceAll("[*_\\[\\]]", "").trim()).matches();
    }

    private static String scenarioIdOf(String cell) {
        return cell.replaceAll("[*_\\[\\]]", "").trim();
    }

    private static Set<String> scenarioIdsIn(String text) {
        Set<String> ids = new LinkedHashSet<>();
        if (text != null) {
            ScenarioFileCheck.SCENARIO_ID.matcher(text).results().map(MatchResult::group).forEach(ids::add);
        }
        return ids;
    }

    private static int columnOf(List<String> header, Predicate<String> isTheColumn) {
        for (int column = 0; column < header.size(); column++) {
            if (isTheColumn.test(ScenarioFileCheck.normalize(header.get(column)))) {
                return column;
            }
        }
        return -1;
    }

    private static String cellAt(List<String> row, int column) {
        return column < row.size() ? row.get(column) : "";
    }

    private static List<String> lines(String contents) {
        return List.of(contents.split("\\R", -1));
    }
}
