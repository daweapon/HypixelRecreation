package net.swofty.type.skyblockgeneric.deployable;

import java.util.Comparator;
import java.util.List;

public final class DeployableMath {
    private DeployableMath() {
    }

    public record Candidate(Object id, double distance) {
    }

    public record Buff(int rank, long placedAt, double distance) {
    }

    public static boolean inRange(double distance, double radius) {
        return distance <= radius;
    }

    public static List<Object> recipients(List<Candidate> candidates, double radius, int max) {
        return candidates.stream()
                .filter(candidate -> inRange(candidate.distance(), radius))
                .sorted(Comparator.comparingDouble(Candidate::distance))
                .limit(max)
                .map(Candidate::id)
                .toList();
    }

    public static int strongest(List<Buff> buffs) {
        int best = -1;
        for (int i = 0; i < buffs.size(); i++) {
            if (best < 0 || beats(buffs.get(i), buffs.get(best))) best = i;
        }
        return best;
    }

    public static double durationSeconds(int baseSeconds, boolean doubled) {
        return doubled ? baseSeconds * 2D : baseSeconds;
    }

    public static double healthRegenMultiplier(double healthRegenStat) {
        return Math.max(0, healthRegenStat / 100D);
    }

    public static float regenPerPulse(double maxHealth, double healthRegenStat, double percentBonus) {
        double base = 1.5 + ((int) maxHealth * 0.01);
        return (float) (base * healthRegenMultiplier(healthRegenStat) + base * percentBonus);
    }

    public static double manaPerSecond(double maxMana, double bonusFraction) {
        return maxMana / 50D + (int) (maxMana / 50D * bonusFraction);
    }

    public static int manaCost(double maxMana, double fraction) {
        return (int) Math.round(maxMana * fraction);
    }

    private static boolean beats(Buff challenger, Buff holder) {
        if (challenger.rank() != holder.rank()) return challenger.rank() > holder.rank();
        if (challenger.placedAt() != holder.placedAt()) return challenger.placedAt() < holder.placedAt();
        return challenger.distance() < holder.distance();
    }
}
