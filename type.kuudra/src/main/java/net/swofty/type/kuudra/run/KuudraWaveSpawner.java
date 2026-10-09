package net.swofty.type.kuudra.run;

import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraWave;

/** Spawns a wave's mobs into a run. Mobs come in a later phase; until then waves only advance the counter. */
@FunctionalInterface
public interface KuudraWaveSpawner {
    KuudraWaveSpawner NONE = (_, _, _) -> {
    };

    void spawn(KuudraRun run, int number, KuudraWave wave);
}
