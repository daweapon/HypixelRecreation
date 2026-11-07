package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.catacombs.classes.ClassCombat;
import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonGhosts;
import dev.daweapon.type.skyblockgeneric.user.MenuSlotOverride;
import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;

/** Plugs the run's ghost rules into SkyBlock's death path, the class passives into SkyBlock combat and its Magical Map into the SkyBlock Menu slot. */
public final class CatacombsHooks {
    private CatacombsHooks() {
    }

    public static void register() {
        PlayerDeathOverrides.register(player -> {
            CatacombsRun run = CatacombsRuns.of(player.getUuid());
            return run != null && run.death(player);
        });
        DungeonGhosts.use(player -> {
            CatacombsRun run = CatacombsRuns.of(player.getUuid());
            return run != null && run.ghosts().isGhost(player.getUuid());
        });
        ClassCombat.install();
        MenuSlotOverride.register(player -> {
            CatacombsRun run = CatacombsRuns.of(player.getUuid());
            if (run == null || run.lifecycle().stage() != CatacombsLifecycle.Stage.ACTIVE) return null;
            DungeonEngine engine = run.engine();
            return engine == null ? null : engine.map().item();
        });
    }
}
