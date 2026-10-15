package dev.daweapon.type.thegarden.events.traditional;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.SharedInstance;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.flow.PlayerFlow;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;

public class ActionPlayerTeleport implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.SPAWN, order = -50)
    public void run(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (!event.isFirstSpawn()) return;

        PlayerFlow.run(player, "garden-instance/load-and-teleport", () -> {
            SharedInstance instance = SkyBlockGarden.of(player).getSharedInstance().join();
            player.setReadyForEvents();
            player.setInstance(instance, player.getRespawnPoint());
            player.teleport(player.getRespawnPoint());
            SkyBlockGarden.getPresence().arrived(player, instance);
        });
    }
}
