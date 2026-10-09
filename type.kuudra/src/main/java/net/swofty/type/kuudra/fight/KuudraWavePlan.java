package net.swofty.type.kuudra.fight;

import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraWave;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How many of each mob a wave sends (research K1 5.1). Waves 1-20 follow the wiki list; batch sizes the wiki
 * leaves out grow with the party. Later waves repeat the list with bigger batches, add Dropships every few waves
 * and Chaosmites from wave 35.
 */
public final class KuudraWavePlan {
    public record Batch(String mobId, int count) {
    }

    private KuudraWavePlan() {
    }

    public static List<Batch> plan(int wave, List<KuudraWave> waves, int partySize, KuudraFightSpec.Waves spec) {
        if (wave < 1 || waves.isEmpty()) return List.of();
        int cycle = (wave - 1) / waves.size();
        int growth = cycle * spec.cycleGrowth();
        int unsized = spec.batchBase() + Math.max(0, partySize - 1) * spec.batchPerExtraPlayer() + growth;
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (KuudraWave.Spawn spawn : waves.get((wave - 1) % waves.size()).spawns()) {
            counts.merge(spawn.mobId(), spawn.count() > 0 ? spawn.count() + growth : unsized, Integer::sum);
        }
        if (wave > waves.size() && spec.dropshipEvery() > 0 && wave % spec.dropshipEvery() == 0) {
            counts.merge("DROPSHIP", Math.max(1, wave / (spec.dropshipEvery() * 2)), Math::max);
        }
        if (spec.chaosmiteFromWave() > 0 && wave >= spec.chaosmiteFromWave()) {
            counts.merge("CHAOSMITE", 1 + (wave - spec.chaosmiteFromWave()) / 10, Integer::sum);
        }
        List<Batch> batches = new ArrayList<>();
        counts.forEach((mob, count) -> batches.add(new Batch(mob, count)));
        return batches;
    }

    /** How many of a batch may spawn without going over the cap on living mobs. */
    public static int allowed(int alive, int maxAlive, int wanted) {
        return Math.max(0, Math.min(wanted, maxAlive - alive));
    }
}
