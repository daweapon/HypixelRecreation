package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DanteContentTest {

    @Test
    void vendorPricesFollowTheWiki() {
        assertEquals(100, DanteAccess.HAPPY_MASK_PRICE);
        assertEquals(1_000_000, DanteAccess.TALISMAN_PRICE);
    }

    @Test
    void goonsLetInAMaskWearerOrAnyPassHolder() {
        assertTrue(DanteAccess.grants(true, List.of(), false));
        assertTrue(DanteAccess.grants(false, List.of(ItemType.DANTE_TALISMAN), false));
        assertTrue(DanteAccess.grants(false, List.of(ItemType.DANTE_RING), false));
        assertTrue(DanteAccess.grants(false, List.of(), true));
        assertFalse(DanteAccess.grants(false, List.of(ItemType.HAPPY_MASK), false));
        assertFalse(DanteAccess.grants(false, List.of(), false));
    }

    @Test
    void fourOfficialBuildingsAndTheCommunityCenterIsNotOne() {
        assertEquals(List.of("Bank", "Auction House", "Bazaar Alley", "Farmhouse"),
                List.of(OfficialBuilding.values()).stream().map(OfficialBuilding::getDisplayName).toList());
        assertTrue(OfficialBuilding.FARMHOUSE.contains(new Pos(61.5, 72, -147.5)));
        assertFalse(OfficialBuilding.FARMHOUSE.contains(new Pos(0, 79, 12)));
        assertNull(OfficialBuilding.at(new Pos(9.5, 79, 12.5)));
    }

    @Test
    void zooSpookyAndJerryAreCancelled() {
        assertEquals(Set.of("traveling_zoo", "spooky_festival", "season_of_jerry"), DanteEventCancellation.CANCELLED_EVENT_IDS);
    }

    @Test
    void timeIsFixedToMidnight() {
        assertEquals(18000, DanteSunlight.MIDNIGHT);
    }

    @Test
    void rayOfHopeFollowsTheWiki() {
        assertEquals(750, DanteStaff.BASE_DAMAGE);
        assertEquals(0.2, DanteStaff.INTELLIGENCE_SCALING);
        assertEquals(10, DanteStaff.MANA_PERCENT);
        assertEquals(2, DanteStaff.GENERAL_MANA_PERCENT);
        assertEquals(100, PercentManaCost.costOf(1000, DanteStaff.MANA_PERCENT));
        assertEquals(20, PercentManaCost.costOf(1000, DanteStaff.GENERAL_MANA_PERCENT));
    }

    @Test
    void goonStatsFollowTheWiki() {
        assertEquals(50, DanteProfiles.GOON.level());
        assertEquals(100_000, DanteProfiles.GOON.health());
        assertEquals(1_000, DanteProfiles.GOON.damage());
        assertEquals(100, DanteProfiles.SLIME_GOON.level());
        assertEquals(10_000, DanteProfiles.SLIME_GOON.health());
        assertEquals(1_000, DanteProfiles.SLIME_GOON.damage());
    }

    @Test
    void resistanceKitsHaveStaffAndFourArmorPieces() {
        assertEquals(5, UprisingGear.RESISTANCE_KIT.size());
        assertEquals(5, UprisingGear.GENERALS_KIT.size());
        assertEquals(ItemType.HOPE_OF_THE_RESISTANCE, UprisingGear.RESISTANCE_KIT.getFirst());
        assertEquals(ItemType.GENERALS_HOPE_OF_THE_RESISTANCE, UprisingGear.GENERALS_KIT.getFirst());
    }

    @Test
    void uprisingHasTheWikiGoonWaveLinesAndLeaderboard() {
        assertEquals("GOONS, PUNISH THEM ALL!", DanteUprising.INTRO[DanteUprising.INTRO.length - 1]);
        assertEquals("GOONS ASSEMBLE!!!", DanteUprising.AFTER_GOONS[DanteUprising.AFTER_GOONS.length - 1]);
        assertEquals("ADIOS!", DanteUprising.DEFEAT[DanteUprising.DEFEAT.length - 1]);
    }

    @Test
    void catalogueItemsAreNoLongerMarkedUnavailable() throws IOException {
        String armorA = Files.readString(Path.of("../configuration/skyblock/items/catalogue/armor_a.yml"));
        String armorC = Files.readString(Path.of("../configuration/skyblock/items/catalogue/armor_c.yml"));
        assertFalse(block(armorA, "ARMOR_OF_THE_RESISTANCE_HELMET").contains("unavailable"));
        assertFalse(block(armorC, "GENERALS_ARMOR_OF_THE_RESISTANCE_BOOTS").contains("unavailable"));
        assertFalse(block(armorC, "HAPPY_MASK").contains("unavailable"));
    }

    @Test
    void staffWeaponsCarryRayOfHope() throws IOException {
        String weapons = Files.readString(Path.of("../configuration/skyblock/items/weapons.yml"));
        assertTrue(block(weapons, "HOPE_OF_THE_RESISTANCE").endsWith("- RAY_OF_HOPE"));
        assertTrue(block(weapons, "GENERALS_HOPE_OF_THE_RESISTANCE").contains("- RAY_OF_HOPE_GENERAL"));
    }

    private static String block(String yaml, String id) {
        int start = yaml.indexOf("  - id: " + id + "\n");
        assertTrue(start >= 0, id);
        int next = yaml.indexOf("\n  - id: ", start + 1);
        return yaml.substring(start, next < 0 ? yaml.length() : next);
    }
}
