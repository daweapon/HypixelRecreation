package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.kuudra.run.KuudraRuns;
import dev.daweapon.type.skyblockgeneric.fishing.FishingReelHooks;
import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;

/** Plugs the fight into the run's wave timer and SkyBlock's death and fishing paths. */
public final class KuudraFightHooks {
    private KuudraFightHooks() {
    }

    public static void register() {
        KuudraRun.waveSpawner = (run, number, _) -> {
            KuudraFight fight = run.fight();
            if (fight != null) fight.spawnWave(number);
        };
        PlayerDeathOverrides.register(player -> {
            KuudraRun run = KuudraRuns.of(player.getUuid());
            if (run == null) return false;
            KuudraFight fight = run.fight();
            if (fight != null) return fight.death(player);
            player.setHealth(player.getMaxHealth());
            return true;
        });
        FishingReelHooks.register((player, bobber) -> {
            KuudraRun run = KuudraRuns.of(player.getUuid());
            KuudraFight fight = run == null ? null : run.fight();
            return fight != null && fight.reel(player, bobber);
        });
    }
}
