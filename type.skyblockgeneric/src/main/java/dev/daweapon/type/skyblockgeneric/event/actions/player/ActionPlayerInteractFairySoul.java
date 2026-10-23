package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.EntityFairySoul;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoul;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoulGates;

// Collects a fairy soul for the player when they interact with its entity
public class ActionPlayerInteractFairySoul implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (event.getTarget() instanceof EntityFairySoul entitySoul) {
            FairySoul fairySoul = entitySoul.parent;
            if (fairySoul == null || !FairySoulGates.mayCollect(player, fairySoul)) return;

            dev.daweapon.type.skyblockgeneric.technoblade.AncientPigEffects.onFairySoulClicked(player);
            fairySoul.collect(player);
        }
    }
}
