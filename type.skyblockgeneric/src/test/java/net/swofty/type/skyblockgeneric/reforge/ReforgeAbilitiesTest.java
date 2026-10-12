package net.swofty.type.skyblockgeneric.reforge;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.reforge.abilities.FangedAbility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReforgeAbilitiesTest {
    private static ReforgeAbilities.Registry registry;

    @BeforeAll
    static void load() {
        registry = ReforgeAbilities.build(Path.of("../configuration/skyblock/reforge_abilities.yml"));
    }

    private static ReforgeAbility ability(String id) {
        return registry.ability(id).orElseThrow(() -> new AssertionError("No ability registered for " + id));
    }

    private static ItemStatistics stats(String id, ReforgeWorld world) {
        return ReforgeEffects.statistics(List.of(ability(id)), new ReforgeContext(world, ItemStatistics.empty()));
    }

    @Test
    void allFiftySixAbilitiesAreImplementedOrUnavailableWithAReason() {
        assertEquals(56, registry.catalogue().size());
        for (ReforgeAbilityCatalogue.Entry entry : registry.catalogue().values()) {
            assertNotNull(entry.text(), entry.reforgeId());
            assertFalse(entry.text().isBlank(), entry.reforgeId());
            ReforgeAbility ability = ability(entry.reforgeId());
            if (ability instanceof UnavailableReforgeAbility unavailable) {
                assertEquals(entry.unavailable(), unavailable.reason());
                assertFalse(unavailable.reason().isBlank(), entry.reforgeId());
            } else {
                assertNull(entry.unavailable(), entry.reforgeId() + " is implemented but still marked unavailable");
            }
        }
    }

    @Test
    void everyImplementationHasACatalogueEntryAndNoDuplicates() {
        Set<String> seen = new java.util.HashSet<>();
        for (ReforgeAbility implementation : ReforgeAbilities.implementations()) {
            assertTrue(seen.add(implementation.reforgeId()), "duplicate " + implementation.reforgeId());
            assertTrue(registry.catalogue().containsKey(implementation.reforgeId()),
                    "no catalogue text for " + implementation.reforgeId());
        }
    }

    @Test
    void perfectGrantsTwoPercentDefense() {
        ItemStatistics granted = stats("perfect", ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_HUB));
        ItemStatistics player = ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, 200D).build();
        assertEquals(204, ItemStatistics.add(player, granted).getOverall(ItemStatistic.DEFENSE), 1e-9);
    }

    @Test
    void fruitfulOnlyGrantsDefenseOnMiningIslands() {
        for (ServerType island : List.of(ServerType.SKYBLOCK_GOLD_MINE, ServerType.SKYBLOCK_DEEP_CAVERNS,
                ServerType.SKYBLOCK_DWARVEN_MINES)) {
            assertEquals(75, stats("fruitful", ReforgeWorld.Fixed.on(island)).getOverall(ItemStatistic.DEFENSE));
        }
        assertEquals(0, stats("fruitful", ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_HUB)).getOverall(ItemStatistic.DEFENSE));
    }

    @Test
    void trashyOnlyGrantsTreasureChanceInTheBayou() {
        assertEquals(1, stats("trashy", ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_BACKWATER_BAYOU))
                .getOverall(ItemStatistic.TREASURE_CHANCE));
        assertEquals(0, stats("trashy", ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_HUB))
                .getOverall(ItemStatistic.TREASURE_CHANCE));
    }

    @Test
    void glacialGrantsTwoMiningFortunePerColdPoint() {
        assertEquals(40, stats("glacial", new ReforgeWorld.Fixed(ServerType.SKYBLOCK_HUB, -20, 0))
                .getOverall(ItemStatistic.MINING_FORTUNE));
        assertEquals(0, stats("glacial", new ReforgeWorld.Fixed(ServerType.SKYBLOCK_HUB, 0, 0))
                .getOverall(ItemStatistic.MINING_FORTUNE));
    }

    @Test
    void thornyGrantsTenthOverbloomPerThornsTier() {
        assertEquals(1.2, stats("thorny", new ReforgeWorld.Fixed(ServerType.SKYBLOCK_HUB, 0, 12))
                .getOverall(ItemStatistic.OVERBLOOM), 1e-9);
    }

    @Test
    void renownedBoostsMagicFindAndEveryCombatStatByOnePercent() {
        ItemStatistics player = ItemStatistics.builder()
                .withBase(ItemStatistic.MAGIC_FIND, 100D).withBase(ItemStatistic.STRENGTH, 300D)
                .withBase(ItemStatistic.SPEED, 100D).build();
        ItemStatistics total = ItemStatistics.add(player, stats("renowned", ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_HUB)));
        assertEquals(101, total.getOverall(ItemStatistic.MAGIC_FIND), 1e-9);
        assertEquals(303, total.getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(100, total.getOverall(ItemStatistic.SPEED), 1e-9);
    }

    @Test
    void saltyDoublesTheReforgeStatsOnlyInJerrysWorkshop() {
        ItemStatistics reforge = ItemStatistics.builder().withBase(ItemStatistic.SEA_CREATURE_CHANCE, 3D).build();
        ReforgeAbility salty = ability("salty");
        ItemStatistics workshop = ReforgeEffects.statistics(List.of(salty),
                new ReforgeContext(ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_JERRYS_WORKSHOP), reforge));
        ItemStatistics hub = ReforgeEffects.statistics(List.of(salty),
                new ReforgeContext(ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_HUB), reforge));
        assertEquals(6, ItemStatistics.add(reforge, workshop).getOverall(ItemStatistic.SEA_CREATURE_CHANCE));
        assertEquals(3, ItemStatistics.add(reforge, hub).getOverall(ItemStatistic.SEA_CREATURE_CHANCE));
    }

    @Test
    void undeadAndCubicReduceDamageFromTheirMobTypesByTwoPercent() {
        ReforgeHit.Incoming undead = new ReforgeHit.Incoming(Set.of(MobType.UNDEAD), false);
        ReforgeHit.Incoming infernal = new ReforgeHit.Incoming(Set.of(MobType.INFERNAL), false);
        assertEquals(0.98, ReforgeEffects.incomingMultiplier(List.of(ability("undead")), undead), 1e-9);
        assertEquals(1, ReforgeEffects.incomingMultiplier(List.of(ability("undead")), infernal), 1e-9);
        assertEquals(0.98, ReforgeEffects.incomingMultiplier(List.of(ability("cubic")), infernal), 1e-9);
    }

    @Test
    void calcifiedAddsDefenseOnlyAgainstSeaCreatures() {
        List<ReforgeAbility> loadout = List.of(ability("calcified"), ability("calcified"));
        ReforgeHit.Incoming seaCreature = new ReforgeHit.Incoming(Set.of(MobType.AQUATIC), true);
        ReforgeHit.Incoming other = new ReforgeHit.Incoming(Set.of(MobType.UNDEAD), false);
        assertEquals(50, ReforgeEffects.bonusDefense(loadout, seaCreature));
        assertEquals(150D / 200D, ReforgeEffects.defenseMultiplier(loadout, seaCreature, 50), 1e-9);
        assertEquals(1, ReforgeEffects.defenseMultiplier(loadout, other, 50), 1e-9);
    }

    @Test
    void fabledOnlyBoostsCritsByUpToFifteenPercent() {
        RandomGenerator max = new FixedRandom(0.999999);
        RandomGenerator mid = new FixedRandom(0.5);
        UUID player = UUID.randomUUID();
        List<ReforgeAbility> loadout = List.of(ability("fabled"));
        assertEquals(1, ReforgeEffects.outgoingMultiplier(loadout, new ReforgeHit.Outgoing(player, Set.of(), false, max)));
        assertEquals(1.075, ReforgeEffects.outgoingMultiplier(loadout, new ReforgeHit.Outgoing(player, Set.of(), true, mid)), 1e-9);
        assertEquals(1.15, ReforgeEffects.outgoingMultiplier(loadout, new ReforgeHit.Outgoing(player, Set.of(), true, max)), 1e-5);
    }

    @Test
    void fangedDoublesEverySeventhHitPerPlayer() {
        FangedAbility fanged = new FangedAbility();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ReforgeHit.Outgoing hit = new ReforgeHit.Outgoing(first, Set.of(), false, new FixedRandom(0));
        for (int i = 1; i <= 14; i++) {
            assertEquals(i % 7 == 0 ? 2 : 1, fanged.outgoingMultiplier(hit), "hit " + i);
        }
        assertEquals(1, fanged.outgoingMultiplier(new ReforgeHit.Outgoing(second, Set.of(), false, new FixedRandom(0))));
    }

    @Test
    void magneticDoublesMiningExperience() {
        assertEquals(2, ReforgeEffects.miningExperienceMultiplier(List.of(ability("magnetic"))));
        assertEquals(1, ReforgeEffects.miningExperienceMultiplier(List.of()));
    }

    @Test
    void dimensionalAndBountifulAndCandiedGiveTheirFlatBonuses() {
        assertEquals(10, ReforgeEffects.titaniumMithrilPowder(List.of(ability("dimensional"))));
        assertEquals(0.4, ReforgeEffects.harvestCoins(List.of(ability("bountiful"), ability("bountiful"))), 1e-9);
        assertEquals(0.01, ReforgeEffects.candyDropBonus(List.of(ability("candied"))), 1e-9);
        assertEquals(0.01, ReforgeEffects.forestWhisperFraction(List.of(ability("marshy"))), 1e-9);
    }

    @Test
    void hyperGrantsFiveSpeedForFiveSeconds() {
        List<ReforgeAbility> loadout = List.of(ability("hyper"));
        assertEquals(5, ReforgeEffects.transmissionBoost(loadout).getOverall(ItemStatistic.SPEED));
        assertEquals(5000, ReforgeEffects.transmissionDurationMillis(loadout));
    }

    @Test
    void stickyOnlyHelpsWaterShards() {
        List<ReforgeAbility> loadout = List.of(ability("sticky"));
        assertEquals(2.5, ReforgeEffects.hunterFortune(loadout, AttributeDefinition.AttributeCategory.WATER));
        assertEquals(0, ReforgeEffects.hunterFortune(loadout, AttributeDefinition.AttributeCategory.FOREST));
    }

    @Test
    void bloodSoakedAddsALevelToLifeStealDrainAndVampirismOnly() {
        List<ReforgeAbility> loadout = List.of(ability("blood_soaked"));
        assertEquals(1, ReforgeEffects.enchantLevelBonus(loadout, EnchantmentType.LIFE_STEAL));
        assertEquals(1, ReforgeEffects.enchantLevelBonus(loadout, EnchantmentType.DRAIN));
        assertEquals(1, ReforgeEffects.enchantLevelBonus(loadout, EnchantmentType.VAMPIRISM));
        assertEquals(0, ReforgeEffects.enchantLevelBonus(loadout, EnchantmentType.SHARPNESS));
    }

    @Test
    void eruditeAddsTwoBurrows() {
        assertEquals(2, ReforgeEffects.extraBurrows(List.of(ability("erudite"))));
    }

    @Test
    void unavailableAbilitiesDoNothing() {
        ReforgeAbility heated = ability("heated");
        assertInstanceOf(UnavailableReforgeAbility.class, heated);
        assertEquals("Needs the Heat mechanic", ((UnavailableReforgeAbility) heated).reason());
        assertEquals(0, stats("heated", ReforgeWorld.Fixed.on(ServerType.SKYBLOCK_CRIMSON_ISLE))
                .getOverall(ItemStatistic.MINING_FORTUNE));
    }

    private record FixedRandom(double value) implements RandomGenerator {
        @Override
        public long nextLong() {
            return 0;
        }

        @Override
        public double nextDouble() {
            return value;
        }
    }
}
