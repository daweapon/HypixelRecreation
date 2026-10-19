package dev.daweapon.type.skyblockgeneric.event.actions.player.data;

import dev.daweapon.type.skyblockgeneric.user.pets.PetEquipEffects;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.MobAggroLog;
import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.actions.player.ActionPlayerStrayTooFar;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.server.eventcaller.CustomEventCaller;
import dev.daweapon.type.skyblockgeneric.trade.TradeManager;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockScoreboard;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

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
        PetEquipEffects.release(player.getUuid());
        dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks.release(player.getUuid());
        dev.daweapon.type.skyblockgeneric.hoppity.StrayRabbitService.release(player.getUuid());
        MobAggroLog.clear(player.getUuid());
        PlayerItemOrigin.clearCache(player.getUuid());
        ActionPlayerStrayTooFar.startedStray.remove(player.getUuid());
        CustomEventCaller.clearCache(player);
        // Stop counting this player against their visited island's guest limit if they disconnect mid-visit.
        VisitManager.clearVisit(player.getUuid());
        TradeManager.handleDisconnect(player);

        CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
        if (coop != null) CoopDatabase.markLastSeen(coop.coopUUID(), player.getUuid());
    }
}
