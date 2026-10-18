package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabSettingsTest {
    private static final TabWidget WIDGET = new TabWidget() {
        public String id() { return "test"; }
        public String displayName() { return "Test"; }
        public Material icon() { return Material.STONE; }
        public int defaultPriority() { return 30; }
        public boolean enabledByDefault(TabLocation location) { return location == TabLocation.HUB; }
        public List<TabWidgetToggle> toggles() { return List.of(new TabWidgetToggle("a", "A", true), new TabWidgetToggle("b", "B", false)); }
        public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) { return List.of(); }
    };

    private final AtomicReference<String> stored = new AtomicReference<>("");

    private TabSettings settings() {
        return new TabSettings(stored.get(), stored::set);
    }

    @Test
    void defaultsComeFromTheWidget() {
        TabSettings settings = settings();
        assertTrue(settings.enabled(TabLocation.HUB, WIDGET));
        assertFalse(settings.enabled(TabLocation.GARDEN, WIDGET));
        assertEquals(30, settings.priority(TabLocation.HUB, WIDGET));
        assertTrue(settings.playerListInfo());
        assertTrue(settings.spacing(TabLocation.HUB, WIDGET));
        assertEquals(4, settings.columns(TabLocation.HUB));
    }

    @Test
    void settingsPersistThroughTheStoredString() {
        settings().setEnabled(TabLocation.GARDEN, WIDGET, true);
        settings().setSpacing(TabLocation.GARDEN, WIDGET, false);
        settings().setPlayerListInfo(false);

        TabSettings reloaded = settings();
        assertTrue(reloaded.enabled(TabLocation.GARDEN, WIDGET));
        assertFalse(reloaded.spacing(TabLocation.GARDEN, WIDGET));
        assertFalse(reloaded.playerListInfo());
    }

    @Test
    void settingsAreIndependentPerLocation() {
        settings().setEnabled(TabLocation.HUB, WIDGET, false);
        assertFalse(settings().enabled(TabLocation.HUB, WIDGET));
        assertFalse(settings().enabled(TabLocation.GARDEN, WIDGET));
        settings().setWrapping(TabLocation.HUB, WIDGET, true);
        assertFalse(settings().wrapping(TabLocation.GARDEN, WIDGET));
    }

    @Test
    void togglesOnlyApplyWhenMinimized() {
        assertTrue(settings().shows(TabLocation.HUB, WIDGET, "b"));
        settings().setMinimized(TabLocation.HUB, WIDGET, true);
        assertTrue(settings().shows(TabLocation.HUB, WIDGET, "a"));
        assertFalse(settings().shows(TabLocation.HUB, WIDGET, "b"));
        settings().setToggle(TabLocation.HUB, WIDGET, "b", true);
        settings().setToggle(TabLocation.HUB, WIDGET, "a", false);
        assertTrue(settings().shows(TabLocation.HUB, WIDGET, "b"));
        assertFalse(settings().shows(TabLocation.HUB, WIDGET, "a"));
    }

    @Test
    void corruptStoredValueFallsBackToDefaults() {
        stored.set("not json");
        assertTrue(settings().enabled(TabLocation.HUB, WIDGET));
    }

    @Test
    void thirdColumnToggleAndResetRestoreDefaults() {
        settings().setThirdColumn(TabLocation.HUB, false);
        settings().setEnabled(TabLocation.HUB, WIDGET, false);
        assertEquals(3, settings().columns(TabLocation.HUB));
        assertEquals(4, settings().columns(TabLocation.GARDEN));
        settings().resetLocation(TabLocation.HUB);
        assertEquals(4, settings().columns(TabLocation.HUB));
        assertTrue(settings().enabled(TabLocation.HUB, WIDGET));
    }

    @Test
    void applyToAllCopiesSettingsButKeepsPriority() {
        settings().setSpacing(TabLocation.HUB, WIDGET, false);
        settings().setToggle(TabLocation.HUB, WIDGET, "b", true);
        settings().applyToAllLocations(TabLocation.HUB, WIDGET);
        assertFalse(settings().spacing(TabLocation.GARDEN, WIDGET));
        assertTrue(settings().toggleValue(TabLocation.GARDEN, WIDGET, "b"));
        assertEquals(30, settings().priority(TabLocation.GARDEN, WIDGET));
    }
}
