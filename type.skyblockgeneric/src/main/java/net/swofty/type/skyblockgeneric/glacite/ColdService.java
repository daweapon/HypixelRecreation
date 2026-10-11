package net.swofty.type.skyblockgeneric.glacite;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.title.Title;

public final class ColdService {
    public static final Pos BASE_CAMP_SPAWN = new Pos(0.5, 128, 200.5, 0, 0);

    private static final Map<UUID, Double> COLD = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> PROGRESS = new ConcurrentHashMap<>();
    private static boolean started;

    private ColdService() {}

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(ColdService::tick).repeat(TaskSchedule.seconds(1)).schedule();
    }

    public static double cold(SkyBlockPlayer player) {
        return COLD.getOrDefault(player.getUuid(), 0.0);
    }

    public static void addCold(SkyBlockPlayer player, double amount) {
        COLD.put(player.getUuid(), ColdMath.clamp(cold(player) + amount));
    }

    public static void reset(SkyBlockPlayer player) {
        COLD.remove(player.getUuid());
        PROGRESS.remove(player.getUuid());
    }

    public static double miningSpeedMultiplier(SkyBlockPlayer player) {
        double cold = cold(player);
        if (cold >= 0) return 1;
        return ColdMath.miningSpeedMultiplier(cold, resistance(player));
    }

    private static double resistance(SkyBlockPlayer player) {
        return player.getStatistics().allStatistics().getOverall(ItemStatistic.COLD_RESISTANCE);
    }

    private static void tick() {
        var players = SkyBlockGenericLoader.getLoadedPlayers();
        COLD.keySet().removeIf(id -> players.stream().noneMatch(p -> p.getUuid().equals(id)));
        PROGRESS.keySet().removeIf(id -> players.stream().noneMatch(p -> p.getUuid().equals(id)));
        for (SkyBlockPlayer player : players) {
            try {
                tickPlayer(player);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static void tickPlayer(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        RegionType type = region == null ? null : region.getType();
        if (GlaciteZones.isBaseCamp(type)) {
            reset(player);
            return;
        }
        double before = cold(player);
        if (GlaciteZones.isColdZone(type)) {
            double vulnerability = GlaciteZones.isWater(type) && inWater(player) ? ColdMath.WATER_VULNERABILITY : 0;
            double step = 1 / ColdMath.secondsPerPoint(resistance(player), vulnerability);
            double progress = PROGRESS.merge(player.getUuid(), step, Double::sum);
            int whole = (int) Math.floor(progress);
            if (whole > 0) {
                PROGRESS.put(player.getUuid(), progress - whole);
                COLD.put(player.getUuid(), ColdMath.clamp(before - whole));
            }
        } else if (before < 0) {
            COLD.put(player.getUuid(), ColdMath.clamp(before + 1));
        }
        double after = cold(player);
        announce(player, before, after);
        if (after < 0) {
            SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.COLD,
                    Text.of("<b>❄ {} Cold", (int) Math.floor(after)), 30, 1);
        }
        if (ColdMath.freezesToDeath(after)) freeze(player);
    }

    private static boolean inWater(SkyBlockPlayer player) {
        var instance = player.getInstance();
        return instance != null && instance.getBlock(player.getPosition()).compare(Block.WATER);
    }

    private static void announce(SkyBlockPlayer player, double before, double after) {
        for (ColdMath.Threshold threshold : ColdMath.crossed(before, after)) {
            player.sendMessage(threshold.message());
            if (threshold.cold() == -90) {
                player.showTitle(Text.of("<c><l>FROSTBITE!"), Text.of("<c>Find warmth before you freeze to death..."),
                        Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(500)));
            }
        }
    }

    private static void freeze(SkyBlockPlayer player) {
        reset(player);
        player.sendMessage("<c>☠ <7>You froze to death.");
        player.teleport(BASE_CAMP_SPAWN);
    }
}
