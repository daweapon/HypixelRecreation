package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.protocol.objects.orchestrator.RejoinGameProtocol;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.run.InstancedRunRejoin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Rejoining SkyBlock after a disconnect sends the player back to the Catacombs or Kuudra run they left
public class ActionRejoinInstancedRun implements HypixelEventClass {
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn() || !(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (InstancedRunRejoin.isRunServer(HypixelConst.getTypeLoader().getType())) return;
        if (!InstancedRunRejoin.tryClaim(player.getUuid(), System.currentTimeMillis())) return;
        ORCHESTRATOR.handleRequest(new RejoinGameProtocol.RejoinGameRequest(player.getUuid())).thenAccept(response -> {
            if (!(response instanceof RejoinGameProtocol.RejoinGameResponse rejoin) || !InstancedRunRejoin.shouldRejoin(rejoin)) {
                InstancedRunRejoin.release(player.getUuid());
                return;
            }
            player.sendMessage("<a>Sending you back to your run...");
            player.asProxyPlayer().transferToWithIndication(rejoin.server().uuid());
        }).exceptionally(throwable -> {
            InstancedRunRejoin.release(player.getUuid());
            return null;
        });
    }
}
