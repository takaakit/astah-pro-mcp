package com.astahpromcp.tool.astah.pro.matrix;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Reads back the Markdown of a relationship matrix, so that the tests can check a cell or a line rather than the whole text
final class MatrixMarkdown {

    private final String markdown;

    MatrixMarkdown(String markdown) {
        this.markdown = markdown;
    }

    String text() {
        return markdown;
    }

    // The lines of a section, without its heading, up to the next section
    List<String> section(String heading) {
        List<String> lines = List.of(markdown.split("\n", -1));
        int start = lines.indexOf("## " + heading);
        assertTrue(start >= 0, "no section " + heading + " in:\n" + markdown);

        List<String> section = new ArrayList<>();
        for (int i = start + 1; i < lines.size() && !lines.get(i).startsWith("## "); i++) {
            if (!lines.get(i).isEmpty()) {
                section.add(lines.get(i));
            }
        }
        return section;
    }

    boolean hasSection(String heading) {
        return List.of(markdown.split("\n", -1)).contains("## " + heading);
    }

    // The one line of a section that holds no table, such as the line that says why the grid is left out
    String line(String heading) {
        List<String> section = section(heading);
        assertEquals(1, section.size(), "section " + heading + " is not one line: " + section);
        return section.get(0);
    }

    // The value of a list item such as "- Rows with relationships: 3 / 5 (60%)"
    String item(String heading, String label) {
        String prefix = "- " + label + ": ";
        return section(heading).stream()
                .filter(line -> line.startsWith(prefix))
                .map(line -> line.substring(prefix.length()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no item " + label + " in section " + heading + ":\n" + markdown));
    }

    // The rows of the table of a section, each from its header to its cells, keyed by its first cell
    Map<String, Map<String, String>> table(String heading) {
        List<String> lines = section(heading);
        assertTrue(lines.size() >= 2 && lines.get(0).startsWith("|"), "section " + heading + " holds no table: " + lines);

        List<String> header = cells(lines.get(0));
        Map<String, Map<String, String>> rows = new LinkedHashMap<>();
        for (String line : lines.subList(2, lines.size())) {
            List<String> cells = cells(line);
            assertEquals(header.size(), cells.size(), "a row of " + heading + " has another number of cells than its header: " + line);

            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < header.size(); i++) {
                row.put(header.get(i), cells.get(i));
            }
            rows.put(cells.get(0), row);
        }
        return rows;
    }

    // The cells of a table line, with an escaped vertical bar read back as one
    static List<String> cells(String line) {
        assertTrue(line.startsWith("|") && line.endsWith("|"), "not a table line: " + line);

        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        for (int i = 1; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length() && line.charAt(i + 1) == '|') {
                cell.append('|');
                i++;
            } else if (c == '|') {
                cells.add(cell.toString().trim());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        return cells;
    }
}
