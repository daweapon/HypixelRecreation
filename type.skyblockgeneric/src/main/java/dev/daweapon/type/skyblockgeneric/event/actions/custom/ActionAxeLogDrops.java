package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.tool.AxeLogDrops;
import dev.daweapon.type.skyblockgeneric.item.tool.ToolPassives;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

public class ActionAxeLogDrops implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        if (!AxeLogDrops.isLog(event.getMaterial())) return;
        if (ToolPassives.holding(event.getPlayer(), "EFFICIENT_AXE")) {
            AxeLogDrops.planksFor(event.getMaterial())
                    .ifPresent(planks -> event.getDrops().add(new SkyBlockItem(planks, AxeLogDrops.EFFICIENT_AXE_PLANKS)));
        }
        if (ToolPassives.holding(event.getPlayer(), "SWEET_AXE")
                && LootRoll.roll(event.getPlayer().getUuid(), ItemType.APPLE, AxeLogDrops.SWEET_AXE_APPLE_CHANCE)) {
            event.getDrops().add(new SkyBlockItem(ItemType.APPLE, 1));
        }
    }
}
