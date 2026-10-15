package dev.daweapon.type.island.gui;

import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PatchNoteTest {
    @Test
    void parseFillsDefaultsForMissingFields() {
        Map<String, Object> entry = new HashMap<>();
        entry.put("version", "0.21");
        PatchNote note = PatchNote.parse(entry);
        assertEquals("0.21", note.version());
        assertEquals("SkyBlock v0.21", note.name());
        assertEquals("", note.date());
        assertEquals(Material.BOOK, note.material());
        assertEquals("", note.title());
        assertEquals("", note.url());
        assertFalse(note.hasBook());
    }

    @Test
    void parseResolvesMaterialCaseInsensitively() {
        Map<String, Object> entry = new HashMap<>();
        entry.put("version", "1");
        entry.put("material", "DIAMOND_SWORD");
        assertEquals(Material.DIAMOND_SWORD, PatchNote.parse(entry).material());
    }

    @Test
    void unknownMaterialFallsBackToBook() {
        Map<String, Object> entry = new HashMap<>();
        entry.put("version", "1");
        entry.put("material", "not_a_material");
        assertEquals(Material.BOOK, PatchNote.parse(entry).material());
    }

    @Test
    void bookFileLivesUnderPatchNotesDirectory() {
        Map<String, Object> entry = new HashMap<>();
        entry.put("version", "2");
        entry.put("book", "update_2.yml");
        PatchNote note = PatchNote.parse(entry);
        assertTrue(note.hasBook());
        assertEquals("update_2.yml", note.bookFile().getName());
        assertEquals("patch_notes", note.bookFile().getParentFile().getName());
    }

    @Test
    void blankBookMeansNoBook() {
        assertFalse(new PatchNote("1", "n", "d", Material.BOOK, "t", "u", "  ").hasBook());
    }
}
