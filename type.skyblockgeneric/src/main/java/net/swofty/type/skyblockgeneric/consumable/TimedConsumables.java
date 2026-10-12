package net.swofty.type.skyblockgeneric.consumable;

import net.swofty.type.generic.data.datapoints.DatapointMapStringLong;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

public final class TimedConsumables {
    private TimedConsumables() {
    }

    public static boolean isActive(Map<String, Long> expirations, String id, long now) {
        Long until = expirations.get(id);
        return until != null && until > now;
    }

    public static boolean isActive(SkyBlockPlayer player, String id) {
        return isActive(expirations(player), id, System.currentTimeMillis());
    }

    public static long expiration(SkyBlockPlayer player, String id) {
        return expirations(player).getOrDefault(id, 0L);
    }

    public static boolean start(SkyBlockPlayer player, String id, long durationMillis) {
        if (isActive(player, id)) return false;
        Map<String, Long> updated = new HashMap<>(expirations(player));
        updated.put(id, System.currentTimeMillis() + durationMillis);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TIMED_CONSUMABLES, DatapointMapStringLong.class).setValue(updated);
        return true;
    }

    public static void consumeHeld(SkyBlockPlayer player, SkyBlockItem item) {
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
    }

    static Map<String, Long> expirations(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return Map.of();
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TIMED_CONSUMABLES, DatapointMapStringLong.class).getValue();
    }
}
