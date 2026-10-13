package net.swofty.type.skyblockgeneric.crystalhollows;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HeatService {
    public static final double HEAT_Y = 64;
    public static final double DEATH_HEAT = 100;
    public static final double COOL_PER_SECOND = 5;
    private static final Set<RegionType> HOT_ZONES = Set.of(RegionType.MAGMA_FIELDS, RegionType.KHAZAD_DUM);
    private static final List<Warning> WARNINGS = List.of(
            new Warning(90, "<c>You are heating up!"),
            new Warning(93, "<c>You are starting to burn!"),
            new Warning(96, "<c>You are melting!"),
            new Warning(99, "<4>Time to leave!"));

    private static final Map<UUID, Double> HEAT = new ConcurrentHashMap<>();
    private static boolean started;

    private HeatService() {
    }

    private record Warning(double heat, String message) {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(HeatService::tick).repeat(TaskSchedule.seconds(1)).schedule();
    }

    public static double heat(SkyBlockPlayer player) {
        return HEAT.getOrDefault(player.getUuid(), 0.0);
    }

    public static void set(SkyBlockPlayer player, double value) {
        double clamped = Math.max(0, Math.min(DEATH_HEAT, value));
        if (clamped <= 0) HEAT.remove(player.getUuid());
        else HEAT.put(player.getUuid(), clamped);
    }

    public static void reset(SkyBlockPlayer player) {
        HEAT.remove(player.getUuid());
    }

    public static void reduce(SkyBlockPlayer player, double amount) {
        if (amount <= 0) return;
        HEAT.computeIfPresent(player.getUuid(), (_, heat) -> heat - amount > 0 ? heat - amount : null);
    }

    public static double resistance(SkyBlockPlayer player) {
        return Math.max(0, player.getStatistics().allStatistics().getOverall(ItemStatistic.HEAT_RESISTANCE));
    }

    public static double heatPerSecond(double y, double resistance) {
        double depth = 1 + Math.max(0, HEAT_Y - y) / HEAT_Y;
        return depth * 100 / (100 + resistance);
    }

    public static boolean isHot(RegionType region, double y) {
        return region != null && HOT_ZONES.contains(region) && y <= HEAT_Y;
    }

    private static void tick() {
        var players = SkyBlockGenericLoader.getLoadedPlayers();
        HEAT.keySet().removeIf(id -> players.stream().noneMatch(player -> player.getUuid().equals(id)));
        for (SkyBlockPlayer player : players) {
            try {
                tickPlayer(player);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static void tickPlayer(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        double before = heat(player);
        double after;
        if (!player.isDead() && isHot(region == null ? null : region.getType(), player.getPosition().y())) {
            after = before + heatPerSecond(player.getPosition().y(), resistance(player));
        } else {
            after = Math.max(0, before - COOL_PER_SECOND);
        }
        if (after <= 0) {
            HEAT.remove(player.getUuid());
            return;
        }
        HEAT.put(player.getUuid(), after);
        for (Warning warning : WARNINGS) {
            if (before < warning.heat() && after >= warning.heat()) player.sendMessage(Text.of(warning.message()));
        }
        if (after >= DEATH_HEAT) {
            reset(player);
            player.sendMessage(Text.of("<c>You burned to death."));
            player.kill();
            return;
        }
        SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.HEAT,
                Text.of("<c>♨ {} Heat", (int) Math.floor(after)), 30, 1);
    }
}
