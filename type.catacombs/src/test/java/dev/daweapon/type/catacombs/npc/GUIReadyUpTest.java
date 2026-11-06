package dev.daweapon.type.catacombs.npc;

import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Mort's Ready Up menu against the menu captured in M7 runs 1 and 3. */
class GUIReadyUpTest {

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    private static List<String> lines(ItemStack.Builder builder) {
        ItemStack stack = builder.build();
        List<String> lines = new ArrayList<>();
        lines.add(legacy(stack.get(DataComponents.CUSTOM_NAME)));
        stack.get(DataComponents.LORE).forEach(line -> lines.add(legacy(line)));
        return lines;
    }

    @Test
    void theReadyPaneMatchesBothCapturedStates() {
        assertEquals(Material.RED_STAINED_GLASS_PANE, GUIReadyUp.readyPane(false).build().material());
        assertEquals(List.of("§cNot Ready", "§7Click to mark yourself as ready to", "§7start!", "", "§eClick to ready up!"),
                lines(GUIReadyUp.readyPane(false)));
        assertEquals(Material.LIME_STAINED_GLASS_PANE, GUIReadyUp.readyPane(true).build().material());
        assertEquals(List.of("§aReady", "§7You are marked as ready! The fight", "§7will start when all players have",
                "§7readied up and have the same tier", "§7selected!"), lines(GUIReadyUp.readyPane(true)));
    }

    @Test
    void theAutoReadyToggleMatchesTheCapturedMasterModeText() {
        ItemStack.Builder toggle = GUIReadyUp.autoReady(true, CatacombsMode.MASTER);
        assertEquals(Material.LIME_STAINED_GLASS, toggle.build().material());
        assertEquals(List.of("§aToggle Auto Ready Up", "§7Auto ready up when joining §c§lMM§c The",
                "§cCatacombs§7 if you have the", "§7requirements, so you don't have to!", "", "§7Currently: §aEnabled",
                "", "§eClick to toggle!"), lines(toggle));
    }

    @Test
    void theDungeonOrbIsClaimedFromTheBottomLeft() {
        assertEquals(45, GUIReadyUp.ORB_SLOT);
        assertEquals(List.of("§6Dungeon Orb", "§7When entering a Dungeon, this stone", "§7adapts to its user and allows them to",
                "§7use their class abilities.", "", "§6§lLEGENDARY DUNGEON ITEM", "", "§eClick to claim!"), lines(GUIReadyUp.orb()));
    }

    @Test
    void theCapturedSlotsHoldTheClassesAndTheirCounts() {
        assertEquals(13, GUIReadyUp.READY_SLOT);
        assertEquals(29, GUIReadyUp.FIRST_CLASS_SLOT);
        assertEquals(38, GUIReadyUp.FIRST_COUNT_SLOT);
        assertEquals(49, GUIReadyUp.CLOSE_SLOT);
        assertEquals(53, GUIReadyUp.AUTO_READY_SLOT);
        assertEquals(List.of(DungeonClassType.HEALER, DungeonClassType.MAGE, DungeonClassType.BERSERK, DungeonClassType.ARCHER,
                DungeonClassType.TANK), List.of(DungeonClassType.values()));
    }

    @Test
    void aSoloHeadSitsInTheMiddleOfTheTopRow() {
        assertEquals(List.of(4), GUIReadyUp.headSlots(1));
        assertEquals(List.of(3, 5), GUIReadyUp.headSlots(2));
        assertEquals(List.of(0, 2, 4, 6, 8), GUIReadyUp.headSlots(5));
    }

    @Test
    void classBonusLinesUseTheCapturedLabelsAndColours() {
        assertEquals("<7>Arrow Damage: <c>+<c>", GUIReadyUp.bonuses(DungeonClassType.ARCHER, 43).lines().findFirst().orElseThrow()
                .replaceAll("[0-9.]+%$", ""));
        assertEquals("<7>Melee Damage: <c>-<c>25%", GUIReadyUp.bonuses(DungeonClassType.ARCHER, 43).lines().toList().getLast());
        assertEquals(3, GUIReadyUp.bonuses(DungeonClassType.TANK, 48).lines().count());
        assertEquals("<7>Health: <a>+100", GUIReadyUp.bonuses(DungeonClassType.TANK, 48).lines().findFirst().orElseThrow());
    }

    @Test
    void numbersDropTrailingZerosAndKeepOneDecimal() {
        assertEquals("460", GUIReadyUp.number(460));
        assertEquals("18.4", GUIReadyUp.number(18.4));
    }
}
