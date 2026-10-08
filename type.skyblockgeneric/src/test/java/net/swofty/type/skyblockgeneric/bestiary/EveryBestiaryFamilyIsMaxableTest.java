package net.swofty.type.skyblockgeneric.bestiary;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EveryBestiaryFamilyIsMaxableTest {
    private static final BestiaryData DATA = new BestiaryData();

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void everyFamilyHasAMobWithTiersSoMaxProfileCanMaxIt() {
        for (BestiaryCategories category : BestiaryCategories.values()) {
            for (BestiaryEntry entry : category.getEntries()) {
                String where = category.name() + " / " + entry.getName();
                assertFalse(entry.getMobs().isEmpty(), where + " has no mob to record kills against");
                for (BestiaryMob mob : entry.getMobs()) {
                    assertTrue(mob.getMaxBestiaryTier() > 0, where + " has no tiers");
                    assertTrue(DATA.getTotalKillsForMaxTier(mob) > 0, where + " needs no kills to max");
                }
            }
        }
    }

    @Test
    void mobIdsAreUniqueAcrossAllBestiaries() {
        Set<String> seen = new HashSet<>();
        for (BestiaryCategories category : BestiaryCategories.values()) {
            for (BestiaryMob mob : category.getAllMobs()) {
                assertTrue(seen.add(mob.getMobID()), "Duplicate Bestiary mob id " + mob.getMobID());
            }
        }
    }

    @Test
    void maxingEveryMobCompletesEveryFamily() {
        DatapointBestiary.PlayerBestiary bestiary = new DatapointBestiary.PlayerBestiary(new HashMap<>());
        for (BestiaryCategories category : BestiaryCategories.values()) {
            category.getAllMobs().forEach(mob -> bestiary.set(mob, DATA.getTotalKillsForMaxTier(mob)));
        }
        for (BestiaryCategories category : BestiaryCategories.values()) {
            for (BestiaryEntry entry : category.getEntries()) {
                int kills = bestiary.getAmount(entry.getMobs());
                assertTrue(kills >= DATA.getTotalKillsForMaxTier(entry.getMobs().getFirst()),
                        category.name() + " / " + entry.getName() + " is not completed by /maxprofile");
            }
        }
    }
}
