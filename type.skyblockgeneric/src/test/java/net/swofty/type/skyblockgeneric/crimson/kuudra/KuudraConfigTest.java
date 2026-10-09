package net.swofty.type.skyblockgeneric.crimson.kuudra;

import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraPerkShop;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraTierSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraWave;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraConfigTest {
    static final KuudraConfig CONFIG = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra"));

    @Test
    void everyTierHasASpecMatchingTheCodeTables() {
        assertEquals(KuudraTier.values().length, CONFIG.tiers().size());
        for (KuudraTier tier : KuudraTier.values()) {
            KuudraTierSpec spec = CONFIG.tier(tier);
            assertEquals(tier.ordinal() + 1, spec.number(), tier.name());
            assertEquals(tier.getRequiredReputation(), spec.requiredReputation(), tier.name());
            assertEquals(tier.getFirstClearSkyBlockXP(), spec.firstClearSkyBlockXP(), tier.name());
            assertEquals(KuudraCollectionTier.pointsFor(tier), spec.collectionPoints(), tier.name());
            assertEquals(FactionQuestService.dailyKuudraReputation(tier), spec.dailyReputation(), tier.name());
        }
        assertEquals(List.of(35, 30, 25, 20, 15),
                List.of(KuudraTier.values()).stream().map(tier -> CONFIG.tier(tier).secondsPerWave()).toList());
        assertEquals(22, CONFIG.areaCombatLevel());
        assertEquals(1.5, CONFIG.tier(KuudraTier.INFERNAL).krakenShards());
        assertTrue(CONFIG.tier(KuudraTier.INFERNAL).lairPhase());
        assertFalse(CONFIG.tier(KuudraTier.HOT).stunRequired());
    }

    @Test
    void wavesNameOnlyKnownMobsAndRepeatAfterTwenty() {
        assertEquals(20, CONFIG.waves().size());
        for (KuudraWave wave : CONFIG.waves()) {
            for (KuudraWave.Spawn spawn : wave.spawns()) assertNotNull(CONFIG.mob(spawn.mobId()), spawn.mobId());
        }
        assertEquals(8, CONFIG.wave(20).spawns().getFirst().count());
        assertEquals(CONFIG.wave(1), CONFIG.wave(21));
    }

    @Test
    void sixteenMobsWithStatsForEveryTierTheyAppearIn() {
        assertEquals(16, CONFIG.mobs().size());
        for (KuudraMobSpec mob : CONFIG.mobs()) {
            int expectedTiers = mob.id().equals("MAGMA_BACTERIA") ? 3 : 5;
            assertEquals(expectedTiers, mob.stats().size(), mob.id());
            mob.stats().values().forEach(stats -> assertTrue(stats.health() > 0 && stats.level() > 0, mob.id()));
        }
        KuudraMobSpec dropship = CONFIG.mob("DROPSHIP");
        assertEquals(200_000_000L, dropship.stats(KuudraTier.INFERNAL).health());
        assertEquals(150, dropship.tokens());
        assertEquals(13, CONFIG.mobs().stream().filter(KuudraMobSpec::inBestiary).count());
    }

    @Test
    void perkShopHasTheFourRoutes() {
        KuudraPerkShop shop = CONFIG.perkShop();
        assertEquals(List.of("CANNONEER", "CROWD_CONTROL", "SPECIALIST", "SUPPORT"),
                shop.routes().stream().map(KuudraPerkShop.Route::id).toList());
        assertEquals(5, shop.basic().size());
        assertEquals(40, shop.route("SPECIALIST").humanCannonballCost());
        KuudraPerkShop.Perk proficiency = shop.route("CANNONEER").perks().stream()
                .filter(perk -> perk.id().equals("CANNON_PROFICIENCY")).findFirst().orElseThrow();
        assertEquals(7, proficiency.maxLevel());
        assertEquals(680, proficiency.cost(7));
        assertEquals(50, CONFIG.routeUnlockTokens());
    }

    @Test
    void elleDialogueCoversTheFight() {
        for (String key : List.of("idle", "start_first_time", "start_veterans", "supplies", "build", "ballista_ready",
                "ballista_ready_stun", "elle_eaten", "belly_outside", "belly_inside", "belly_bacteria", "stomach_flooding",
                "stunned", "defeated", "lair_opens", "lair_defeated")) {
            assertFalse(CONFIG.dialogue().lines(key).isEmpty(), key);
        }
        assertEquals("<a><l>Steve has been eaten by Kuudra!",
                CONFIG.dialogue().announcement("player_eaten", Map.of("player", "Steve")));
        assertEquals("<c><l>Steve was FINAL KILLED by Kuudra!",
                CONFIG.dialogue().announcement("final_killed", Map.of("player", "Steve")));
        assertEquals(List.of("<e>[NPC] <c>Elle<f>: Talk with me to begin!"),
                CONFIG.dialogue().chat("idle", RandomGenerator.getDefault()));
        assertEquals(1, CONFIG.dialogue().chat("ballista_damaged", RandomGenerator.getDefault()).size());
        assertEquals("Steve <a>recovered one of Elle's supplies! (3/6)",
                CONFIG.dialogue().announcement("supply_recovered", Map.of("player", "Steve", "count", 3)));
    }

    @Test
    void theStunAndLairSectionsLoad() {
        var fight = CONFIG.fight();
        assertEquals(10, fight.stun().leanInBlocks());
        assertEquals(30, fight.stun().chargedSurfaceSeconds(KuudraTier.INFERNAL));
        assertEquals(2500.0, fight.stun().stunnedHitPoints().get(KuudraTier.BURNING));
        assertEquals(5, fight.stomach().podHitPercent());
        assertEquals(240_000_000, fight.lair().health());
        assertEquals(List.of(132, 292), fight.lair().collapseTicks());
        assertEquals(310, fight.death().finishTicks());
        assertNotNull(CONFIG.arena().stomach());
        assertNotNull(CONFIG.arena().lair());
        assertEquals(2, CONFIG.arena().lair().collapse().size());
        assertEquals(2, CONFIG.arena().stomach().bacteria().size());
    }
}
