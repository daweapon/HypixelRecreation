package dev.daweapon.type.skyblockgeneric.abiphone.ninef;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NineFRelays {
    public static final double HEARING_RANGE = 64;
    private static final int TICK_INTERVAL = 4;
    private static final int FASTEST_PULSE_TICKS = 4;
    private static final int SLOWEST_PULSE_TICKS = 40;
    private static final List<EntityNineFRelay> SPAWNED = new ArrayList<>();
    private static final Map<UUID, Integer> NEXT_PULSE = new ConcurrentHashMap<>();

    private NineFRelays() {
    }

    public static void start(Instance instance, ServerType server) {
        for (NineFRelay relay : NineFRelay.values()) {
            if (relay.server() != server) continue;
            EntityNineFRelay entity = new EntityNineFRelay(relay);
            entity.spawn(instance);
            SPAWNED.add(entity);
        }
        if (SPAWNED.isEmpty()) return;
        MinecraftServer.getSchedulerManager().buildTask(NineFRelays::tick)
                .repeat(TaskSchedule.tick(TICK_INTERVAL)).schedule();
    }

    private static void tick() {
        for (EntityNineFRelay entity : SPAWNED) {
            if (entity.getInstance() == null) continue;
            for (Player online : entity.getInstance().getPlayers()) {
                if (!(online instanceof SkyBlockPlayer player) || player.getSkyblockDataHandler() == null) continue;
                boolean visible = NineFNetwork.canSee(player, entity.relay());
                if (visible && !entity.isViewer(player)) entity.addViewer(player);
                else if (!visible && entity.isViewer(player)) entity.removeViewer(player);
                if (visible) pulse(player, player.getPosition().distance(entity.relay().position()));
            }
        }
    }

    private static void pulse(SkyBlockPlayer player, double distance) {
        if (distance > HEARING_RANGE) return;
        int remaining = NEXT_PULSE.merge(player.getUuid(), -TICK_INTERVAL, Integer::sum);
        if (remaining > 0) return;
        double closeness = 1 - distance / HEARING_RANGE;
        NEXT_PULSE.put(player.getUuid(), pulseTicks(distance));
        player.playSound(Sound.sound(Key.key("block.note_block.chime"), Sound.Source.MASTER, 1f, (float) (0.5 + 1.5 * closeness)));
    }

    static int pulseTicks(double distance) {
        int scaled = (int) Math.round(SLOWEST_PULSE_TICKS * Math.min(1, distance / HEARING_RANGE));
        return Math.max(FASTEST_PULSE_TICKS, scaled);
    }
}
