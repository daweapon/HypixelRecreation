package dev.daweapon.type.skyblockgeneric.event.actions.custom.garden;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;

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
