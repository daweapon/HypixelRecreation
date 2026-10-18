package dev.daweapon.type.skyblockgeneric.minion.actions;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.crop.CropFieldExpectation;
import dev.daweapon.type.skyblockgeneric.minion.crop.CropPlanting;
import dev.daweapon.type.skyblockgeneric.minion.crop.MinionCrop;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hypixel's farming loop: till any plain soil, plant a grown crop on every empty tile, and once the
 * field is full harvest one tile per action, so resources arrive every other action.
 */
public class MinionCropAction extends MinionAction {
    private final MinionCrop crop;

    public MinionCropAction(MinionCrop crop) {
        this.crop = crop;
    }

    @Override
    public @NotNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island) {
        List<Pos> tiles = CropFieldExpectation.cropTiles(minion);

        List<Pos> untilled = tiles.stream()
                .map(tile -> tile.sub(0, 1, 0))
                .filter(soil -> crop.needsTilling(island.getBlock(soil)))
                .toList();
        if (!untilled.isEmpty()) {
            Pos soil = MathUtility.getRandomElement(untilled);
            event.setToLook(soil);
            event.setAction(() -> {
                if (!crop.needsTilling(island.getBlock(soil))) return;
                minion.getMinionEntity().swingAnimation();
                island.setBlock(soil, crop.getTilledSoil());
            });
            return List.of();
        }

        List<Pos> empty = tiles.stream().filter(tile -> island.getBlock(tile).air()).toList();
        if (!empty.isEmpty()) {
            Pos tile = MathUtility.getRandomElement(empty);
            Block grown = choosePlanting().grown();
            event.setToLook(tile);
            event.setAction(() -> {
                if (!island.getBlock(tile).air()) return;
                minion.getMinionEntity().placeAnimation();
                island.setBlock(tile, grown);
            });
            return List.of();
        }

        Pos tile = MathUtility.getRandomElement(tiles);
        CropPlanting harvested = crop.plantingOf(island.getBlock(tile)).orElse(null);
        event.setToLook(tile);
        if (harvested == null) {
            event.setAction(() -> {});
            return List.of();
        }
        event.setAction(() -> {
            minion.getMinionEntity().swingAnimation();
            island.setBlock(tile, Block.AIR);
        });
        return harvested.drops().rollItems();
    }

    @Override
    public List<SkyBlockItem> offlineYield(IslandMinionData.IslandMinion minion) {
        return choosePlanting().drops().rollItems();
    }

    @Override
    public boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island) {
        return isStorageFull(minion);
    }

    private CropPlanting choosePlanting() {
        List<CropPlanting> plantings = crop.getPlantings();
        if (plantings.size() == 1) return plantings.getFirst();
        return LootRoll.pick(null, plantings, planting -> planting.drops().drops().getFirst().item(),
                planting -> 1, ThreadLocalRandom.current());
    }
}
