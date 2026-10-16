package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.communityupgrades.IslandBorder;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

public class IslandBorderLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        if (context.memberProfiles().isEmpty()) return;
        IslandBorder.apply(context.island(), IslandBorder.sizeForProfile(context.memberProfiles().getFirst()));
    }
}
