package dev.daweapon.type.skyblockgeneric.staff;

import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryExperience;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.event.actions.custom.ActionSyncSkyBlockXPOnJoin;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPCapacity;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSyncs;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A maxed profile must stay maxed when every task is recomputed from its real progress, as a server join does. */
class MaxProfileReconcileTest {
    private static final double TOLERANCE = 1e-6;

    @BeforeAll
    static void init() throws Exception {
        MaxProfileTest.init();
        SlayerRegistry.loadAll();
        FishingRegistry.loadAll();
    }

    private static SkyBlockPlayer maxed() {
        SkyBlockPlayer player = MaxProfileTest.player();
        assertEquals(java.util.List.of(), MaxStickService.runSteps(player), "every /max profile step must succeed");
        return player;
    }

    private static double expectedMax(DatapointSkyBlockExperience.PlayerSkyBlockExperience experience) {
        return SkyBlockXPSource.TOTAL_MAX_XP + experience.getUncappedSourceXP();
    }

    @Test
    void everyJoinSyncLeavesAMaxedProfileAtTheRealMaximum() {
        SkyBlockPlayer player = maxed();
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        double maxed = experience.getTotalXP();
        assertEquals(expectedMax(experience), maxed, TOLERANCE);
        assertEquals(0, experience.getBonusXP(), "the level must not lean on the admin bonus offset");

        ActionSyncSkyBlockXPOnJoin.reconcile(player);
        assertEquals(maxed, experience.getTotalXP(), TOLERANCE, "a join reconcile lowered a maxed profile");

        for (Consumer<SkyBlockPlayer> sync : SkyBlockXPSyncs.ON_CHANGE) {
            sync.accept(player);
            assertEquals(maxed, experience.getTotalXP(), TOLERANCE, "a data sync lowered a maxed profile");
        }
        assertEquals(expectedMax(experience), experience.getTotalXP(), TOLERANCE);
    }

    @Test
    void everyTaskIsFullAndItsRealProgressCoversAllThisServerCanAward() {
        SkyBlockPlayer player = maxed();
        SkyBlockXPSyncs.syncAll(player);
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();

        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            if (source.isUncapped()) continue;
            assertEquals(source.getMaxXP(), experience.getSourceXP(source), TOLERANCE, source.name());
            assertTrue(experience.getRealSourceXP(source) >= SkyBlockXPCapacity.built(source) - TOLERANCE,
                    source.name() + ": /max profile left real progress below what the server can award");
            assertEquals(SkyBlockXPCapacity.unbuilt(source), experience.getUnbuiltCredit().getOrDefault(source.name(), 0D),
                    TOLERANCE, source.name());
        }
    }

    @Test
    void theMaxedLevelSurvivesAReloadAndAnotherJoin() {
        SkyBlockPlayer player = maxed();
        double maxed = player.getSkyBlockExperience().getTotalXP();

        SkyBlockDataHandler reloaded = SkyBlockDataHandler.createFromProfileOnly(player.getSkyblockDataHandler().toProfileDocument());
        DatapointSkyBlockExperience.PlayerSkyBlockExperience restored = reloaded.get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE,
                DatapointSkyBlockExperience.class).getValue();
        assertEquals(maxed, restored.getTotalXP(), TOLERANCE);
        assertEquals(player.getSkyBlockExperience().getUnbuiltCredit(), restored.getUnbuiltCredit());

        ActionSyncSkyBlockXPOnJoin.reconcile(player);
        ActionSyncSkyBlockXPOnJoin.reconcile(player);
        assertEquals(maxed, player.getSkyBlockExperience().getTotalXP(), TOLERANCE);
    }

    @Test
    void maxedRealProgressReachesTheBuiltBestiaryAndPowderTasks() {
        SkyBlockPlayer player = maxed();
        assertEquals(BestiaryExperience.maxTotalTiers(), BestiaryExperience.totalTiers(player.getBestiaryData()));
        assertEquals(SkyBlockXPSource.HEART_OF_THE_MOUNTAIN.getMaxXP(), HotmService.maxSkyBlockXp(), TOLERANCE,
                "tiers (1,175) and powder (1,942) are both built");
        assertEquals(SkyBlockXPSource.HEART_OF_THE_MOUNTAIN.getMaxXP(),
                player.getSkyBlockExperience().getRealSourceXP(SkyBlockXPSource.HEART_OF_THE_MOUNTAIN), TOLERANCE);
    }
}
