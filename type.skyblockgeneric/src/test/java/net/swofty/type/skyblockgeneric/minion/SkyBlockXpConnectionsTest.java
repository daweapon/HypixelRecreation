package net.swofty.type.skyblockgeneric.minion;

import net.swofty.type.skyblockgeneric.bank.BankAccountTier;
import net.swofty.type.skyblockgeneric.bank.BankUpgradeAwards;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.warps.TravelScrollAwards;
import net.swofty.type.skyblockgeneric.warps.TravelScrollType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkyBlockXpConnectionsTest {
    @Test
    void minionTiersFollowTheWikiTable() {
        int twelveTiers = 0;
        for (int tier = 1; tier <= 12; tier++) twelveTiers += MinionAwards.xpForTier(tier);
        assertEquals(57, twelveTiers);
        assertEquals(0, MinionAwards.xpForTier(13));
        var data = new DatapointMinionData.ProfileMinionData(new ArrayList<>(List.of(
                Map.entry("A", List.of(1, 1, 12)), Map.entry("B", List.of(7)))));
        assertEquals(1 + 24 + 2, MinionAwards.skyBlockXp(data));
    }

    @Test
    void maxedMinionRegistryIsWithinTheCap() {
        var crafted = new ArrayList<Map.Entry<String, List<Integer>>>();
        for (MinionRegistry minion : MinionRegistry.values()) {
            crafted.add(Map.entry(minion.name(), minion.asSkyBlockMinion().getTiers().stream()
                    .map(SkyBlockMinion.MinionTier::tier).toList()));
        }
        int total = MinionAwards.skyBlockXp(new DatapointMinionData.ProfileMinionData(crafted));
        System.out.println("MINION_REGISTRY_MAX_XP=" + total + " minions=" + crafted.size());
        assertEquals(true, total <= SkyBlockXPSource.CRAFT_MINIONS.getMaxXP());
    }

    @Test
    void bankUpgradesAddUpToTheCap() {
        assertEquals(0, BankUpgradeAwards.skyBlockXp(BankAccountTier.STARTER));
        assertEquals(45, BankUpgradeAwards.skyBlockXp(BankAccountTier.DELUXE));
        assertEquals(SkyBlockXPSource.BANK_UPGRADES.getMaxXP(), BankUpgradeAwards.skyBlockXp(BankAccountTier.PALATIAL));
    }

    @Test
    void travelScrollsAreFifteenEachAndSkipUnobtainableOnes() {
        List<String> all = Arrays.stream(TravelScrollType.values()).map(TravelScrollType::getInternalName).toList();
        assertEquals(27 * 15, TravelScrollAwards.skyBlockXp(all));
        assertEquals(30, TravelScrollAwards.skyBlockXp(List.of("castle", "museum", "dragontail", "bogus")));
        assertEquals(SkyBlockXPSource.FAST_TRAVELS_UNLOCKED.getMaxXP(), 24 * TravelScrollAwards.XP_PER_SCROLL);
    }
}
