package dev.daweapon.type.skyblockgeneric.minion.crop;

import lombok.Getter;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDropTable;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.daweapon.type.skyblockgeneric.minion.drops.MinionDrop.between;
import static dev.daweapon.type.skyblockgeneric.minion.drops.MinionDrop.of;

/**
 * What a farming minion grows: the fully grown block it plants on each tile, the soils it works on,
 * and, for crops that need farmland, the block it tills plain soil into first.
 */
@Getter
public enum MinionCrop {
    WHEAT(List.of(planting(Block.WHEAT.withProperty("age", "7"), MinionDropTable.of(of(ItemType.WHEAT, 1), between(ItemType.SEEDS, 1, 2)))),
            Soils.FARMABLE, Block.FARMLAND),
    CARROT(List.of(planting(Block.CARROTS.withProperty("age", "7"), MinionDropTable.of(between(ItemType.CARROT, 2, 4)))),
            Soils.FARMABLE, Block.FARMLAND),
    POTATO(List.of(planting(Block.POTATOES.withProperty("age", "7"), MinionDropTable.of(between(ItemType.POTATO, 2, 4)))),
            Soils.FARMABLE, Block.FARMLAND),
    PUMPKIN(List.of(planting(Block.PUMPKIN, MinionDropTable.of(of(ItemType.PUMPKIN, 1)))),
            Soils.FARMABLE, null),
    MELON(List.of(planting(Block.MELON, MinionDropTable.of(between(ItemType.MELON_SLICE, 3, 7)))),
            Soils.FARMABLE, null),
    SUGAR_CANE(List.of(planting(Block.SUGAR_CANE, MinionDropTable.of(of(ItemType.SUGAR_CANE, 3)))),
            List.of(Block.GRASS_BLOCK, Block.DIRT, Block.SAND, Block.RED_SAND), null),
    CACTUS(List.of(planting(Block.CACTUS, MinionDropTable.of(of(ItemType.CACTUS, 3)))),
            List.of(Block.SAND, Block.RED_SAND), null),
    MUSHROOM(List.of(
            planting(Block.RED_MUSHROOM, MinionDropTable.of(of(ItemType.RED_MUSHROOM, 1))),
            planting(Block.BROWN_MUSHROOM, MinionDropTable.of(of(ItemType.BROWN_MUSHROOM, 1)))),
            List.of(Block.MYCELIUM, Block.PODZOL), null),
    NETHER_WART(List.of(planting(Block.NETHER_WART.withProperty("age", "3"), MinionDropTable.of(between(ItemType.NETHER_WART, 2, 3)))),
            List.of(Block.SOUL_SAND), null);

    private final List<CropPlanting> plantings;
    private final List<Block> soils;
    private final @Nullable Block tilledSoil;

    MinionCrop(List<CropPlanting> plantings, List<Block> soils, @Nullable Block tilledSoil) {
        this.plantings = plantings;
        this.soils = soils;
        this.tilledSoil = tilledSoil;
    }

    private static CropPlanting planting(Block grown, MinionDropTable drops) {
        return new CropPlanting(grown, drops);
    }

    public boolean acceptsSoil(Block block) {
        return soils.stream().anyMatch(soil -> soil.id() == block.id());
    }

    public boolean needsTilling(Block block) {
        return tilledSoil != null && block.id() != tilledSoil.id() && acceptsSoil(block);
    }

    public Optional<CropPlanting> plantingOf(Block block) {
        return plantings.stream().filter(planting -> planting.matches(block)).findFirst();
    }

    public boolean acceptsCropTile(Block block) {
        return block.air() || plantingOf(block).isPresent();
    }

    public Set<ItemType> producedItems() {
        Set<ItemType> items = new LinkedHashSet<>();
        plantings.forEach(planting -> items.addAll(planting.drops().producedItems()));
        return items;
    }

    private static final class Soils {
        private static final List<Block> FARMABLE = List.of(Block.DIRT, Block.GRASS_BLOCK, Block.FARMLAND);
    }
}
