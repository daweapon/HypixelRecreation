package dev.daweapon.type.skyblockgeneric.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import dev.daweapon.LinkedField;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.CoopLinks;
import dev.daweapon.type.generic.data.Datapoint;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

// Subscribes to coop-linked profile datapoint changes and applies incoming updates to every
// online player currently sharing that coop, keeping their in-memory datapoints in sync.
public final class CoopSync {

    private CoopSync() {}

    public static void subscribe() {
        for (SkyBlockDataHandler.Data data : SkyBlockDataHandler.Data.values()) {
            if (data.coopField() == null) continue;
            SharedData.profile().subscribe(data.coopField(),
                    (coopId, oldValue, newValue, affected) -> apply(data, coopId, newValue));
        }
    }

    public static <T> void track(LinkedField<UUID, T> field) {
        SharedData.profile().subscribe(field, (coopId, oldValue, newValue, affected) -> {});
    }

    private static void apply(SkyBlockDataHandler.Data data, UUID coopId, String newValue) {
        if (newValue == null) return;
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            SkyBlockDataHandler handler = player.getSkyblockDataHandler();
            if (handler == null) continue;

            UUID profileId = handler.getCurrentProfileId();
            UUID playerCoopId = SharedData.profile().getLinkKey(profileId, CoopLinks.COOP).orElse(profileId);
            if (!playerCoopId.equals(coopId)) continue;

            Datapoint<?> datapoint = handler.getDatapoint(data.getKey());
            if (datapoint == null) continue;
            datapoint.deserializeValue(newValue);

            Player online = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(player.getUuid());
            if (online != null && data.onChange != null) {
                data.onChange.accept(online, datapoint);
            }
        }
    }
}
