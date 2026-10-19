package dev.daweapon.type.skyblockgeneric.hoppity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.abiphone.impl.AbiphoneHoppity;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HoppityCalls {
    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static long lastDay = -1;

    private HoppityCalls() {
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().buildTask(HoppityCalls::tick).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void tick() {
        long day = SkyBlockCalendar.getElapsed() / SkyBlockCalendar.DAY;
        if (day == lastDay) return;
        boolean first = lastDay < 0;
        lastDay = day;
        if (first || !(AbiphoneRegistry.getFromId(AbiphoneHoppity.ID) instanceof AbiphoneHoppity hoppity)) return;
        for (Player online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (online instanceof SkyBlockPlayer player
                    && LootRoll.roll(player.getUuid(), null, HoppityAbiphoneOffer.CALL_CHANCE_PERCENT, ThreadLocalRandom.current())) {
                hoppity.incomingCall(player);
            }
        }
    }
}
