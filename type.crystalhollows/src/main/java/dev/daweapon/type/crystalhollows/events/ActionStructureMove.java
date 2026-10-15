package dev.daweapon.type.crystalhollows.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.crystalhollows.gate.EntryGate;
import dev.daweapon.type.crystalhollows.temple.JungleTemple;
import dev.daweapon.type.crystalhollows.temple.TempleDoor;
import dev.daweapon.type.skyblockgeneric.region.rules.TempleLockdown;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
        if (current == RegionType.JUNGLE_TEMPLE) {
            if (previous != RegionType.JUNGLE_TEMPLE) {
                TempleLockdown.enter(player);
                player.sendMessage(Text.of("<c>You are entering the jungle temple, your speed is reduced and your jump boost will not work!"));
            }
            JungleTemple.onMove(player, to);
        } else if (previous == RegionType.JUNGLE_TEMPLE) {
            TempleLockdown.leave(player);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        PREVIOUS.remove(event.getPlayer().getUuid());
        TempleLockdown.forget(event.getPlayer().getUuid());
        TempleDoor.released(event.getPlayer().getUuid());
    }
}
