package dev.daweapon.type.skyblockgeneric.leaderboard;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.function.ToDoubleFunction;

public record SkyBlockBoard(String id, String name, BoardCategory category, Material icon, String unit,
                            ToDoubleFunction<SkyBlockPlayer> score) {
    public static final String KEY_PREFIX = "skyblock:board:";

    public String key() {
        return KEY_PREFIX + id;
    }
}
