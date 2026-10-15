package dev.daweapon.type.skyblockgeneric.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.data.Datapoint;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data;
import org.tinylog.Logger;

final class SkyBlockRepeatSetValueLoop {

    private SkyBlockRepeatSetValueLoop() {
    }

    static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                SkyBlockDataHandler h = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                if (h == null) return;
                for (Data data : Data.values()) {
                    if (!data.getRepeatSetValue()) continue;
                    try {
                        Datapoint<?> dp = h.get(data);
                        @SuppressWarnings("unchecked")
                        Datapoint<Object> any = (Datapoint<Object>) dp;
                        any.setValue(any.getValue());
                    } catch (Exception e) {
                        Logger.error(e, "Failed to repeat set SkyBlock datapoint {} for user {}",
                                data.getKey(), player.getUuid());
                    }
                }
            });
            return TaskSchedule.nextTick();
        });
    }
}
