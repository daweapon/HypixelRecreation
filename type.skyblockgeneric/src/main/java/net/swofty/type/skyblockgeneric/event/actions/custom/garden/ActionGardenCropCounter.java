package net.swofty.type.skyblockgeneric.event.actions.custom.garden;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.EnumMap;
import java.util.Map;

public class ActionGardenCropCounter implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (event.getPlayerPlaced()) return;

        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        if (region == null || region.getType() != RegionType.THE_GARDEN) return;

        Map<GardenCrop, Long> harvested = new EnumMap<>(GardenCrop.class);
        for (SkyBlockItem drop : event.getDrops()) {
            GardenCrop crop = GardenCrop.ofItem(drop.getAttributeHandler().getPotentialType());
            if (crop != null) {
                harvested.merge(crop, (long) drop.getAmount(), Long::sum);
            }
        }
        harvested.forEach((crop, amount) -> GardenService.recordCrops(event.getPlayer(), crop, amount));
    }
}
