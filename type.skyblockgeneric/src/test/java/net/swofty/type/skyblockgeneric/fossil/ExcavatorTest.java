package net.swofty.type.skyblockgeneric.fossil;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExcavatorTest {
    private static final UUID PLAYER = UUID.randomUUID();
    private static final ChiselStats COMMON = ChiselStats.of(ItemType.CHISEL, List.of());

    private static ExcavatorGame gameWithFossil() {
        for (long seed = 0; seed < 2_000; seed++) {
            ExcavatorGame game = ExcavatorGame.start(PLAYER, COMMON, 0, new Random(seed));
            if (game.hasFossil()) return game;
        }
        throw new AssertionError("no seed produced a fossil");
    }

    private static ExcavatorGame gameWithoutFossil() {
        for (long seed = 0; seed < 2_000; seed++) {
            ExcavatorGame game = ExcavatorGame.start(PLAYER, COMMON, 0, new Random(seed));
            if (!game.hasFossil()) return game;
        }
        throw new AssertionError("every seed produced a fossil");
    }

    @Test
    void treasureWeightsAddUpToTheWikisTotal() {
        assertEquals(6_475, ExcavatorLoot.totalWeight(), 1e-9);
    }

    @Test
    void treasureTileCountIsBetweenTenAndFourteen() {
        for (long seed = 0; seed < 200; seed++) {
            int count = ExcavatorLoot.rollCount(new Random(seed));
            assertTrue(count >= 10 && count <= 14, "count " + count);
        }
    }

    @Test
    void rolledAmountsStayInsideTheirRange() {
        Random random = new Random(7);
        for (int i = 0; i < 5_000; i++) {
            ExcavatorLoot.Drop drop = ExcavatorLoot.roll(PLAYER, random);
            switch (drop.kind()) {
                case POWDER -> assertTrue(drop.amount() >= 1_000 && drop.amount() <= 10_000);
                case ESSENCE -> assertTrue(List.of(10, 20, 40).contains(drop.amount()));
                case BOOK -> assertEquals(1, drop.amount());
                case ITEM -> assertNotNull(drop.item());
            }
        }
    }

    @Test
    void aFossilGameHidesTheFullShapeAndNeverUnderTreasure() {
        ExcavatorGame game = gameWithFossil();
        int fossilTiles = 0;
        for (int i = 0; i < ExcavatorGame.SIZE; i++) {
            if (game.isFossilTile(i)) {
                fossilTiles++;
                assertNull(game.treasureAt(i), "tile " + i);
            }
        }
        assertEquals(game.fossil().tileCount(), fossilTiles);
        assertTrue(FossilType.excavatorPool().contains(game.fossil()));
    }

    @Test
    void uncoveringTheWholeFossilCompletesTheDigAndPaysTheFossil() {
        ExcavatorGame game = gameWithFossil();
        int before = game.charges();
        int dug = 0;
        for (int i = 0; i < ExcavatorGame.SIZE; i++) {
            if (!game.isFossilTile(i)) continue;
            assertEquals(ExcavatorGame.Dig.FOSSIL, game.dig(i));
            dug++;
        }
        assertTrue(game.isCompleted());
        assertTrue(game.isOver());
        assertEquals(100.0, game.progressPercent(), 1e-9);
        assertEquals(before - dug, game.charges());
    }

    @Test
    void everyClickCostsOneChargeAndTheDigEndsAtZero() {
        ExcavatorGame game = gameWithoutFossil();
        assertEquals(16, game.charges());
        for (int i = 0; i < 16; i++) {
            assertFalse(game.isOver());
            assertNotEquals(ExcavatorGame.Dig.OVER, game.dig(i));
        }
        assertTrue(game.isOver());
        assertFalse(game.isCompleted());
        assertEquals(ExcavatorGame.Dig.OVER, game.dig(30));
        assertEquals(0, game.charges());
    }

    @Test
    void clearingATileTwiceCostsNothing() {
        ExcavatorGame game = gameWithoutFossil();
        game.dig(0);
        int charges = game.charges();
        assertEquals(ExcavatorGame.Dig.ALREADY_CLEARED, game.dig(0));
        assertEquals(charges, game.charges());
    }

    @Test
    void onlyUncoveredTreasureIsPaid() {
        ExcavatorGame game = gameWithoutFossil();
        assertTrue(game.uncoveredRewards().isEmpty());
        int treasureTile = -1;
        for (int i = 0; i < ExcavatorGame.SIZE; i++) if (game.treasureAt(i) != null) { treasureTile = i; break; }
        assertTrue(treasureTile >= 0);
        assertEquals(ExcavatorGame.Dig.TREASURE, game.dig(treasureTile));
        assertEquals(1, game.uncoveredRewards().size());
        assertSame(game.treasureAt(treasureTile).kind(), game.uncoveredRewards().getFirst().kind());
    }

    @Test
    void aFossilIsFoundOnAboutTenPercentOfDigs() {
        int found = 0;
        for (long seed = 0; seed < 5_000; seed++) {
            if (ExcavatorGame.start(PLAYER, COMMON, 0, new Random(seed)).hasFossil()) found++;
        }
        assertEquals(500, found, 100);
    }

    @Test
    void theExcavatorNeverRollsClawOrHelix() {
        for (long seed = 0; seed < 3_000; seed++) {
            FossilType fossil = ExcavatorGame.start(PLAYER, COMMON, 0, new Random(seed)).fossil();
            if (fossil != null) assertTrue(fossil != FossilType.CLAW && fossil != FossilType.HELIX);
        }
        assertEquals(6, FossilType.excavatorPool().size());
    }

    @Test
    void chiselChargesGrowWithTheTierAndSleightOfHand() {
        assertEquals(16, ExcavatorGame.start(PLAYER, ChiselStats.of(ItemType.CHISEL, List.of()), 0, new Random(1)).charges());
        assertEquals(22, ExcavatorGame.start(PLAYER, ChiselStats.of(ItemType.PERFECT_CHISEL, List.of()), 0, new Random(1)).charges());
        assertEquals(20, ExcavatorGame.start(PLAYER, ChiselStats.of(ItemType.GLACITE_CHISEL, List.of()), 0, new Random(1)).charges());
        assertEquals(17, ExcavatorGame.start(PLAYER, COMMON, 1, new Random(1)).charges());
    }

    @Test
    void aPerfectAquamarineAlwaysAddsACharge() {
        ChiselStats stats = ChiselStats.of(ItemType.PERFECT_CHISEL, List.of(ItemType.PERFECT_AQUAMARINE_GEM));
        assertEquals(100, stats.extraChargeChance(), 1e-9);
        for (long seed = 0; seed < 50; seed++) assertEquals(23, ExcavatorGame.start(PLAYER, stats, 0, new Random(seed)).charges());
    }

    @Test
    void gemEffectsStackByRarityAndType() {
        ChiselStats stats = ChiselStats.of(ItemType.PERFECT_CHISEL, List.of(
                ItemType.PERFECT_CITRINE_GEM, ItemType.PERFECT_CITRINE_GEM, ItemType.FLAWED_PERIDOT_GEM));
        assertEquals(200, stats.highlightChance(), 1e-9);
        assertEquals(40, stats.essenceBonus(), 1e-9);
        assertEquals(0, stats.extraTreasureChance(), 1e-9);
        assertEquals(60, ChiselStats.of(ItemType.REINFORCED_CHISEL, List.of(ItemType.FLAWLESS_CITRINE_GEM)).highlightChance(), 1e-9);
    }

    @Test
    void twoPerfectCitrinesAlwaysHighlightTwoTreasures() {
        ChiselStats stats = ChiselStats.of(ItemType.GLACITE_CHISEL, List.of(ItemType.PERFECT_CITRINE_GEM, ItemType.PERFECT_CITRINE_GEM));
        ExcavatorGame game = ExcavatorGame.start(PLAYER, stats, 0, new Random(3));
        int highlighted = 0;
        for (int i = 0; i < ExcavatorGame.SIZE; i++) if (game.isHighlighted(i)) highlighted++;
        assertEquals(2, highlighted);
    }

    @Test
    void peridotRaisesEssenceDrops() {
        ChiselStats stats = ChiselStats.of(ItemType.PERFECT_CHISEL, List.of(ItemType.PERFECT_PERIDOT_GEM));
        for (long seed = 0; seed < 400; seed++) {
            ExcavatorGame game = ExcavatorGame.start(PLAYER, stats, 0, new Random(seed));
            for (int i = 0; i < ExcavatorGame.SIZE; i++) {
                ExcavatorLoot.Drop drop = game.treasureAt(i);
                if (drop == null || drop.kind() != ExcavatorLoot.Kind.ESSENCE) continue;
                game.dig(i);
                assertEquals(drop.amount() * 2, game.uncoveredRewards().getFirst().amount());
                return;
            }
        }
        fail("no essence tile found");
    }

    @Test
    void nonChiselItemsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ChiselStats.of(ItemType.SUSPICIOUS_SCRAP, List.of()));
        assertFalse(ChiselStats.isChisel(ItemType.SUSPICIOUS_SCRAP));
        assertEquals(10, ChiselStats.of(ItemType.PERFECT_CHISEL, List.of()).hotmTier());
    }
}
