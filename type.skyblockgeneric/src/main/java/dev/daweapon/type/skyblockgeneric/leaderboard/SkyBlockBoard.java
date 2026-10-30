package dev.daweapon.type.skyblockgeneric.leaderboard;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.function.ToDoubleFunction;

public record SkyBlockBoard(String id, String name, BoardCategory category, Material icon, String unit,
                            ToDoubleFunction<SkyBlockPlayer> score, Timeframe timeframe) {
    public static final String KEY_PREFIX = "skyblock:board:";

    public SkyBlockBoard(String id, String name, BoardCategory category, Material icon, String unit,
                         ToDoubleFunction<SkyBlockPlayer> score) {
        this(id, name, category, icon, unit, score, null);
    }

    public String key() {
        return timeframe == null ? KEY_PREFIX + id : windowKey(timeframe.currentWindowId());
    }

    public String windowKey(String windowId) {
        return KEY_PREFIX + timeframe.getPrefix() + ":" + windowId + ":" + id.substring(timeframe.getPrefix().length() + 1);
    }
}
