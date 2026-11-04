package dev.daweapon.service.api.control.admin;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvTableTest {
    @Test
    void readsQuotedCellsWithCommasQuotesAndNewlines() {
        String csv = "\"#\",\"Task\",\"Notes\"\r\n"
                + "\"1\",\"Fix, then test\",\"line one\nline \"\"two\"\"\"\r\n"
                + "\"2\",\"\",\"\"\n";
        List<List<String>> rows = CsvTable.parse(csv);
        assertEquals(3, rows.size());
        assertEquals(List.of("#", "Task", "Notes"), rows.get(0));
        assertEquals(List.of("1", "Fix, then test", "line one\nline \"two\""), rows.get(1));
        assertEquals(List.of("2", "", ""), rows.get(2));
    }

    @Test
    void keepsALastRowWithoutTrailingNewline() {
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), CsvTable.parse("a,b\nc,d"));
    }

    @Test
    void emptyInputHasNoRows() {
        assertEquals(List.of(), CsvTable.parse(""));
    }

    @Test
    void rowsAreReadByPositionAndBlankRowsAreSkipped() {
        List<List<String>> table = CsvTable.parse("\"#\",\"Item\",\"Details\",\"Type\",\"Priority\",\"Effort\",\"Status\",\"Notes\"\n"
                + "\"7\",\"Bug title\",\"details\",\"Bug\",\"P1 - High\",\"M\",\"Done\",\"note\"\n"
                + "\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"\n"
                + "\"8\",\"Short row\"\n");
        List<RoadmapSheet.Row> rows = RoadmapSheet.toRows(table);
        assertEquals(2, rows.size());
        assertEquals(new RoadmapSheet.Row("7", "Bug title", "details", "Bug", "P1 - High", "M", "Done", "note"), rows.get(0));
        assertEquals("", rows.get(1).status());
    }

    @Test
    void tabNamesMatchTheSheet() {
        assertEquals("Bugs & Loose Ends", RoadmapSheet.Tab.parse("bugs").sheetName());
        assertEquals(RoadmapSheet.Tab.ROADMAP, RoadmapSheet.Tab.parse("nonsense"));
        assertEquals(RoadmapSheet.Tab.DONE, RoadmapSheet.Tab.parse("Done"));
    }
}
