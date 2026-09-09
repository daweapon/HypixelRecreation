package net.swofty.type.skyblockgeneric.event.actions.player.data;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.actions.player.ActionPlayerStrayTooFar;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.server.eventcaller.CustomEventCaller;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockScoreboard;
import net.swofty.type.skyblockgeneric.user.island.VisitManager;

// Clears a player's in-memory SkyBlock caches when they disconnect
public class ActionPlayerClearSkyBlockCache implements HypixelEventClass {

    // Must run before ActionPlayerDataSave's PERSIST-phase save/unload (order 0, default) - this action still
    // needs the SkyBlock data domain loaded (getPetData()), which no longer exists once DISCONNECT phase runs.
    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.PERSIST, order = -10)
    public void run(PlayerDisconnectEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        // remove from caches
        SkyBlockScoreboard.removeCache(player);
        player.getPetData().updatePetEntityImpl(null);
        PlayerItemOrigin.clearCache(player.getUuid());
        ActionPlayerStrayTooFar.startedStray.remove(player.getUuid());
        CustomEventCaller.clearCache(player);
        // Stop counting this player against their visited island's guest limit if they disconnect mid-visit.
        VisitManager.clearVisit(player.getUuid());

        CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
        if (coop != null) CoopDatabase.markLastSeen(coop.coopUUID(), player.getUuid());
    }
}
