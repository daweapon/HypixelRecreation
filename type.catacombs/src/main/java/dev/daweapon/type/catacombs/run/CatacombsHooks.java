package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;

/** Plugs the run's ghost rules into SkyBlock's death path. */
public final class CatacombsHooks {
    private CatacombsHooks() {
    }

    public static void register() {
        PlayerDeathOverrides.register(player -> {
            CatacombsRun run = CatacombsRuns.of(player.getUuid());
            return run != null && run.death(player);
        });
    }
}
