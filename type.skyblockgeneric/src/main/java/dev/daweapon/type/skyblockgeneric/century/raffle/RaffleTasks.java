package dev.daweapon.type.skyblockgeneric.century.raffle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class RaffleTasks {
    public static final int PER_DIFFICULTY = 7;

    private RaffleTasks() {
    }

    public static List<RaffleTask> forPeriod(String eventId, int period) {
        Random random = new Random(eventId.hashCode() * 31L + period);
        List<RaffleTask> chosen = new ArrayList<>();
        for (RaffleTask.Difficulty difficulty : RaffleTask.Difficulty.values()) {
            List<RaffleTask> pool = new ArrayList<>(Arrays.stream(RaffleTask.values())
                    .filter(task -> task.difficulty() == difficulty).toList());
            Collections.shuffle(pool, random);
            chosen.addAll(pool.subList(0, Math.min(PER_DIFFICULTY, pool.size())));
        }
        return chosen;
    }
}
