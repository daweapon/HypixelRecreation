package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Weather;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryConditionalStatistic;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The Spider's Den weather (wiki: Spider's_Den): every 40 minutes a 20 minute rain starts, and every third
 * rain is a thunderstorm. It runs off the wall clock so every lobby agrees. Rain gives +25 Fishing Speed.
 * The island also applies Mining Fatigue to every player.
 */
public final class SpiderDenWeather {
    public static final long PERIOD_MILLIS = 40 * 60_000L;
    public static final long RAIN_MILLIS = 20 * 60_000L;
    public static final int RAINS_PER_THUNDERSTORM = 3;
    public static final double RAIN_FISHING_SPEED = 25;
    public static final int FATIGUE_AMPLIFIER = 2;
    private static final int FATIGUE_TICKS = 20 * 60 * 10;
    private static final int TICK_SECONDS = 5;
    private static final Set<SkyBlockPlayer> REGISTERED = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static boolean running;
    private static Weather applied;

    private SpiderDenWeather() {
    }

    public static boolean raining(long now) {
        return now % PERIOD_MILLIS < RAIN_MILLIS;
    }

    public static boolean thundering(long now) {
        return raining(now) && (now / PERIOD_MILLIS) % RAINS_PER_THUNDERSTORM == RAINS_PER_THUNDERSTORM - 1;
    }

    public static Weather forecast(long now) {
        if (thundering(now)) return Weather.THUNDER;
        return raining(now) ? Weather.RAIN : Weather.CLEAR;
    }

    public static boolean rainingHere() {
        return running && raining(System.currentTimeMillis());
    }

    public static boolean thunderingHere() {
        return running && thundering(System.currentTimeMillis());
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().buildTask(SpiderDenWeather::tick)
                .repeat(TaskSchedule.seconds(TICK_SECONDS)).schedule();
    }

    public static void ensureStatistics(SkyBlockPlayer player) {
        if (!REGISTERED.add(player)) return;
        player.getStatistics().boostStatistic(TemporaryConditionalStatistic.builder()
                .withStatistics(owner -> rainingHere()
                        ? ItemStatistics.builder().withBase(ItemStatistic.FISHING_SPEED, RAIN_FISHING_SPEED).build()
                        : ItemStatistics.empty())
                .withExpiry(owner -> true)
                .build());
    }

    private static void tick() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null) return;
        Weather wanted = forecast(System.currentTimeMillis());
        if (applied == null || applied.rainLevel() != wanted.rainLevel() || applied.thunderLevel() != wanted.thunderLevel()) {
            instance.setWeather(wanted);
            applied = wanted;
        }
        for (Player player : instance.getPlayers()) {
            if (!player.hasEffect(PotionEffect.MINING_FATIGUE)) {
                player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, (byte) FATIGUE_AMPLIFIER, FATIGUE_TICKS));
            }
        }
    }
}
