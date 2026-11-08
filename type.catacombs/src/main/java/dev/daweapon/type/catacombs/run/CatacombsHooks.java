package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.catacombs.classes.ClassCombat;
import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonGhosts;
import dev.daweapon.type.skyblockgeneric.user.MenuSlotOverride;
import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;

/** Plugs the run's ghost rules into SkyBlock's death path, the class passives into SkyBlock combat and its Magical Map, then the Score Summary map, into the SkyBlock Menu slot. */
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
            if (run == null) return null;
            if (run.lifecycle().stage() == CatacombsLifecycle.Stage.ENDING) return run.scoreMap(player.getUuid());
            if (run.lifecycle().stage() != CatacombsLifecycle.Stage.ACTIVE) return null;
            DungeonEngine engine = run.engine();
            return engine == null ? null : engine.map().item();
        });
    }
}
