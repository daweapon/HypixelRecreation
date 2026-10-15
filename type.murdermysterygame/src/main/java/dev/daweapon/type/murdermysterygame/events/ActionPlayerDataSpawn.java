package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.entity.EntityType;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.pvp.projectile.BowModule;
import dev.daweapon.pvp.projectile.entities.ArrowProjectile;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionPlayerDataSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, isAsync = true, phase = EventPhase.POST_SPAWN)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        final MurderMysteryPlayer player = (MurderMysteryPlayer) event.getPlayer();

        // Registers the bow module for this player
        new BowModule(player.eventNode(), (p, i) -> new ArrowProjectile(EntityType.ARROW, p));
    }
}
