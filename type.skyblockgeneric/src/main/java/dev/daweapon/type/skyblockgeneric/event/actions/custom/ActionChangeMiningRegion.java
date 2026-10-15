package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.entity.attribute.Attribute;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Resets a player's mining efficiency attribute when they change region on non-island servers
public class ActionChangeMiningRegion implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        SkyBlockPlayer player = event.getPlayer();

        if (HypixelConst.isIslandServer()) return;

        player.getAttribute(Attribute.MINING_EFFICIENCY).setBaseValue(0D);
    }
}
