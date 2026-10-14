package net.swofty.type.skyblockgeneric.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data;
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
