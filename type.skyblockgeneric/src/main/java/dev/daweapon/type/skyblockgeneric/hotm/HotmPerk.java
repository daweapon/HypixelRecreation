package dev.daweapon.type.skyblockgeneric.hotm;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;

import java.util.List;

public record HotmPerk(String id, String name, Kind kind, HotmPowder powder, int tier, int col, int maxLevel, double exponent,
                       int offset, boolean available, String requires, List<String> lore, int tokenCost, List<Effect> effects) {

    public enum Kind {
        PERK,
        ABILITY
    }

    public boolean isAbility() {
        return kind == Kind.ABILITY;
    }


    public record Effect(String key, ItemStatistic statistic, double base, double perLevel, Double cap, HotmScope scope) {
        public double value(int level) {
            if (level <= 0) return 0;
            double raw = base + perLevel * level;
            return cap == null ? raw : Math.min(cap, raw);
        }
    }

    public boolean usesPowder() {
        return powder != null;
    }

    public long costToReach(int level) {
        if (level <= 1 || powder == null) return 0;
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
