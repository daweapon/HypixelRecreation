package dev.daweapon.type.skyblockgeneric.communityupgrades;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityUpgradeCatalogTest {
    private static final File FILE = new File("../configuration/skyblock/community_upgrades.yml");

    @Test
    void everyUpgradeHasTiersAndTheWikiGemTotal() {
        Map<CommunityUpgradeId, List<CommunityUpgradeCatalog.Tier>> all = CommunityUpgradeCatalog.load(FILE);
        int total = 0;
        int withoutCoop = 0;
        for (CommunityUpgradeId id : CommunityUpgradeId.values()) {
            assertTrue(all.get(id).size() >= 2, id + " has no tiers");
            int gems = all.get(id).stream().mapToInt(CommunityUpgradeCatalog.Tier::gems).sum();
            total += gems;
            if (id != CommunityUpgradeId.COOP_SLOTS) withoutCoop += gems;
        }
        assertEquals(17_350, total);
        assertEquals(15_550, withoutCoop);
    }

    @Test
    void tierValuesNeverDecrease() {
        CommunityUpgradeCatalog.load(FILE).forEach((id, tiers) -> {
            double previous = 0;
            for (CommunityUpgradeCatalog.Tier tier : tiers) {
                assertTrue(tier.value() >= previous, id + " value drops");
                previous = tier.value();
            }
        });
    }

    @Test
    void dataSurvivesAJsonRoundTrip() {
        CommunityUpgradeData data = new CommunityUpgradeData();
        data.setTier(CommunityUpgradeId.ARMOR_WARDROBE, 4);
        data.setActive(new CommunityUpgradeData.Active(CommunityUpgradeId.MAGIC_FIND, 2, 100L, 200L));
        CommunityUpgradeData copy = CommunityUpgradeData.fromJson(data.toJson());
        assertEquals(4, copy.tier(CommunityUpgradeId.ARMOR_WARDROBE));
        assertEquals(0, copy.tier(CommunityUpgradeId.LOADOUT_SLOTS));
        assertEquals(CommunityUpgradeId.MAGIC_FIND, copy.active().id());
        assertEquals(200L, copy.active().completesAt());
    }

    @Test
    void emptyDataMeansTierZero() {
        assertEquals(0, CommunityUpgradeData.fromJson("").tier(CommunityUpgradeId.ISLAND_SIZE));
        assertEquals(0.0, CommunityUpgrades.valueAt(CommunityUpgradeId.ISLAND_SIZE, 0));
    }
}
