package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

public final class ObtainCounter {
    private static final String KEY_PREFIX = "hsb:greatspook:obtained:";

    private ObtainCounter() {
    }

    public static void announce(SkyBlockPlayer player, String experimentId) {
        if (SharedData.jedisPool() == null) return;
        Thread.startVirtualThread(() -> {
            long position;
            try (Jedis jedis = SharedData.jedisPool().getResource()) {
                position = jedis.incr(KEY_PREFIX + experimentId);
            } catch (Exception exception) {
                Logger.warn(exception, "Could not count the Great Spook obtain for {}", experimentId);
                return;
            }
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                if (!player.isOnline()) return;
                player.sendMessage("<a>You are the <e>#" + StringUtility.commaify(position) + " <a>player to obtain this item!");
            });
        });
    }
}
