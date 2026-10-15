package net.swofty.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.RanchersBootsSpeedCap;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Farmer's Speed: swinging at nothing while wearing Rancher's Boots opens the speed cap prompt
public class ActionRanchersBootsAbility implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerHandAnimationEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!RanchersBootsSpeedCap.isWearing(player)) return;
        if (player.getTargetBlockPosition(6) != null) return;
        RanchersBootsSpeedCap.prompt(player);
    }
}
