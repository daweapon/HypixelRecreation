package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;

import java.util.List;

public record HotfPerk(String id, String name, Kind kind, HotfCurrency currency, int tier, int col, int maxLevel,
                       double exponent, int offset, int linearBase, int linearSlope, boolean available,
                       String unavailable, String exclusive, List<String> lore, int tokenCost, List<Effect> effects) {

    public enum Kind {
        PERK,
        ABILITY
    }

    public record Effect(String key, ItemStatistic statistic, double base, double perLevel, Double cap, HotfScope scope) {
        public double value(int level) {
            if (level <= 0) return 0;
            double raw = base + perLevel * level;
            return cap == null ? raw : Math.min(cap, raw);
        }
    }

    public boolean isAbility() {
        return kind == Kind.ABILITY;
    }

    public boolean usesWhispers() {
        return currency != null;
    }

    public long costToReach(int level) {
        if (level <= 1 || currency == null) return 0;
        if (linearBase > 0) return linearBase + (long) linearSlope * level;
        return (long) Math.floor(Math.pow(level + offset, exponent));
    }

    public long totalCost(int level) {
        long total = 0;
        for (int current = 2; current <= level; current++) total += costToReach(current);
        return total;
    }

    public double value(String key, int level) {
        for (Effect effect : effects) {
            if (effect.key().equals(key)) return effect.value(level);
        }
        return 0;
    }

    public double value(int level) {
        return value("main", level);
    }
}
