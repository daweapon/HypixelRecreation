package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.catacombs.classes.ClassCombat;
import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;

/** Plugs the run's ghost rules into SkyBlock's death path and the class passives into SkyBlock combat. */
public final class CatacombsHooks {
    private CatacombsHooks() {
    }

    public static void register() {
        PlayerDeathOverrides.register(player -> {
            CatacombsRun run = CatacombsRuns.of(player.getUuid());
            return run != null && run.death(player);
        });
        ClassCombat.install();
    }
}
