package net.swofty.type.skyblockgeneric.crimson.volcano;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Blazing Volcano's eruption cycle and its rotating geyser. It only runs on the Crimson Isle
 * server; anywhere else {@link #isErupting()} stays false.
 */
public final class VolcanoEruptionService {
    public static final Pos CRATER = new Pos(-370, 118, -775);
    public static final List<Pos> GEYSER_POINTS = List.of(
            new Pos(-397, 118, -772), new Pos(-390, 118, -754), new Pos(-386, 118, -795),
            new Pos(-377, 118, -767), new Pos(-365, 118, -761), new Pos(-360, 118, -782),
            new Pos(-357, 118, -797), new Pos(-344, 118, -786), new Pos(-349, 118, -766));

    private static final Duration MIN_INTERVAL = Duration.ofMinutes(120);
    private static final Duration MAX_INTERVAL = Duration.ofMinutes(150);
    private static final Duration ERUPTION_LENGTH = Duration.ofMinutes(3);
    private static final Duration GEYSER_ROTATION = Duration.ofMinutes(10);

    private static boolean running;
    private static long nextEruptionAt;
    private static long eruptionEndsAt;
    private static long lastEruptionEndedAt = -1;
    private static @Nullable VolcanoExplosivity current;
    private static VolcanoExplosivity lastExplosivity = VolcanoExplosivity.CHILL;
    private static int geyserIndex;
    private static long geyserMovesAt;

    private VolcanoEruptionService() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        long now = System.currentTimeMillis();
        scheduleNext(now);
        geyserIndex = ThreadLocalRandom.current().nextInt(GEYSER_POINTS.size());
        geyserMovesAt = now + GEYSER_ROTATION.toMillis();
        MinecraftServer.getSchedulerManager().buildTask(VolcanoEruptionService::tick)
                .repeat(TaskSchedule.seconds(1))
                .schedule();
    }

    public static boolean isErupting() {
        return current != null;
    }

    public static @Nullable VolcanoExplosivity currentExplosivity() {
        return current;
    }

    public static VolcanoExplosivity lastExplosivity() {
        return lastExplosivity;
    }

    /** Milliseconds since the last eruption ended, or {@link Long#MAX_VALUE} if none has happened yet. */
    public static long millisSinceLastEruption() {
        return lastEruptionEndedAt < 0 ? Long.MAX_VALUE : System.currentTimeMillis() - lastEruptionEndedAt;
    }

    public static long millisUntilNextEruption() {
        return Math.max(0, nextEruptionAt - System.currentTimeMillis());
    }

    public static Pos activeGeyser() {
        return GEYSER_POINTS.get(geyserIndex);
    }

    public static boolean isNearGeyser(Point point, double radius) {
        Pos geyser = activeGeyser();
        double dx = point.x() - geyser.x();
        double dz = point.z() - geyser.z();
        return dx * dx + dz * dz <= radius * radius && Math.abs(point.y() - geyser.y()) <= radius + 2;
    }

    /** Staff testing: starts an eruption now, of the given or a freshly rolled explosivity. */
    public static synchronized void forceEruption(@Nullable VolcanoExplosivity explosivity) {
        if (isErupting()) finishEruption(System.currentTimeMillis());
        beginEruption(System.currentTimeMillis(), explosivity == null ? VolcanoExplosivity.roll() : explosivity);
    }

    private static synchronized void tick() {
        long now = System.currentTimeMillis();
        if (current == null && now >= nextEruptionAt) beginEruption(now, VolcanoExplosivity.roll());
        if (current != null && now >= eruptionEndsAt) finishEruption(now);
        if (now >= geyserMovesAt) {
            geyserIndex = (geyserIndex + 1 + ThreadLocalRandom.current().nextInt(GEYSER_POINTS.size() - 1))
                    % GEYSER_POINTS.size();
            geyserMovesAt = now + GEYSER_ROTATION.toMillis();
        }
        showEffects();
    }

    private static void beginEruption(long now, VolcanoExplosivity explosivity) {
        current = explosivity;
        lastExplosivity = explosivity;
        eruptionEndsAt = now + ERUPTION_LENGTH.toMillis();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage("<6><l>VOLCANO! <c>The <6>Blazing Volcano <c>is erupting! <7>Explosivity: " + explosivity.coloured());
            player.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.AMBIENT, 1f, 0.6f));
        }
        HypixelEventHandler.callCustomEvent(new VolcanoEruptionEvent(explosivity, true));
    }

    private static void finishEruption(long now) {
        VolcanoExplosivity ended = current;
        current = null;
        lastEruptionEndedAt = now;
        scheduleNext(now);
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage("<e>The <6>Blazing Volcano <e>has calmed down.");
        }
        HypixelEventHandler.callCustomEvent(new VolcanoEruptionEvent(ended, false));
    }

    private static void scheduleNext(long now) {
        nextEruptionAt = now + ThreadLocalRandom.current().nextLong(MIN_INTERVAL.toMillis(), MAX_INTERVAL.toMillis() + 1);
    }

    private static void showEffects() {
        Pos geyser = activeGeyser();
        ParticlePacket steam = new ParticlePacket(Particle.CLOUD, true, true,
                geyser.x() + 0.5, geyser.y() + 1.5, geyser.z() + 0.5, 0.3f, 1.5f, 0.3f, 0.02f, 12);
        ParticlePacket lava = current == null ? null : new ParticlePacket(Particle.LAVA, true, true,
                CRATER.x(), CRATER.y() + 4, CRATER.z(), 12f, 4f, 12f, 0.5f, 60);
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getPosition().distanceSquared(geyser) < 96 * 96) player.sendPacket(steam);
            if (lava != null && player.getPosition().distanceSquared(CRATER) < 160 * 160) player.sendPacket(lava);
        }
    }
}
