package dev.daweapon.type.skyblockgeneric.bestiary;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.fishing.bestiary.FishingBestiarySection;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryEntry;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Hypixel's Bestiary categories for content this server has, checked against the wiki Bestiary list. */
class BestiaryCoverageTest {
    private static final BestiaryData DATA = new BestiaryData();

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    private static Map<String, BestiaryMob> firstMobByName(BestiaryCategories category) {
        Map<String, BestiaryMob> byName = new HashMap<>();
        for (BestiaryEntry entry : category.getEntries()) {
            byName.put(entry.getName().replace("<a>", ""), entry.getMobs().getFirst());
        }
        return byName;
    }

    private static void assertFamily(BestiaryCategories category, String family, int bracket, int maxTier) {
        BestiaryMob mob = firstMobByName(category).get(family);
        assertTrue(mob != null, category + " is missing " + family);
        assertEquals(bracket, mob.getBestiaryBracket(), family + " bracket");
        assertEquals(maxTier, mob.getMaxBestiaryTier(), family + " max tier");
    }

    private static int tiers(BestiaryCategories category) {
        return Arrays.stream(category.getEntries()).mapToInt(entry -> entry.getMobs().getFirst().getMaxBestiaryTier()).sum();
    }

    @Test
    void theCatacombsHaveHypixelsThirtyFiveFamilies() {
        assertEquals(35, BestiaryCategories.CATACOMBS.getEntries().length);
        assertEquals(665, tiers(BestiaryCategories.CATACOMBS));
        assertFamily(BestiaryCategories.CATACOMBS, "Crypt Lurker", 2, 15);
        assertFamily(BestiaryCategories.CATACOMBS, "Lost Adventurer", 7, 25);
        assertFamily(BestiaryCategories.CATACOMBS, "Wither Miner", 4, 25);
        assertFamily(BestiaryCategories.CATACOMBS, "Zombie Soldier", 3, 20);
        assertFamily(BestiaryCategories.CATACOMBS, "Undead", 2, 15);
    }

    @Test
    void dungeonMobsCountForTheirFamilyUnderItsOwnKillKey() {
        assertEquals("CATACOMBS_LOST_ADVENTURER", BestiaryCategory.CATACOMBS.forDungeonMob("FROZEN_ADVENTURER").orElseThrow().getMobID());
        assertEquals("CATACOMBS_UNDEAD", BestiaryCategory.CATACOMBS.forDungeonMob("WATCHER_UNDEAD").orElseThrow().getMobID());
        assertEquals("CATACOMBS_FELS", BestiaryCategory.CATACOMBS.forDungeonMob("FEL").orElseThrow().getMobID());
        assertEquals("CATACOMBS_BAT", BestiaryCategory.CATACOMBS.forDungeonMob("BAT").orElseThrow().getMobID(),
                "a dungeon bat must not count for the Private Island Bat");
        assertTrue(BestiaryCategory.CATACOMBS.forDungeonMob("BONZO").isEmpty(), "bosses are not Bestiary families");
        assertTrue(BestiaryCategory.CATACOMBS.forDungeonMob("CRYPT_UNDEAD").isEmpty());
    }

    @Test
    void theSpookyFestivalHasItsSevenLandFamilies() {
        assertEquals(7, BestiaryCategories.SPOOKY_FESTIVAL.getEntries().length);
        assertFamily(BestiaryCategories.SPOOKY_FESTIVAL, "Headless Horseman", 7, 15);
        assertFamily(BestiaryCategories.SPOOKY_FESTIVAL, "Wraith", 6, 15);
    }

    @Test
    void newFamiliesUseRealHeadTextures() {
        for (BestiaryCategories category : List.of(BestiaryCategories.CATACOMBS, BestiaryCategories.SPOOKY_FESTIVAL)) {
            for (BestiaryEntry entry : category.getEntries()) {
                assertTrue(entry.getGuiMaterial().hasTexture(), entry.getName() + " has no head texture");
            }
        }
    }

    @Test
    void everyFishingSectionHasACategorySoTheFishingMenuOpens() {
        for (FishingBestiarySection section : FishingBestiarySection.values()) {
            assertDoesNotThrow(() -> BestiaryCategories.of(section), section.name());
        }
        assertEquals(7, BestiaryCategories.of(FishingBestiarySection.MOONGLADE_MARSH).getEntries().length);
        assertEquals(6, BestiaryCategories.of(FishingBestiarySection.TORRHUS_CANYON).getEntries().length);
    }

    @Test
    void moongladeMarshListsEachFamilyOnceIncludingTheInvisibug() {
        Map<String, Long> counts = new HashMap<>();
        for (BestiaryEntry entry : BestiaryCategories.MOONGLADE_MARSH.getEntries()) counts.merge(entry.getName(), 1L, Long::sum);
        counts.forEach((name, count) -> assertEquals(1L, count, name + " is listed more than once"));
        assertFamily(BestiaryCategories.MOONGLADE_MARSH, "Tidetot", 12, 10);
        assertFamily(BestiaryCategories.MOONGLADE_MARSH, "Invisibug", 11, 10);
    }

    @Test
    void bracketsAndCapsMatchTheWiki() {
        assertFamily(BestiaryCategories.HUB, "Zombie Villager", 1, 5);
        assertFamily(BestiaryCategories.HUB, "Wolf", 3, 15);
        assertFamily(BestiaryCategories.HUB, "Old Wolf", 5, 15);
        assertFamily(BestiaryCategories.DWARVEN_MINES, "Diamond Goblin", 8, 15);
        assertFamily(BestiaryCategories.DWARVEN_MINES, "Glacite Walker", 1, 10);
        assertFamily(BestiaryCategories.DWARVEN_MINES, "Goblin", 4, 20);
        assertFamily(BestiaryCategories.DWARVEN_MINES, "Golden Goblin", 6, 15);
        assertFamily(BestiaryCategories.KUUDRA, "Blazing Golem", 6, 10);
        assertFamily(BestiaryCategories.KUUDRA, "Dropship", 6, 10);
        assertFamily(BestiaryCategories.KUUDRA, "Kuudra Slasher", 6, 10);
        assertFamily(BestiaryCategories.KUUDRA, "Magma Follower", 6, 10);
        assertFamily(BestiaryCategories.KUUDRA, "Wither Sentry", 6, 10);
    }

    @Test
    void maxedCatacombsFamiliesCountTowardBestiarySkyBlockXp() {
        DatapointBestiary.PlayerBestiary bestiary = new DatapointBestiary.PlayerBestiary(new HashMap<>());
        BestiaryCategories.CATACOMBS.getAllMobs().forEach(mob -> bestiary.set(mob, DATA.getTotalKillsForMaxTier(mob)));
        assertEquals(665, BestiaryExperience.totalTiers(bestiary));
        assertEquals(725, BestiaryExperience.skyBlockXp(665), "665 tiers plus 10 XP for each 100 tiers");
    }
}
