package net.swofty.type.crystalhollows.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.swofty.type.crystalhollows.gate.EntryGate;
import net.swofty.type.crystalhollows.temple.JungleTemple;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionStructureMove implements HypixelEventClass {
    private static final Map<UUID, RegionType> PREVIOUS = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        var from = player.getPosition();
        var to = event.getNewPosition();
        if (from.blockX() == to.blockX() && from.blockY() == to.blockY() && from.blockZ() == to.blockZ()) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(to);
        RegionType current = region == null ? null : region.getType();
        RegionType previous = PREVIOUS.put(player.getUuid(), current == null ? RegionType.PRIVATE_ISLAND : current);
        for (EntryGate gate : EntryGate.GATES) {
            if (!gate.apply(player, previous, current)) {
                PREVIOUS.put(player.getUuid(), previous == null ? RegionType.PRIVATE_ISLAND : previous);
                return;
            }
        }
        if (current == RegionType.JUNGLE_TEMPLE) JungleTemple.onMove(player, to);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        PREVIOUS.remove(event.getPlayer().getUuid());
    }
}
