package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu;

import dev.daweapon.commons.text.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Inside a dungeon the Magical Map opens the SkyBlock Menu with Profile Management disabled, as captured in M7 run 1. */
class SkyBlockMenuDungeonTest {
    private static final Path STRINGS = Path.of("..", "configuration", "i18n", "en_US", "gui", "gui_sbmenu.properties");

    @BeforeAll
    static void loadStrings() throws IOException {
        Properties strings = new Properties();
        try (Reader reader = Files.newBufferedReader(STRINGS)) {
            strings.load(reader);
        }
        Text.translationKeyLookup(strings::containsKey);
    }

    @AfterAll
    static void forgetStrings() {
        Text.translationKeyLookup(_ -> false);
    }

    @Test
    void profileManagementKeepsItsClickLineOutsideDungeons() {
        List<Text> lore = GUISkyBlockMenu.profileManagementLore(3, false);
        assertEquals(Text.key("gui_sbmenu.main.profile_management.lore.13", 3), lore.getLast());
    }

    @Test
    void inADungeonTheClickLineBecomesHypixelsNotice() {
        List<Text> normal = GUISkyBlockMenu.profileManagementLore(3, false);
        List<Text> dungeon = GUISkyBlockMenu.profileManagementLore(3, true);
        assertEquals(normal.size() + 1, dungeon.size());
        assertEquals(normal.subList(0, normal.size() - 1), dungeon.subList(0, normal.size() - 1));
        assertEquals(Text.key("gui_sbmenu.main.profile_management.dungeons.1"), dungeon.get(dungeon.size() - 2));
        assertEquals(Text.key("gui_sbmenu.main.profile_management.dungeons.2"), dungeon.getLast());
    }
}
