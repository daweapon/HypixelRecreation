package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.EntityFairySoul;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulGates;

// Collects a fairy soul for the player when they interact with its entity
public class ActionPlayerInteractFairySoul implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (event.getTarget() instanceof EntityFairySoul entitySoul) {
            FairySoul fairySoul = entitySoul.parent;
            if (fairySoul == null || !FairySoulGates.mayCollect(player, fairySoul)) return;

            fairySoul.collect(player);
        }
    }
}
