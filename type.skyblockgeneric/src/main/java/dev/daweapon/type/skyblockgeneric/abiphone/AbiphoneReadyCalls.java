package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AbiphoneReadyCalls {
    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static final Set<String> CALLED = ConcurrentHashMap.newKeySet();

    private AbiphoneReadyCalls() {
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().buildTask(AbiphoneReadyCalls::tick)
                .delay(TaskSchedule.seconds(30)).repeat(TaskSchedule.seconds(20)).schedule();
    }

    private static void tick() {
        for (Player online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (!(online instanceof SkyBlockPlayer player) || player.getSkyblockDataHandler() == null) continue;
            for (AbiphoneNPC npc : AbiphoneRegistry.getRegisteredContactNPCs()) check(player, npc);
        }
    }

    private static void check(SkyBlockPlayer player, AbiphoneNPC npc) {
        String key = player.getUuid() + ":" + npc.getId();
        if (!npc.wantsToCall(player)) {
            CALLED.remove(key);
            return;
        }
        if (CALLED.contains(key)) return;
        AbiphoneIncomingCalls.Result result = AbiphoneIncomingCalls.ring(player, npc.getId(), npc::onIncomingPickUp);
        if (result == AbiphoneIncomingCalls.Result.RINGING || result == AbiphoneIncomingCalls.Result.SILENCED) CALLED.add(key);
    }

    public static void forget(UUID player) {
        CALLED.removeIf(key -> key.startsWith(player.toString()));
    }
}
