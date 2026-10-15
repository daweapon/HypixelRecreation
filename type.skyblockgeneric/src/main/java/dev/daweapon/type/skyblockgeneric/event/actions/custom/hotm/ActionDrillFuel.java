package dev.daweapon.type.skyblockgeneric.event.actions.custom.hotm;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.drill.DrillFuel;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

public class ActionDrillFuel implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockItem held = new SkyBlockItem(player.getInventory().getItemStack(player.getHeldSlot()));
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        DrillFuel.onBlockMined(player, held, event.getMaterial(), region == null ? null : region.getType(),
                ThreadLocalRandom.current());
    }
}
