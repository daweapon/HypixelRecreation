package dev.daweapon.type.skyblockgeneric.levels;

/**
 * Hypixel's "+1 XP per {@code perXp} until {@code linearCap}, then an exponentially decreasing amount until
 * {@code fullAt}" SkyBlock XP rule (wiki: SkyBlock Levels/Tasks, powder and whispers). The wiki gives no formula for
 * the tail; it is modelled as an exponential approach, scaled so the task is exactly full at {@code fullAt}.
 */
public record DiminishingXp(long perXp, long linearCap, long fullAt, double maxXp) {

    public double experience(long amount) {
        if (amount <= 0) return 0;
        if (amount <= linearCap) return (double) amount / perXp;
        if (amount >= fullAt) return maxXp;
        double linear = (double) linearCap / perXp;
        double scale = (maxXp - linear) * perXp;
        double reach = 1 - Math.exp(-(fullAt - linearCap) / scale);
        return linear + (maxXp - linear) * (1 - Math.exp(-(amount - linearCap) / scale)) / reach;
    }
}
