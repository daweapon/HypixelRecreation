package net.swofty.type.island.lifecycle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Facing;
import net.swofty.type.skyblockgeneric.chest.ChestBuilder;
import net.swofty.type.skyblockgeneric.chest.ChestType;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleContext;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that places the starter supply chest when a new Island is created.
public class StarterChestStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.CREATE;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        // Resolve the instance now, not inside the delayed lambda: getIslandInstance() is guaranteed
        // non-null here (SkyBlockIsland.getSharedInstance() sets it before running CREATE steps), but
        // a disconnect during the 60-tick delay can null it out via runVacantCheck() in the meantime,
        // which used to NPE this step. The captured Instance itself stays valid either way.
        var instance = context.island().getIslandInstance();

        MathUtility.delay(() -> new ChestBuilder(instance, new Pos(10, 93, 38), ChestType.SINGLE, Facing.WEST)
                .setItem(0, ItemStack.of(Material.DIRT, 5))
                .setItem(1, ItemStack.of(Material.GRASS_BLOCK, 7))
                .setItem(2, ItemStack.of(Material.COBBLESTONE, 8))
                .setItem(3, ItemStack.of(Material.WATER_BUCKET))
                .setItem(4, ItemStack.of(Material.LAVA_BUCKET))
                .setItem(5, ItemStack.of(Material.BONE_MEAL, 3))
                .build(), 60);
    }
}
