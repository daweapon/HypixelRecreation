package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.tool.ToolPassives;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;

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
