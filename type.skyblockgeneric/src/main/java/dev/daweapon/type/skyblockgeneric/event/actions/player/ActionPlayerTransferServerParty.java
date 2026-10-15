package dev.daweapon.type.skyblockgeneric.event.actions.player;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.proxyapi.ProxyPlayer;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.party.PartyManager;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Handles party bookkeeping after a player disconnects: server transfer vs. true disconnect
public class ActionPlayerTransferServerParty implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        // Wait 60 ticks (3 seconds) to give the player time to connect to a new server
        ScheduleUtility.delay(() -> {
            ProxyService partyService = new ProxyService(ServiceType.PARTY);
            if (!partyService.isOnline().join()) {
                return;
            }

            // Check if the player is still online on the network
            ProxyPlayer proxyPlayer = new ProxyPlayer(player.getUuid());
            boolean isOnline = proxyPlayer.isOnline().join();

            if (isOnline) {
                // Player is on another server - just a server transfer
                PartyManager.switchPartyServer(player);
            } else {
                // Player is truly offline - check if they're in a party and notify
                if (PartyManager.isInParty(player)) {
                    PartyManager.notifyPlayerDisconnect(player.getUuid());
                }
            }
        }, 60);
    }
}
