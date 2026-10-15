package dev.daweapon.type.island.lifecycle;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import dev.daweapon.type.skyblockgeneric.utility.JerryInformation;

// Island lifecycle step that sets Jerry's default spawn position when a new Island is created.
public class JerryDefaultsStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.CREATE;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        context.island().setJerryInformation(new JerryInformation(null, new Pos(9.5, 100, 33.5, 145, 0), null));
    }
}
