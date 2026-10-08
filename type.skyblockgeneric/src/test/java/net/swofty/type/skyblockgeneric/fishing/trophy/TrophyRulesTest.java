package net.swofty.type.skyblockgeneric.fishing.trophy;

import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrophyRulesTest {

    @BeforeAll
    static void load() {
        FishingRegistry.loadAll(new File("../configuration/skyblock/fishing"));
    }

    private static TrophyFishDefinition fish(String id) {
        return FishingRegistry.getTrophyFish(id);
    }

    @Test
    void goldenFishSurfacesBetweenEightAndTwelveMinutes() {
        assertEquals(0D, GoldenFishService.surfaceChance(7 * 60_000L, GoldenFishService.ELIGIBLE_AFTER_MS));
        assertEquals(50D, GoldenFishService.surfaceChance(10 * 60_000L, GoldenFishService.ELIGIBLE_AFTER_MS), 1e-9);
        assertEquals(100D, GoldenFishService.surfaceChance(13 * 60_000L, GoldenFishService.ELIGIBLE_AFTER_MS));
    }

    @Test
    void volcanoFishUseTheVolcanoConditions() {
        assertTrue(fish("SULPHUR_SKITTER").conditions().stream().anyMatch(c -> c instanceof TrophyCondition.NearBlock));
        assertTrue(fish("STEAMING_HOT_FLOUNDER").conditions().stream().anyMatch(c -> c instanceof TrophyCondition.NearGeyser));
        assertTrue(fish("GUSHER").conditions().stream().anyMatch(c -> c instanceof TrophyCondition.AfterEruption));
        assertEquals(4D, fish("VOLCANIC_STONEFISH").catchChance());
    }

    @Test
    void skyBlockXpCountsLowerTiersAndMaxesAt1080() {
        var data = new net.swofty.type.skyblockgeneric.data.datapoints.DatapointTrophyFish.TrophyFishData();
        data.getProgress("BLOBFISH").increment("GOLD");
        assertEquals(28, TrophyFishAwards.skyBlockXp(data));
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) data.getProgress(fish.id()).increment("DIAMOND");
        assertEquals(1080, TrophyFishAwards.skyBlockXp(data));
        assertTrue(TrophyFisherRank.NOVICE.achieved(data));
        assertTrue(TrophyFisherRank.EXPERT.achieved(data));
        assertEquals(false, TrophyFisherRank.MASTER.achieved(data));
    }

    @Test
    void anyTierOfTheObfuscatedFishWorksAsBait() {
        TrophyCondition.UsingBait bait = (TrophyCondition.UsingBait) fish("OBFUSCATED_3").conditions().getFirst();
        assertEquals(4, bait.baitIds().size());
    }
}
