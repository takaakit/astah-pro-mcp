package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.scenario.inputdto.WorkType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

// Checks whether a scenario file holds everything the architecture scenario development steps write
final class ScenarioFileCheck {

    static final String DEVELOPED_BEFORE = "Before the architecture work";
    static final String DEVELOPED_AFTER = "After the architecture work";

    static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+?)\\s*#*\\s*$");
    static final Pattern LIST_ITEM = Pattern.compile("^(\\s*)[-*+]\\s+(.*)$");
    private static final Pattern SCENARIO_ITEM = Pattern.compile("^\\s*[-*+]\\s+\\[(S-\\d+)]\\s*(.*)$");
    static final Pattern SCENARIO_ID = Pattern.compile("S-\\d+");
    // An entry under "# Architecture Decisions": its ID and what follows the colon, as in "D-01: Keep card data in the reader (S-06, S-09)"
    private static final Pattern DECISION_ENTRY = Pattern.compile("^[*_`]*(D-\\d+)[*_`]*\\s*:(.*)$");
    // The scenarios a decision serves, in parentheses at its end: their IDs, or "none"
    private static final Pattern DECISION_GROUNDS = Pattern.compile("\\(([^()]*)\\)[*_`.\\s]*$");
    // The label of a part of a concrete scenario, with the emphasis marks that Markdown may put around it, as in "**Situation**:" and "**Situation:**"
    private static final Pattern SCENARIO_PART_LABEL = Pattern.compile("(?<![\\p{L}\\p{N}])[*_]{0,2}(situation|response|measure)[*_]{0,2}\\s*:[*_]{0,2}", Pattern.CASE_INSENSITIVE);
    private static final List<String> SCENARIO_PARTS = List.of("situation", "response", "measure");
    // What the architecture scenario development steps show in place of a value, which is no value: "<one line saying what the user asked for>", "..."
    private static final Pattern PLACEHOLDER = Pattern.compile("<[^>]*>|\\.{3,}|…+");
    private static final Pattern ASSUMED_MARK = Pattern.compile("\\(assumed\\)", Pattern.CASE_INSENSITIVE);
    // A cell of the row under the header row of a table, such as "---" or ":---:"
    private static final Pattern SEPARATOR_CELL = Pattern.compile(":?-+:?");
    private static final Set<String> MATRIX_CELL_VALUES = Set.of("o", "-", ".");
    // The start of an entry under "# Trade-off Resolutions": the ID of the scenario whose Situation was revised, at the head of a list item, as in "S-06 (conflicted with S-01)", "[S-06]" and "**S-06**"
    private static final Pattern RESOLUTION_ENTRY = Pattern.compile("[*_`\\[]*(S-\\d+)\\b");
    // The label of a part of an entry under "# Trade-off Resolutions", with the emphasis marks that Markdown may put around it
    private static final Pattern RESOLUTION_PART_LABEL = Pattern.compile("(?<![\\p{L}\\p{N}])[*_]{0,2}(before|after|no longer covers)[*_]{0,2}\\s*:[*_]{0,2}", Pattern.CASE_INSENSITIVE);
    // What may stand around a Situation without changing it: quotation marks, and a full stop at its end
    private static final Pattern SURROUNDING_MARKS = Pattern.compile("^[\"'“”‘’]+|[\"'“”‘’.]+$");
    private static final List<String> ABSTRACT_SCENARIO_PARTS = List.of("Summary", "Narrative", "Key Quality Attributes");
    // The parts of the abstract scenario that are written as text, which must not be empty
    private static final List<String> ABSTRACT_SCENARIO_TEXT_PARTS = List.of("Summary", "Narrative");

    // The sections the architecture scenario development steps write, with the steps that write them
    private static final List<KnownHeading> SECTION_HEADINGS = List.of(
            new KnownHeading("Work", "step 1"),
            new KnownHeading("System of Interest", "step 1"),
            new KnownHeading("Concrete Scenarios", "steps 2 to 4"),
            new KnownHeading("Trade-off Matrix", "step 5"),
            new KnownHeading("Trade-off Resolutions", "step 5"),
            new KnownHeading("Unresolved Trade-offs", "step 5"),
            new KnownHeading("Abstract Scenario", "step 6"));
    private static final List<KnownHeading> ABSTRACT_SCENARIO_PART_HEADINGS = ABSTRACT_SCENARIO_PARTS.stream()
            .map(part -> new KnownHeading(part, "step 6"))
            .toList();
    // The section that step 9 of the architecture scenario development writes while the architecture is edited, which does not keep the scenarios from being fully developed
    private static final List<KnownHeading> DECISION_HEADINGS = List.of(
            new KnownHeading("Architecture Decisions", "step 9"));

    private ScenarioFileCheck() {
    }

    // What the check found: the work type the file names, the sections that say whose file it is, what keeps the scenarios from being fully developed, the IDs of the concrete scenarios, and what is wrong with the architecture decisions.
    // The architecture decisions are recorded while the architecture is edited, after the scenarios are fully developed, so what is wrong with them does not keep the scenarios from being so.
    record Result(
        Optional<WorkType> workType,
        String head,
        List<String> problems,
        List<String> scenarioIds,
        List<String> decisionProblems
    ) {

        boolean complete() {
            return problems.isEmpty();
        }
    }

    // A section: its heading line as written, and the lines under it
    record Section(
        String headingLine,
        List<String> body
    ) {
    }

    // The sections of a text, keyed by the heading text in lower case, and the headings, in lower case, that appear more than once
    record Sections(
        Map<String, Section> byTitle,
        Set<String> repeated
    ) {

        Section get(String title) {
            return byTitle.get(title);
        }
    }

    // A heading that the architecture scenario development steps write, and the step that writes it
    record KnownHeading(
        String title,
        String step
    ) {
    }

    // A list item that is not a concrete scenario - a stakeholder or a quality attribute - and how deep it is indented
    private record ListItem(
        int indent,
        String text
    ) {
    }

    // The concrete scenarios: their IDs, each once, in the order in which they appear; the quality attribute each is listed under, in the form names are compared in; the quality attributes listed, in that form, with their names as written; and the Situation of each scenario that gives its three parts, as written
    private record ConcreteScenarios(
        List<String> ids,
        Map<String, String> qualityAttributeOf,
        Map<String, String> qualityAttributes,
        Map<String, String> situationOf
    ) {
    }

    // The result for a file whose contents cannot be read at all
    static Result unreadable(String problem) {
        return new Result(Optional.empty(), "(The file cannot be read.)", List.of(problem), List.of(), List.of());
    }

    static Result check(String contents) {
        // A byte order mark would keep the first heading from being recognised
        String text = contents.startsWith("﻿") ? contents.substring(1) : contents;
        Sections sections = split(List.of(text.split("\\R", -1)), 1);
        List<String> problems = new ArrayList<>();

        checkRepeatedHeadings(sections, "# ", SECTION_HEADINGS, problems);
        Optional<WorkType> workType = checkWork(sections.get("work"), problems);
        checkSystemOfInterest(sections.get("system of interest"), problems);
        ConcreteScenarios scenarios = checkConcreteScenarios(sections.get("concrete scenarios"), problems);
        checkTradeOffMatrix(sections.get("trade-off matrix"), scenarios, problems);
        checkTradeOffResolutions(sections.get("trade-off resolutions"), scenarios, problems);
        checkAbstractScenario(sections.get("abstract scenario"), scenarios.ids(), problems);
        List<String> decisionProblems = new ArrayList<>();
        checkRepeatedHeadings(sections, "# ", DECISION_HEADINGS, decisionProblems);
        checkArchitectureDecisions(sections.get("architecture decisions"), scenarios.ids(), decisionProblems);

        return new Result(workType, head(sections), List.copyOf(problems), scenarios.ids(), List.copyOf(decisionProblems));
    }

    // C0: a heading written more than once. The check reads the sections under it as one, so that what the later ones hold is not reported as missing.
    static void checkRepeatedHeadings(Sections sections, String marks, List<KnownHeading> headings, List<String> problems) {
        for (KnownHeading heading : headings) {
            if (sections.repeated().contains(heading.title().toLowerCase(Locale.ROOT))) {
                problems.add("The heading \"" + marks + heading.title() + "\" appears more than once: write the section once, under a single heading (" + heading.step() + ").");
            }
        }
    }

    // C1
    private static Optional<WorkType> checkWork(Section work, List<String> problems) {
        if (work == null) {
            problems.add("The \"# Work\" section is missing: write it at the top of the file as step 1 says.");
            return Optional.empty();
        }

        Optional<WorkType> workType = WorkType.ofLabel(field(work, "Type"));
        if (workType.isEmpty()) {
            problems.add("\"Type\" under \"# Work\" is missing or is neither New nor Change (step 1).");
        }

        String request = field(work, "Request");
        if (request == null || isEmptyValue(request)) {
            problems.add("\"Request\" under \"# Work\" is missing or empty: write in one line what the user asked for (step 1).");
        }

        String developed = field(work, "Developed");
        if (developed == null || !(developed.equalsIgnoreCase(DEVELOPED_BEFORE) || developed.equalsIgnoreCase(DEVELOPED_AFTER))) {
            problems.add("\"Developed\" under \"# Work\" is missing or is neither \"" + DEVELOPED_BEFORE + "\" nor \"" + DEVELOPED_AFTER + "\" (step 1).");
        }

        return workType;
    }

    // C2
    private static void checkSystemOfInterest(Section systemOfInterest, List<String> problems) {
        if (systemOfInterest == null || isEmptyValue(String.join(" ", systemOfInterest.body()))) {
            problems.add("The \"# System of Interest\" section is missing or empty (step 1).");
        }
    }

    // C3
    private static ConcreteScenarios checkConcreteScenarios(Section concreteScenarios, List<String> problems) {
        Map<String, String> texts = new LinkedHashMap<>();
        Map<String, String> qualityAttributeOf = new LinkedHashMap<>();
        Map<String, String> qualityAttributes = new LinkedHashMap<>();
        Set<String> duplicates = new LinkedHashSet<>();
        Set<String> outOfPlace = new LinkedHashSet<>();

        if (concreteScenarios != null) {
            // The list items above the current line that are not scenarios: a stakeholder, and under it a quality attribute
            Deque<ListItem> parents = new ArrayDeque<>();
            String currentId = null;
            for (String line : concreteScenarios.body()) {
                Matcher item = LIST_ITEM.matcher(line);
                if (item.matches()) {
                    int indent = indentOf(item.group(1));
                    while (!parents.isEmpty() && parents.peek().indent() >= indent) {
                        parents.pop();
                    }

                    Matcher scenario = SCENARIO_ITEM.matcher(line);
                    if (scenario.matches()) {
                        currentId = scenario.group(1);
                        if (texts.containsKey(currentId)) {
                            duplicates.add(currentId);
                        }
                        texts.merge(currentId, scenario.group(2), (before, added) -> before + " " + added);
                        if (parents.size() == 2) {
                            qualityAttributeOf.putIfAbsent(currentId, normalize(parents.peek().text()));
                        } else {
                            outOfPlace.add(currentId);
                        }

                    } else {
                        parents.push(new ListItem(indent, item.group(2)));
                        if (parents.size() == 2) {
                            qualityAttributes.putIfAbsent(normalize(item.group(2)), item.group(2).trim());
                        }
                        currentId = null;
                    }

                } else if (line.isBlank()) {
                    currentId = null;

                } else if (currentId != null) {
                    // A scenario written over several lines
                    texts.merge(currentId, line.trim(), (before, added) -> before + " " + added);
                }
            }
        }

        if (texts.isEmpty()) {
            problems.add("The \"# Concrete Scenarios\" section is missing or holds no concrete scenario with an ID such as \"[S-01]\" (steps 2 to 4).");
            return new ConcreteScenarios(List.of(), Map.of(), qualityAttributes, Map.of());
        }
        if (!duplicates.isEmpty()) {
            problems.add("These scenario IDs are used more than once under \"# Concrete Scenarios\": " + String.join(", ", duplicates) + " (step 4).");
        }
        if (!outOfPlace.isEmpty()) {
            problems.add("These concrete scenarios are not at the third level of the list, under a quality attribute under a stakeholder: " + String.join(", ", outOfPlace) + " (steps 2 to 4).");
        }

        List<String> withoutParts = new ArrayList<>();
        List<String> withEmptyParts = new ArrayList<>();
        Map<String, String> situationOf = new LinkedHashMap<>();
        texts.forEach((id, text) -> {
            List<String> values = partValues(text);
            if (values == null) {
                withoutParts.add(id);
                return;
            }
            situationOf.put(id, values.get(0));
            if (values.stream().anyMatch(ScenarioFileCheck::isEmptyValue)) {
                withEmptyParts.add(id);
            }
        });
        if (!withoutParts.isEmpty()) {
            problems.add("These concrete scenarios lack \"Situation:\", \"Response:\" or \"Measure:\", or do not give them in this order: " + String.join(", ", withoutParts) + " (step 4).");
        }
        if (!withEmptyParts.isEmpty()) {
            problems.add("These concrete scenarios leave \"Situation:\", \"Response:\" or \"Measure:\" empty: " + String.join(", ", withEmptyParts) + " (step 4).");
        }

        return new ConcreteScenarios(List.copyOf(texts.keySet()), qualityAttributeOf, qualityAttributes, situationOf);
    }

    // C4
    private static void checkTradeOffMatrix(Section matrix, ConcreteScenarios scenarios, List<String> problems) {
        List<List<String>> rows = matrix == null ? List.of() : matrix.body().stream()
                .map(String::trim)
                .filter(line -> line.startsWith("|"))
                .map(ScenarioFileCheck::cells)
                .filter(row -> !isSeparatorRow(row))
                .toList();
        if (rows.isEmpty()) {
            problems.add("The \"# Trade-off Matrix\" section is missing or holds no table (step 5).");
            return;
        }

        // The header row names the quality attributes, one column each, and decides how many cells every row has to fill in
        List<String> columns = rows.stream()
                .filter(row -> !row.isEmpty() && !SCENARIO_ID.matcher(row.get(0)).matches())
                .findFirst()
                .map(header -> header.subList(1, header.size()))
                .orElse(null);
        if (columns == null) {
            problems.add("The trade-off matrix has no header row that names the quality attributes (step 5).");
        } else {
            checkColumns(columns, scenarios.qualityAttributes(), problems);
        }
        List<String> comparedColumns = columns == null ? List.of() : columns.stream().map(ScenarioFileCheck::normalize).toList();

        Set<String> rowIds = new LinkedHashSet<>();
        Set<String> withTwoRows = new LinkedHashSet<>();
        Set<String> withoutScenario = new LinkedHashSet<>();
        Set<String> withEmptyCells = new LinkedHashSet<>();
        Set<String> withUnknownValues = new LinkedHashSet<>();
        Set<String> withExtraCells = new LinkedHashSet<>();
        Set<String> withMisplacedO = new LinkedHashSet<>();
        for (List<String> row : rows) {
            if (row.isEmpty() || !SCENARIO_ID.matcher(row.get(0)).matches()) {
                continue;
            }

            String id = row.get(0);
            if (!rowIds.add(id)) {
                withTwoRows.add(id);
            }
            if (!scenarios.ids().contains(id)) {
                withoutScenario.add(id);
            }

            List<String> values = row.subList(1, row.size());
            boolean considered = !values.isEmpty() && values.size() >= comparedColumns.size();
            if (!considered) {
                withEmptyCells.add(id);
            }
            if (columns != null && values.size() > comparedColumns.size()) {
                withExtraCells.add(id);
            }
            for (String value : values) {
                if (value.isEmpty()) {
                    withEmptyCells.add(id);
                    considered = false;
                } else if (!MATRIX_CELL_VALUES.contains(value.toLowerCase(Locale.ROOT))) {
                    withUnknownValues.add(id);
                    considered = false;
                }
            }

            // "o" marks the column of the quality attribute the scenario belongs to, and that column alone. A row already reported for its cells is not looked at again.
            String qualityAttribute = scenarios.qualityAttributeOf().get(id);
            int own = qualityAttribute == null ? -1 : comparedColumns.indexOf(qualityAttribute);
            if (considered && own >= 0 && values.size() == comparedColumns.size()
                    && IntStream.range(0, values.size()).anyMatch(column -> values.get(column).equalsIgnoreCase("o") != (column == own))) {
                withMisplacedO.add(id);
            }
        }

        List<String> withoutRow = scenarios.ids().stream().filter(id -> !rowIds.contains(id)).toList();
        if (!withoutRow.isEmpty()) {
            problems.add("The trade-off matrix has no row for these scenarios: " + String.join(", ", withoutRow) + " (step 5).");
        }
        if (!withEmptyCells.isEmpty()) {
            problems.add("The trade-off matrix has empty cells, which are cells that have not been considered, in the rows of these scenarios: " + String.join(", ", withEmptyCells) + " (step 5).");
        }
        if (!withUnknownValues.isEmpty()) {
            problems.add("The trade-off matrix has cells that hold something other than \"o\", \"-\" and \".\" in the rows of these scenarios: " + String.join(", ", withUnknownValues) + " (step 5).");
        }
        if (!withTwoRows.isEmpty()) {
            problems.add("The trade-off matrix has more than one row for these scenarios: " + String.join(", ", withTwoRows) + " (step 5).");
        }
        if (!withoutScenario.isEmpty()) {
            problems.add("The trade-off matrix has rows for these scenario IDs, which \"# Concrete Scenarios\" does not have: " + String.join(", ", withoutScenario) + " (step 5).");
        }
        if (!withExtraCells.isEmpty()) {
            problems.add("The trade-off matrix has more cells than columns in the rows of these scenarios: " + String.join(", ", withExtraCells) + " (step 5).");
        }
        if (!withMisplacedO.isEmpty()) {
            problems.add("In the rows of these scenarios, the trade-off matrix does not hold \"o\" in the column of the quality attribute the scenario belongs to, or holds it in another column as well: " + String.join(", ", withMisplacedO) + " (step 5).");
        }
    }

    // The columns of the trade-off matrix against the quality attributes listed under "# Concrete Scenarios": each of them once, and nothing else
    private static void checkColumns(List<String> columns, Map<String, String> qualityAttributes, List<String> problems) {
        Map<String, String> named = new LinkedHashMap<>();
        Set<String> twice = new LinkedHashSet<>();
        for (String column : columns) {
            if (named.putIfAbsent(normalize(column), column) != null) {
                twice.add(column);
            }
        }
        if (!twice.isEmpty()) {
            problems.add("The trade-off matrix has more than one column for these quality attributes: " + String.join(", ", twice) + " (step 5).");
        }

        // Without quality attributes to compare with, the list of concrete scenarios has been reported already
        if (qualityAttributes.isEmpty()) {
            return;
        }

        List<String> withoutColumn = qualityAttributes.entrySet().stream()
                .filter(attribute -> !named.containsKey(attribute.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (!withoutColumn.isEmpty()) {
            problems.add("The trade-off matrix has no column for these quality attributes listed under \"# Concrete Scenarios\": " + String.join(", ", withoutColumn) + " (step 5).");
        }

        List<String> unknown = named.entrySet().stream()
                .filter(column -> !qualityAttributes.containsKey(column.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (!unknown.isEmpty()) {
            problems.add("The trade-off matrix has columns that name no quality attribute listed under \"# Concrete Scenarios\": " + String.join(", ", unknown) + " (step 5).");
        }
    }

    // C6: the entries under "# Trade-off Resolutions" against the concrete scenarios. The section is written only when a Situation has been revised, so a file without it lacks nothing.
    private static void checkTradeOffResolutions(Section resolutions, ConcreteScenarios scenarios, List<String> problems) {
        // Without concrete scenarios to compare with, the list of concrete scenarios has been reported already
        if (resolutions == null || scenarios.ids().isEmpty()) {
            return;
        }

        Set<String> namedIds = new LinkedHashSet<>();
        // The text of the last entry for each scenario. A Situation revised in more than one round has an entry for each round, and only the last one gives the Situation the scenario has now.
        Map<String, String> lastEntries = new LinkedHashMap<>();
        Integer topIndent = null;
        String currentId = null;
        StringBuilder currentText = null;
        for (String line : resolutions.body()) {
            Matcher item = LIST_ITEM.matcher(line);
            if (item.matches()) {
                int indent = indentOf(item.group(1));
                if (topIndent == null) {
                    topIndent = indent;
                }
                // An item at the top level begins the next entry, unless it is a part written without indentation
                if (indent <= topIndent && !RESOLUTION_PART_LABEL.matcher(item.group(2)).lookingAt()) {
                    if (currentId != null) {
                        lastEntries.put(currentId, currentText.toString());
                    }
                    // An item that does not begin with an ID, such as "- None.", is no entry
                    Matcher entry = RESOLUTION_ENTRY.matcher(item.group(2));
                    if (entry.lookingAt()) {
                        currentId = entry.group(1);
                        currentText = new StringBuilder(item.group(2).substring(entry.end()));
                        namedIds.addAll(namedIdsOf(currentId, currentText.toString()));
                    } else {
                        currentId = null;
                        currentText = null;
                    }
                } else if (currentText != null) {
                    currentText.append(' ').append(item.group(2));
                }

            } else if (currentText != null && !line.isBlank()) {
                // A part written over several lines
                currentText.append(' ').append(line.trim());
            }
        }
        if (currentId != null) {
            lastEntries.put(currentId, currentText.toString());
        }

        List<String> unknown = namedIds.stream().filter(id -> !scenarios.ids().contains(id)).toList();
        List<String> withoutAfter = new ArrayList<>();
        List<String> withAnotherSituation = new ArrayList<>();
        lastEntries.forEach((id, text) -> {
            // An entry for a scenario that does not exist is reported as such, and has nothing to be compared with
            if (!scenarios.ids().contains(id)) {
                return;
            }
            String after = afterOf(text);
            // A scenario without its three parts has been reported already, and has no Situation to compare with
            String situation = scenarios.situationOf().get(id);
            if (after == null || isEmptyValue(after)) {
                withoutAfter.add(id);
            } else if (situation != null && !comparableSituation(after).equals(comparableSituation(situation))) {
                withAnotherSituation.add(id);
            }
        });

        if (!unknown.isEmpty()) {
            problems.add("The entries under \"# Trade-off Resolutions\" name these scenario IDs, which \"# Concrete Scenarios\" does not have: " + String.join(", ", unknown) + " (step 5).");
        }
        if (!withoutAfter.isEmpty()) {
            problems.add("Under \"# Trade-off Resolutions\", the last entry for each of these scenarios lacks \"After:\" or leaves it empty: " + String.join(", ", withoutAfter) + " (step 5).");
        }
        if (!withAnotherSituation.isEmpty()) {
            problems.add("Under \"# Trade-off Resolutions\", the \"After:\" of the last entry for each of these scenarios is not the Situation the scenario has under \"# Concrete Scenarios\", and the two have to be the same: " + String.join(", ", withAnotherSituation) + " (step 5).");
        }
    }

    // The IDs that the first line of an entry names before its parts: the scenario whose Situation was revised, and the one it conflicted with
    private static List<String> namedIdsOf(String id, String restOfFirstLine) {
        Matcher label = RESOLUTION_PART_LABEL.matcher(restOfFirstLine);
        String heading = label.find() ? restOfFirstLine.substring(0, label.start()) : restOfFirstLine;
        List<String> ids = new ArrayList<>(List.of(id));
        SCENARIO_ID.matcher(heading).results().map(MatchResult::group).forEach(ids::add);
        return ids;
    }

    // The value of the "After:" part of an entry, or null when the entry has no such part
    private static String afterOf(String entry) {
        List<MatchResult> labels = RESOLUTION_PART_LABEL.matcher(entry).results().toList();
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i).group(1).equalsIgnoreCase("after")) {
                int end = i + 1 < labels.size() ? labels.get(i + 1).start() : entry.length();
                return entry.substring(labels.get(i).end(), end);
            }
        }
        return null;
    }

    // A Situation in the form it is compared in: as values are, without the mark of an assumption, and without quotation marks around it or a full stop at its end
    private static String comparableSituation(String text) {
        return SURROUNDING_MARKS.matcher(normalize(ASSUMED_MARK.matcher(text).replaceAll(""))).replaceAll("").trim();
    }

    // C5
    private static void checkAbstractScenario(Section abstractScenario, List<String> ids, List<String> problems) {
        if (abstractScenario == null) {
            problems.add("The \"# Abstract Scenario\" section is missing (step 6).");
            return;
        }

        Sections parts = split(abstractScenario.body(), 2);
        checkRepeatedHeadings(parts, "## ", ABSTRACT_SCENARIO_PART_HEADINGS, problems);

        List<String> missingParts = ABSTRACT_SCENARIO_PARTS.stream()
                .filter(part -> parts.get(part.toLowerCase(Locale.ROOT)) == null)
                .map(part -> "\"## " + part + "\"")
                .toList();
        if (!missingParts.isEmpty()) {
            problems.add("The \"# Abstract Scenario\" section lacks these parts: " + String.join(", ", missingParts) + " (step 6).");
        }

        List<String> emptyParts = ABSTRACT_SCENARIO_TEXT_PARTS.stream()
                .filter(part -> {
                    Section section = parts.get(part.toLowerCase(Locale.ROOT));
                    return section != null && isEmptyValue(String.join(" ", section.body()));
                })
                .map(part -> "\"## " + part + "\"")
                .toList();
        if (!emptyParts.isEmpty()) {
            problems.add("These parts of the \"# Abstract Scenario\" section are empty: " + String.join(", ", emptyParts) + " (step 6).");
        }

        Section keyQualityAttributes = parts.get("key quality attributes");
        if (keyQualityAttributes == null) {
            return;
        }
        Set<String> mentioned = new LinkedHashSet<>();
        for (String line : keyQualityAttributes.body()) {
            Matcher id = SCENARIO_ID.matcher(line);
            while (id.find()) {
                mentioned.add(id.group());
            }
        }
        List<String> notMentioned = ids.stream().filter(id -> !mentioned.contains(id)).toList();
        if (!notMentioned.isEmpty()) {
            problems.add("These scenario IDs do not appear under \"## Key Quality Attributes\": " + String.join(", ", notMentioned) + " (step 6).");
        }
    }

    // C7: the entries under "# Architecture Decisions", which step 9 of the architecture scenario development has written while the architecture is edited. A file without the section lacks nothing.
    private static void checkArchitectureDecisions(Section decisions, List<String> scenarioIds, List<String> problems) {
        if (decisions == null) {
            return;
        }

        Map<String, String> entries = new LinkedHashMap<>();
        Set<String> twice = new LinkedHashSet<>();
        List<String> notDecisions = new ArrayList<>();
        Integer topIndent = null;
        String currentId = null;
        StringBuilder currentText = null;
        for (String line : decisions.body()) {
            Matcher item = LIST_ITEM.matcher(line);
            if (item.matches()) {
                int indent = indentOf(item.group(1));
                if (topIndent == null) {
                    topIndent = indent;
                }
                if (indent <= topIndent) {
                    if (currentId != null) {
                        entries.put(currentId, currentText.toString());
                    }
                    Matcher entry = DECISION_ENTRY.matcher(item.group(2).trim());
                    if (entry.matches()) {
                        currentId = entry.group(1);
                        currentText = new StringBuilder(entry.group(2));
                        if (entries.containsKey(currentId)) {
                            twice.add(currentId);
                        }
                    } else {
                        notDecisions.add(item.group(2).trim());
                        currentId = null;
                        currentText = null;
                    }
                } else if (currentText != null) {
                    currentText.append(' ').append(item.group(2).trim());
                }

            } else if (currentText != null && !line.isBlank()) {
                // A decision written over several lines
                currentText.append(' ').append(line.trim());
            }
        }
        if (currentId != null) {
            entries.put(currentId, currentText.toString());
        }

        List<String> withoutDecision = new ArrayList<>();
        List<String> withoutGrounds = new ArrayList<>();
        Set<String> unknown = new LinkedHashSet<>();
        entries.forEach((id, text) -> {
            Matcher grounds = DECISION_GROUNDS.matcher(text.trim());
            if (!grounds.find()) {
                withoutGrounds.add(id);
                return;
            }
            if (isEmptyValue(text.trim().substring(0, grounds.start()))) {
                withoutDecision.add(id);
            }
            String named = grounds.group(1).trim();
            if (normalize(named).equals("none")) {
                return;
            }
            List<String> ids = SCENARIO_ID.matcher(named).results().map(MatchResult::group).toList();
            if (ids.isEmpty()) {
                withoutGrounds.add(id);
            }
            ids.stream()
                    .filter(scenarioId -> !scenarioIds.isEmpty() && !scenarioIds.contains(scenarioId))
                    .forEach(unknown::add);
        });

        if (!notDecisions.isEmpty()) {
            problems.add("These items under \"# Architecture Decisions\" are not decisions in the form \"- D-01: <the decision> (S-01, S-02)\": " + String.join(" / ", notDecisions) + " (step 9).");
        }
        if (!twice.isEmpty()) {
            problems.add("These decision IDs are used more than once under \"# Architecture Decisions\": " + String.join(", ", twice) + " (step 9).");
        }
        if (!withoutDecision.isEmpty()) {
            problems.add("These entries under \"# Architecture Decisions\" do not say what was decided: " + String.join(", ", withoutDecision) + " (step 9).");
        }
        if (!withoutGrounds.isEmpty()) {
            problems.add("These decisions under \"# Architecture Decisions\" do not end with the IDs of the scenarios they serve in parentheses, or with \"(none)\": " + String.join(", ", withoutGrounds) + " (step 9).");
        }
        if (!unknown.isEmpty()) {
            problems.add("The decisions under \"# Architecture Decisions\" name these scenario IDs, which \"# Concrete Scenarios\" does not have: " + String.join(", ", unknown) + " (step 9).");
        }
    }

    // The values of the three parts of a concrete scenario, in order, or null when a label is missing or the labels are not in order
    private static List<String> partValues(String scenario) {
        List<MatchResult> labels = SCENARIO_PART_LABEL.matcher(scenario).results().toList();
        List<MatchResult> found = new ArrayList<>();
        int next = 0;
        for (String part : SCENARIO_PARTS) {
            int index = next;
            while (index < labels.size() && !labels.get(index).group(1).equalsIgnoreCase(part)) {
                index++;
            }
            if (index == labels.size()) {
                return null;
            }
            found.add(labels.get(index));
            next = index + 1;
        }

        List<String> values = new ArrayList<>();
        for (int i = 0; i < found.size(); i++) {
            int end = i + 1 < found.size() ? found.get(i + 1).start() : scenario.length();
            values.add(scenario.substring(found.get(i).end(), end));
        }
        return values;
    }

    // Whether a value is empty: nothing once emphasis marks and the "(assumed)" mark are taken out, or nothing but a placeholder of the architecture scenario development steps
    static boolean isEmptyValue(String value) {
        String text = normalize(ASSUMED_MARK.matcher(value).replaceAll(""));
        return text.isEmpty() || PLACEHOLDER.matcher(text).matches();
    }

    // A name or a value in the form it is compared in: without emphasis marks and backquotes, with white space trimmed and each run of it made one space, in lower case
    static String normalize(String text) {
        return text.replaceAll("[*_`]", "").trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    // How deep a list item is indented, a tab counting as four spaces
    static int indentOf(String indentation) {
        int width = 0;
        for (char character : indentation.toCharArray()) {
            width += character == '\t' ? 4 : 1;
        }
        return width;
    }

    // Whether a table row is the row under the header row, such as "|---|:---:|"
    static boolean isSeparatorRow(List<String> row) {
        return !row.isEmpty() && row.stream().allMatch(cell -> SEPARATOR_CELL.matcher(cell).matches());
    }

    // The sections that say whose file this is, as they are written
    private static String head(Sections sections) {
        List<String> lines = new ArrayList<>();
        for (String title : List.of("work", "system of interest")) {
            Section section = sections.get(title);
            if (section == null) {
                continue;
            }
            if (!lines.isEmpty()) {
                lines.add("");
            }
            lines.add(section.headingLine().trim());
            List<String> body = new ArrayList<>(section.body());
            while (!body.isEmpty() && body.get(body.size() - 1).isBlank()) {
                body.remove(body.size() - 1);
            }
            lines.addAll(body);
        }

        if (lines.isEmpty()) {
            return "(The file has neither a \"# Work\" section nor a \"# System of Interest\" section.)";
        }
        return String.join("\n", lines);
    }

    // Split lines into the sections that headings of the given level begin, keyed by the heading text in lower case.
    // Headings inside a fenced code block are not headings. A section under a heading that has already appeared is read as the rest of the first one, and the heading is noted as repeated.
    static Sections split(List<String> lines, int level) {
        Map<String, Section> sections = new LinkedHashMap<>();
        Set<String> repeated = new LinkedHashSet<>();
        List<String> body = null;
        boolean inCodeBlock = false;

        for (String line : lines) {
            if (line.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock;
            }

            Matcher heading = inCodeBlock ? null : HEADING.matcher(line);
            if (heading != null && heading.matches() && heading.group(1).length() <= level) {
                body = null;
                if (heading.group(1).length() == level) {
                    String title = heading.group(2).toLowerCase(Locale.ROOT);
                    Section first = sections.get(title);
                    if (first != null) {
                        repeated.add(title);
                        body = first.body();
                    } else {
                        body = new ArrayList<>();
                        sections.put(title, new Section(line, body));
                    }
                }

            } else if (body != null) {
                body.add(line);
            }
        }

        return new Sections(sections, repeated);
    }

    // The value of a "- Name: value" line of a section, or null when the section has no such line
    private static String field(Section section, String name) {
        Pattern pattern = Pattern.compile("^\\s*(?:[-*+]\\s+)?" + Pattern.quote(name) + "\\s*:\\s*(.*?)\\s*$", Pattern.CASE_INSENSITIVE);
        for (String line : section.body()) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    // The cells of a table row, without the pipes that enclose it
    static List<String> cells(String row) {
        String inner = row.substring(1);
        if (inner.endsWith("|")) {
            inner = inner.substring(0, inner.length() - 1);
        }

        List<String> cells = new ArrayList<>();
        for (String cell : inner.split("\\|", -1)) {
            cells.add(cell.replace("`", "").trim());
        }
        return cells;
    }
}
