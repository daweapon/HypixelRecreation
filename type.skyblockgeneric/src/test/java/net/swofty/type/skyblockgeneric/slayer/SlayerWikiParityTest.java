package net.swofty.type.skyblockgeneric.slayer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlayerWikiParityTest {
    @BeforeAll
    static void load() {
        SlayerRegistry.loadAll(new File("../configuration/skyblock/slayers.yml"));
    }

    private static SlayerTierDefinition tier(SlayerType type, SlayerTier tier) {
        return SlayerRegistry.get(type).tiers().get(tier);
    }

    @Test
    void combatXpToSpawnMatchesTheWiki() {
        assertEquals(100, tier(SlayerType.REVENANT_HORROR, SlayerTier.I).requiredCombatXp());
        assertEquals(1000, tier(SlayerType.REVENANT_HORROR, SlayerTier.II).requiredCombatXp());
        assertEquals(10000, tier(SlayerType.REVENANT_HORROR, SlayerTier.V).requiredCombatXp());
        assertEquals(650, tier(SlayerType.TARANTULA_BROODFATHER, SlayerTier.II).requiredCombatXp());
        assertEquals(3000, tier(SlayerType.TARANTULA_BROODFATHER, SlayerTier.IV).requiredCombatXp());
        assertEquals(1500, tier(SlayerType.SVEN_PACKMASTER, SlayerTier.III).requiredCombatXp());
    }

    @Test
    void bossHealthMatchesTheWiki() {
        assertEquals(2500, tier(SlayerType.TARANTULA_BROODFATHER, SlayerTier.I).bossHealth());
        assertEquals(2_500_000, tier(SlayerType.TARANTULA_BROODFATHER, SlayerTier.IV).bossHealth());
        assertEquals(12_000_000, tier(SlayerType.VOIDGLOOM_SERAPH, SlayerTier.II).bossHealth());
        assertEquals(210_000_000, tier(SlayerType.VOIDGLOOM_SERAPH, SlayerTier.IV).bossHealth());
    }
}
