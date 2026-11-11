package dev.daweapon.type.skyblockgeneric.carnival.games;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Zombie Shootout rules as captured on Hypixel: zombies come in growing waves that unlock better armour, a lit
 * Redstone Lamp is worth 100 and boosts zombie points by 1.2x for five seconds, and baby zombies are worth double.
 */
public class ZombieShootoutScore {
    public static final int SECONDS = 100;
    public static final int LAMP_POINTS = 100;
    public static final int LAMP_LIT_TICKS = 120;
    public static final int LAMP_CYCLE_TICKS = 120;
    public static final int LAMP_BOOST_TICKS = 100;
    public static final int BABY_MULTIPLIER = 2;
    public static final int ZOMBIE_LIFETIME_TICKS = 160;
    public static final int MAX_WAVE_SIZE = 9;
    public static final int FIRST_BABY_WAVE = 5;
    private static final int[] WAVE_SIZES = {1, 2, 3, 4, 4, 5, 6, 7, 7, 8};

    @Getter
    public enum ZombieTier {
        LEATHER("Leather Zombies", "<e>", 30, 1, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE),
        IRON("Iron Zombies", "<f>", 50, 3, Material.IRON_HELMET, Material.IRON_CHESTPLATE),
        GOLD("Gold Zombies", "<6>", 80, 6, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE),
        DIAMOND("Diamond Zombies", "<b>", 120, 11, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE);

        private final String displayName;
        private final String color;
        private final int points;
        private final int firstWave;
        private final Material helmet;
        private final Material chestplate;

        ZombieTier(String displayName, String color, int points, int firstWave, Material helmet, Material chestplate) {
            this.displayName = displayName;
            this.color = color;
            this.points = points;
            this.firstWave = firstWave;
            this.helmet = helmet;
            this.chestplate = chestplate;
        }
    }

    public record Spawn(ZombieTier tier, boolean baby) {}

    public static int waveSize(int wave) {
        return wave <= WAVE_SIZES.length ? WAVE_SIZES[Math.max(1, wave) - 1] : MAX_WAVE_SIZE;
    }

    /** One wave in spawn order: the baby (from wave 5) comes first, as on Hypixel. */
    public static List<Spawn> wave(int wave, Random random) {
        List<Spawn> spawns = new ArrayList<>();
        int size = waveSize(wave);
        if (wave >= FIRST_BABY_WAVE) {
            spawns.add(new Spawn(random.nextInt(100) < 35 ? ZombieTier.IRON : ZombieTier.LEATHER, true));
        }
        while (spawns.size() < size) spawns.add(new Spawn(adultTier(wave, random), false));
        return spawns;
    }

    private static ZombieTier adultTier(int wave, Random random) {
        int[] weights;
        if (wave >= ZombieTier.DIAMOND.firstWave) weights = new int[]{45, 15, 20, 20};
        else if (wave >= ZombieTier.GOLD.firstWave) weights = new int[]{57, 14, 29, 0};
        else if (wave >= ZombieTier.IRON.firstWave) weights = new int[]{60, 40, 0, 0};
        else return ZombieTier.LEATHER;
        int roll = random.nextInt(100);
        for (ZombieTier tier : ZombieTier.values()) {
            if ((roll -= weights[tier.ordinal()]) < 0) return tier;
        }
        return ZombieTier.LEATHER;
    }

    @Getter
    private long score;
    private final Map<ZombieTier, Integer> kills = new EnumMap<>(ZombieTier.class);
    private final Map<ZombieTier, Long> points = new EnumMap<>(ZombieTier.class);
    @Getter
    private int lamps;
    private long boostedUntil = -1;

    public boolean isBoosted(long tick) {
        return tick < boostedUntil;
    }

    public int onZombie(ZombieTier tier, boolean baby, long tick) {
        int earned = tier.points * (baby ? BABY_MULTIPLIER : 1);
        if (isBoosted(tick)) earned = earned * 6 / 5;
        kills.merge(tier, 1, Integer::sum);
        points.merge(tier, (long) earned, Long::sum);
        score += earned;
        return earned;
    }

    public int onLamp(long tick) {
        lamps++;
        boostedUntil = tick + LAMP_BOOST_TICKS;
        score += LAMP_POINTS;
        return LAMP_POINTS;
    }

    public int kills(ZombieTier tier) {
        return kills.getOrDefault(tier, 0);
    }

    public long points(ZombieTier tier) {
        return points.getOrDefault(tier, 0L);
    }

    public int totalKills() {
        int total = 0;
        for (ZombieTier tier : ZombieTier.values()) total += kills(tier);
        return total;
    }

    public CarnivalGameResult result() {
        Map<Metric, Integer> metrics = new EnumMap<>(Metric.class);
        metrics.put(Metric.ZOMBIES, totalKills());
        metrics.put(Metric.DIAMOND_ZOMBIES, kills(ZombieTier.DIAMOND));
        metrics.put(Metric.LAMPS, lamps);
        return new CarnivalGameResult(CarnivalGame.ZOMBIE_SHOOTOUT, score, metrics);
    }
}
