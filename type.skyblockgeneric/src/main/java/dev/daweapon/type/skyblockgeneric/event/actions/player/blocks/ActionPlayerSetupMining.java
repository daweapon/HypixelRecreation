package dev.daweapon.type.skyblockgeneric.event.actions.player.blocks;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

// Resets block-break-speed attribute on player spawn for non-island servers
public class ActionPlayerSetupMining implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        var player = event.getPlayer();

        if (HypixelConst.isIslandServer()) return;

        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).clearModifiers();
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0D);
    }
}
