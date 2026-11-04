package dev.daweapon.type.theend.dragon;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hypixel plays its own "Mythic Warfare" track during a dragon fight, which needs resource-pack audio;
 * until the pack carries it, every player in The End hears the vanilla dragon fight music, looped.
 */
public final class DragonMusic {
    public static final Key TRACK = Key.key("minecraft", "music.dragon");
    public static final long LOOP_MILLIS = 300_000;
    private static final int CHECK_TICKS = 20;
    private static final Sound SOUND = Sound.sound(TRACK, Sound.Source.MUSIC, 1f, 1f);
    private static final SoundStop STOP = SoundStop.named(TRACK);

    private static final Map<UUID, Long> startedAt = new HashMap<>();
    private static Task task;

    private DragonMusic() {
    }

    public static void start(Instance instance) {
        stop(instance);
        task = MinecraftServer.getSchedulerManager().buildTask(() -> tick(instance))
                .repeat(TaskSchedule.tick(CHECK_TICKS)).schedule();
        tick(instance);
    }

    public static void stop(Instance instance) {
        if (task != null) task.cancel();
        task = null;
        if (instance != null) {
            for (Player player : instance.getPlayers()) {
                if (startedAt.containsKey(player.getUuid())) player.stopSound(STOP);
            }
        }
        startedAt.clear();
    }

    private static void tick(Instance instance) {
        long now = System.currentTimeMillis();
        Set<UUID> present = new HashSet<>();
        for (Player player : instance.getPlayers()) {
            present.add(player.getUuid());
            Long started = startedAt.get(player.getUuid());
            if (started != null && now - started < LOOP_MILLIS) continue;
            player.stopSound(STOP);
            player.playSound(SOUND);
            startedAt.put(player.getUuid(), now);
        }
        startedAt.keySet().retainAll(present);
    }
}
