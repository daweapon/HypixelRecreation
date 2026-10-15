package dev.daweapon.type.skyblockgeneric.event.actions.player.blocks;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.PlayerTickEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.garden.tool.FarmingTools;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionGardenInstantBreak implements HypixelEventClass {
    private static final double INSTANT = 1_000_000D;
    private static final int CHECK_INTERVAL_TICKS = 4;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerTickEvent event) {
        if (!HypixelConst.isGardenServer()) return;
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (player.getAliveTicks() % CHECK_INTERVAL_TICKS != 0) return;

        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        boolean farmingTool = !held.isAir() && FarmingTools.of(held.getAttributeHandler().getTypeAsString()) != null;
        var attribute = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (farmingTool && attribute.getBaseValue() != INSTANT) attribute.setBaseValue(INSTANT);
        else if (!farmingTool && attribute.getBaseValue() == INSTANT) attribute.setBaseValue(1D);
    }
}
