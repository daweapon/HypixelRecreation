package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSyncs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionSyncSkyBlockXPOnJoin implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        reconcile((SkyBlockPlayer) event.getPlayer());
    }

    public static void reconcile(SkyBlockPlayer player) {
        player.getSkyBlockExperience().reconcile(() -> SkyBlockXPSyncs.JOIN.forEach(sync -> sync.accept(player)));
    }
}
