package dev.daweapon.type.island.events.traditional;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.SharedInstance;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.flow.PlayerFlow;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

// Loads and teleports a player into their private Island instance on first spawn.
public class ActionPlayerTeleport implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.SPAWN, order = -50)
    public void run(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (!event.isFirstSpawn()) return;

        // A cross-server /visit queues a pending-visit marker before transferring the player here -
        // if one is waiting, load the foreign island instead of the player's own (their own
        // SkyBlockIsland reference is left untouched, so leaving the visit is just a normal spawn).
        VisitManager.VisitSession pendingVisit = VisitManager.consumePendingVisit(player.getUuid());

        PlayerFlow.run(player, "island-instance/load-and-teleport", () -> {
            if (pendingVisit != null) {
                SkyBlockIsland island = SkyBlockIsland.getOrCreate(pendingVisit.islandId(), pendingVisit.hostProfileId());
                SharedInstance instance = island.getSharedInstance().join();
                player.setInstance(instance, player.getRespawnPoint());
                player.teleport(player.getRespawnPoint());
                VisitManager.markVisiting(player.getUuid(), pendingVisit);
                return;
            }

            SharedInstance instance = player.getSkyBlockIsland().getSharedInstance().join();
            player.setInstance(instance, player.getRespawnPoint());
            player.teleport(player.getRespawnPoint());
        });
    }
}
