package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.tool.ToolPassives;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

public class ActionStonkExtraEndStone implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        if (event.getMaterial() != Material.END_STONE) return;
        if (!ToolPassives.holding(event.getPlayer(), "STONK_PICKAXE")) return;
        if (LootRoll.roll(event.getPlayer().getUuid(), ItemType.END_STONE, ToolPassives.STONK_EXTRA_END_STONE_CHANCE)) {
            event.getDrops().add(new SkyBlockItem(ItemType.END_STONE, 1));
        }
    }
}
