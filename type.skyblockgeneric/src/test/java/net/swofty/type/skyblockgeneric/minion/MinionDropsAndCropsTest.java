package net.swofty.type.skyblockgeneric.minion;

import net.minestom.server.instance.block.Block;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.minion.actions.MinionCropAction;
import net.swofty.type.skyblockgeneric.minion.actions.MinionKillMobAction;
import net.swofty.type.skyblockgeneric.minion.crop.CropFieldExpectation;
import net.swofty.type.skyblockgeneric.minion.crop.MinionCrop;
import net.swofty.type.skyblockgeneric.minion.drops.MinionDrop;
import net.swofty.type.skyblockgeneric.minion.drops.MinionDropTable;
import net.swofty.type.skyblockgeneric.minion.drops.MinionDropTable.Drop;
import net.swofty.type.skyblockgeneric.minion.minions.combat.MinionBlaze;
import net.swofty.type.skyblockgeneric.minion.minions.combat.MinionCaveSpider;
import net.swofty.type.skyblockgeneric.minion.minions.combat.MinionGhast;
import net.swofty.type.skyblockgeneric.minion.minions.combat.MinionMagmaCube;
import net.swofty.type.skyblockgeneric.minion.minions.combat.MinionSpider;
import net.swofty.type.skyblockgeneric.minion.minions.combat.MinionZombie;
import net.swofty.type.skyblockgeneric.minion.minions.farming.MinionNetherWart;
import net.swofty.type.skyblockgeneric.minion.minions.farming.MinionWheat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinionDropsAndCropsTest {
    private static final RandomGenerator LOWEST = () -> 0L;
    private static final RandomGenerator HIGHEST = () -> -1L;

    @ParameterizedTest
    @EnumSource(MinionRegistry.class)
    void everyMinionHasAnActionAndPlacementRules(MinionRegistry registry) {
        SkyBlockMinion minion = registry.asSkyBlockMinion();
        assertDoesNotThrow(minion::getAction);
        assertDoesNotThrow(minion::getExpectations);
    }

    @Test
    void crimsonCombatMinionsDropTheirWikiItems() {
        assertEquals(List.of(new Drop(ItemType.BLAZE_ROD, 1)), MinionBlaze.DROPS.roll(HIGHEST));
        assertEquals(List.of(new Drop(ItemType.GHAST_TEAR, 1)), MinionGhast.DROPS.roll(HIGHEST));
        assertEquals(List.of(new Drop(ItemType.MAGMA_CREAM, 1)), MinionMagmaCube.DROPS.roll(LOWEST));
        assertEquals(List.of(new Drop(ItemType.MAGMA_CREAM, 3)), MinionMagmaCube.DROPS.roll(HIGHEST));
    }

    @Test
    void chanceDropsRollThroughLootRoll() {
        assertEquals(List.of(new Drop(ItemType.STRING, 1), new Drop(ItemType.SPIDER_EYE, 1)), MinionSpider.DROPS.roll(LOWEST));
        assertEquals(List.of(new Drop(ItemType.STRING, 1)), MinionSpider.DROPS.roll(HIGHEST));
        assertEquals(List.of(new Drop(ItemType.SPIDER_EYE, 1)), MinionCaveSpider.DROPS.roll(HIGHEST));
        assertEquals(List.of(new Drop(ItemType.ROTTEN_FLESH, 1)), MinionZombie.DROPS.roll(HIGHEST));
    }

    @Test
    void dropRangesStayWithinBounds() {
        MinionDropTable table = MinionDropTable.of(MinionDrop.between(ItemType.CARROT, 2, 4));
        assertEquals(2, table.roll(LOWEST).getFirst().amount());
        assertEquals(4, table.roll(HIGHEST).getFirst().amount());
        assertEquals(Set.of(ItemType.CARROT), table.producedItems());
    }

    @Test
    void cropMinionsUseTheGenericCropAction() {
        assertInstanceOf(MinionCropAction.class, new MinionWheat().getAction());
        assertInstanceOf(CropFieldExpectation.class, new MinionNetherWart().getExpectations().getFirst());
        assertInstanceOf(MinionKillMobAction.class, new MinionBlaze().getAction());
    }

    @Test
    void wheatTillsDirtAndGivesWheatAndSeeds() {
        assertTrue(MinionCrop.WHEAT.needsTilling(Block.DIRT));
        assertTrue(MinionCrop.WHEAT.needsTilling(Block.GRASS_BLOCK));
        assertFalse(MinionCrop.WHEAT.needsTilling(Block.FARMLAND));
        assertTrue(MinionCrop.WHEAT.acceptsSoil(Block.FARMLAND));
        assertFalse(MinionCrop.WHEAT.acceptsSoil(Block.STONE));
        assertEquals(Set.of(ItemType.WHEAT, ItemType.SEEDS), MinionCrop.WHEAT.producedItems());
        assertTrue(MinionCrop.WHEAT.acceptsCropTile(Block.WHEAT.withProperty("age", "7")));
        assertTrue(MinionCrop.WHEAT.acceptsCropTile(Block.AIR));
        assertFalse(MinionCrop.WHEAT.acceptsCropTile(Block.STONE));
    }

    @Test
    void cropsNeedTheirOwnSoil() {
        assertTrue(MinionCrop.NETHER_WART.acceptsSoil(Block.SOUL_SAND));
        assertFalse(MinionCrop.NETHER_WART.acceptsSoil(Block.DIRT));
        assertFalse(MinionCrop.NETHER_WART.needsTilling(Block.SOUL_SAND));
        assertTrue(MinionCrop.CACTUS.acceptsSoil(Block.SAND));
        assertFalse(MinionCrop.CACTUS.acceptsSoil(Block.GRASS_BLOCK));
        assertTrue(MinionCrop.MUSHROOM.acceptsSoil(Block.MYCELIUM));
        assertTrue(MinionCrop.MUSHROOM.acceptsSoil(Block.PODZOL));
        assertTrue(MinionCrop.SUGAR_CANE.acceptsSoil(Block.GRASS_BLOCK));
        assertFalse(MinionCrop.PUMPKIN.needsTilling(Block.DIRT));
    }

    @Test
    void mushroomHarvestMatchesThePlantedColour() {
        assertEquals(Set.of(ItemType.RED_MUSHROOM, ItemType.BROWN_MUSHROOM), MinionCrop.MUSHROOM.producedItems());
        assertEquals(ItemType.BROWN_MUSHROOM, MinionCrop.MUSHROOM.plantingOf(Block.BROWN_MUSHROOM).orElseThrow()
                .drops().roll(LOWEST).getFirst().item());
    }

    @Test
    void cropYieldsMatchTheWiki() {
        assertEquals(List.of(new Drop(ItemType.NETHER_WART, 3)),
                MinionCrop.NETHER_WART.getPlantings().getFirst().drops().roll(HIGHEST));
        assertEquals(List.of(new Drop(ItemType.MELON_SLICE, 7)),
                MinionCrop.MELON.getPlantings().getFirst().drops().roll(HIGHEST));
        assertEquals(List.of(new Drop(ItemType.SUGAR_CANE, 3)),
                MinionCrop.SUGAR_CANE.getPlantings().getFirst().drops().roll(LOWEST));
    }
}
