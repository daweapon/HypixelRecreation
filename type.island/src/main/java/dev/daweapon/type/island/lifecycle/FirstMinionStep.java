package dev.daweapon.type.island.lifecycle;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionRegistry;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that spawns the free starter Cobblestone Minion when a new Island is created.
public class FirstMinionStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.CREATE;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        MathUtility.delay(() -> {
            IslandMinionData.IslandMinion minion = context.island().getMinionData().initializeMinion(
                    new Pos(3, 100, 36),
                    MinionRegistry.COBBLESTONE,
                    new ItemAttributeMinionData.MinionData(1, 0),
                    false
            );

            context.island().getMinionData().spawn(minion);
        }, 40);
    }
}
