package dev.daweapon.type.skyblockgeneric.island.pad;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.coordinate.Pos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportPads {
    public static final String STORAGE_KEY = "teleport_pads";
    private static final double STAND_HEIGHT = 0.8125;
    private static final Map<UUID, TeleportPadSet> LOADED = new ConcurrentHashMap<>();

    private TeleportPads() {
    }

    public static TeleportPadSet of(SkyBlockIsland island) {
        return LOADED.computeIfAbsent(island.getIslandID(),
                _ -> TeleportPadSet.parse(String.valueOf(island.getDatabase().get(STORAGE_KEY, ""))));
    }

    public static void save(SkyBlockIsland island) {
        island.getDatabase().insertOrUpdate(STORAGE_KEY, of(island).serialize());
    }

    public static boolean isMember(SkyBlockPlayer player, SkyBlockIsland island) {
        return island == player.getSkyBlockIsland();
    }

    public static void teleport(SkyBlockPlayer player, SkyBlockIsland island, TeleportPad from) {
        TeleportPad target = of(island).byKey(from.destination());
        if (target == null) {
            player.sendMessage("<c>This Teleport Pad has no destination set!");
            return;
        }
        Pos current = player.getPosition();
        Pos arrival = new Pos(target.x() + 0.5, target.y() + STAND_HEIGHT, target.z() + 0.5,
                from.direction().yawOr(current.yaw()), current.pitch());
        player.teleport(arrival);
        player.playTeleportSound();
    }
}
