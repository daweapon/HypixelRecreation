package net.swofty.type.skyblockgeneric.event.actions.custom.hotm;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.drill.DrillFuel;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
