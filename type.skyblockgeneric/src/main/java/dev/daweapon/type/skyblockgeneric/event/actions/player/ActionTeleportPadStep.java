package dev.daweapon.type.skyblockgeneric.event.actions.player;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleportPad;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleportPads;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Teleports a player who steps onto a Teleport Pad to the pad's destination
public class ActionTeleportPadStep implements HypixelEventClass {
    private static final Map<UUID, String> STANDING_ON = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        if (!HypixelConst.isIslandServer()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockIsland island = SkyBlockIsland.forInstance(player.getInstance());
        if (island == null) return;

        Pos position = event.getNewPosition();
        TeleportPad pad = TeleportPads.of(island).at(position.blockX(), (int) Math.floor(position.y()), position.blockZ());
        if (pad == null) {
            STANDING_ON.remove(player.getUuid());
            return;
        }
        if (pad.key().equals(STANDING_ON.get(player.getUuid()))) return;

        STANDING_ON.put(player.getUuid(), pad.key());
        if (pad.destination() != null) STANDING_ON.put(player.getUuid(), pad.destination());
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            if (player.isOnline() && player.getInstance() == island.getIslandInstance()) TeleportPads.teleport(player, island, pad);
        });
    }
}
