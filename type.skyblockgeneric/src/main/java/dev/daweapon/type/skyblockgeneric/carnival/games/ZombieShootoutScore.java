package dev.daweapon.type.skyblockgeneric.carnival.games;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/** Zombie Shootout scoring: zombie and lamp points, baby doubling and the lamp's 1.2x boost. */
public class ZombieShootoutScore {
    public static final int SECONDS = 100;
    public static final int LAMP_POINTS = 100;
    public static final int LAMP_LIT_TICKS = 6 * 20;
    public static final int LAMP_BOOST_TICKS = 6 * 20;
    public static final double LAMP_BOOST = 1.2;
    public static final double BABY_CHANCE = 0.10;
    public static final int BABY_MULTIPLIER = 2;

    /** {@code weight} is an estimate; the wiki gives points and lifetimes but not spawn rates. */
    @Getter
    public enum ZombieTier {
        LEATHER("Leather Zombies", "<e>", 30, 7, 40, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE),
        IRON("Iron Zombies", "<f>", 50, 6, 30, Material.IRON_HELMET, Material.IRON_CHESTPLATE),
        GOLD("Gold Zombies", "<6>", 80, 5, 20, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE),
        DIAMOND("Diamond Zombies", "<b>", 120, 4, 10, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE);

        private final String displayName;
        private final String color;
        private final int points;
        private final int lifetimeSeconds;
        private final int weight;
        private final Material helmet;
        private final Material chestplate;

        ZombieTier(String displayName, String color, int points, int lifetimeSeconds, int weight,
                   Material helmet, Material chestplate) {
            this.displayName = displayName;
            this.color = color;
            this.points = points;
            this.lifetimeSeconds = lifetimeSeconds;
            this.weight = weight;
            this.helmet = helmet;
            this.chestplate = chestplate;
        }

        public static ZombieTier roll(Random random) {
            int roll = random.nextInt(100);
            for (ZombieTier tier : values()) {
                if ((roll -= tier.weight) < 0) return tier;
            }
            return LEATHER;
        }
    }

    @Getter
    private double score;
    private final Map<ZombieTier, Integer> kills = new EnumMap<>(ZombieTier.class);
    private final Map<ZombieTier, Integer> babyKills = new EnumMap<>(ZombieTier.class);
    private final Map<ZombieTier, Double> points = new EnumMap<>(ZombieTier.class);
    @Getter
    private int lamps;
    @Getter
    private int shots;
    @Getter
    private int hits;
    private long boostedUntil = -1;

    public void onShot() {
        shots++;
    }

    public boolean isBoosted(long tick) {
        return tick < boostedUntil;
    }

    public double onZombie(ZombieTier tier, boolean baby, long tick) {
        hits++;
        double earned = tier.points * (baby ? BABY_MULTIPLIER : 1) * (isBoosted(tick) ? LAMP_BOOST : 1);
        (baby ? babyKills : kills).merge(tier, 1, Integer::sum);
        points.merge(tier, earned, Double::sum);
        score += earned;
        return earned;
    }

    public int onLamp(long tick) {
        hits++;
        lamps++;
        boostedUntil = tick + LAMP_BOOST_TICKS;
        score += LAMP_POINTS;
        return LAMP_POINTS;
    }

    public int kills(ZombieTier tier) {
        return kills.getOrDefault(tier, 0) + babyKills.getOrDefault(tier, 0);
    }

    public int babyKills(ZombieTier tier) {
        return babyKills.getOrDefault(tier, 0);
    }

    public long points(ZombieTier tier) {
        return Math.round(points.getOrDefault(tier, 0D));
    }

    public int totalKills() {
        int total = 0;
        for (ZombieTier tier : ZombieTier.values()) total += kills(tier);
        return total;
    }

    public int accuracyPercent() {
        return shots == 0 ? 0 : Math.round(hits * 100F / shots);
    }

    public CarnivalGameResult result() {
        Map<Metric, Integer> metrics = new EnumMap<>(Metric.class);
        metrics.put(Metric.ZOMBIES, totalKills());
        metrics.put(Metric.DIAMOND_ZOMBIES, kills(ZombieTier.DIAMOND));
        metrics.put(Metric.LAMPS, lamps);
        return new CarnivalGameResult(CarnivalGame.ZOMBIE_SHOOTOUT, Math.round(score), metrics);
    }
}
