package net.swofty.type.kuudra.fight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

/**
 * Building the Ballista from the six supply piles (research K1 3.2). Sneak + left-clicking a pile keeps a player
 * building it for a short while; each builder adds progress to their pile at the base rate times their own speed
 * (Ballista Mechanic, Fresh Tools, organs). Overall progress is the average of the piles. Mob hits and tentacle
 * slams knock piles back down.
 */
public final class BallistaBuild {
    private record Builder(String pile, long untilTick) {
    }

    private final Map<String, Double> progress = new LinkedHashMap<>();
    private final Map<UUID, Builder> builders = new ConcurrentHashMap<>();
    private final double percentPerTick;

    public BallistaBuild(Collection<String> piles, double pileSeconds) {
        if (piles.isEmpty() || pileSeconds <= 0) throw new IllegalArgumentException("A Ballista needs piles and a build time");
        piles.forEach(pile -> progress.put(pile, 0D));
        this.percentPerTick = 100 / (pileSeconds * 20);
    }

    public boolean click(UUID player, String pile, long tick, int holdTicks) {
        if (!progress.containsKey(pile) || progress.get(pile) >= 100) return false;
        builders.put(player, new Builder(pile, tick + holdTicks));
        return true;
    }

    /** Advances one tick; returns the piles that just reached 100%. */
    public List<String> tick(long tick, ToDoubleFunction<UUID> speedMultiplier) {
        List<String> finished = new ArrayList<>();
        builders.entrySet().removeIf(entry -> entry.getValue().untilTick() < tick);
        for (Map.Entry<UUID, Builder> entry : builders.entrySet()) {
            String pile = entry.getValue().pile();
            double before = progress.get(pile);
            if (before >= 100) continue;
            double after = Math.min(100, before + percentPerTick * speedMultiplier.applyAsDouble(entry.getKey()));
            progress.put(pile, after);
            if (after >= 100) finished.add(pile);
        }
        builders.entrySet().removeIf(entry -> progress.get(entry.getValue().pile()) >= 100);
        return finished;
    }

    public void knock(String pile, double points) {
        progress.computeIfPresent(pile, (_, value) -> value >= 100 ? value : Math.max(0, value - points));
    }

    /** Knocks a random pile that is under way but not finished; returns it, or null if there is none. */
    public String knockAny(double points, RandomGenerator random) {
        List<String> started = progress.entrySet().stream()
                .filter(entry -> entry.getValue() > 0 && entry.getValue() < 100)
                .map(Map.Entry::getKey)
                .toList();
        if (started.isEmpty()) return null;
        String pile = started.get(random.nextInt(started.size()));
        knock(pile, points);
        return pile;
    }

    /** Unfinished piles nobody is working on slowly fall back (seen in the Burning capture). */
    public void decayIdle(double points) {
        for (Map.Entry<String, Double> entry : progress.entrySet()) {
            double value = entry.getValue();
            if (value <= 0 || value >= 100) continue;
            boolean worked = builders.values().stream().anyMatch(builder -> builder.pile().equals(entry.getKey()));
            if (!worked) entry.setValue(Math.max(0, value - points));
        }
    }

    public void completeAll() {
        progress.replaceAll((_, _) -> 100D);
        builders.clear();
    }

    public double progress(String pile) {
        return progress.getOrDefault(pile, 0D);
    }

    public double overall() {
        return progress.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public int overallPercent() {
        return (int) Math.floor(overall());
    }

    public boolean complete() {
        return progress.values().stream().allMatch(value -> value >= 100);
    }

    public int helping() {
        return builders.size();
    }

    public boolean isBuilding(UUID player) {
        return builders.containsKey(player);
    }
}
