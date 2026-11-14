package dev.daweapon.type.skywarsconfigurator.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class ActionPlayerDataSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, isAsync = true, phase = EventPhase.POST_SPAWN)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;

        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        player.sendMessage("<6><l>=== SkyWars Configurator ===");
        player.sendMessage("<e>Use <b>/choosemap \\<mapId> <e>to load a map");
        player.sendMessage("<e>Use <b>/swconfig <e>to configure the map");
        player.sendMessage("<7>Anvil worlds will be automatically converted to Polar format");
    }
}
