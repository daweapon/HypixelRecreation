package dev.daweapon.type.theend.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermiteHigh;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermiteLow;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.item.tool.ToolPassives;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.theend.EnderNodes;

/**
 * Wiki (Endermite, End Stone): mining End Stone with anything but a Stonk has a small chance to spawn an
 * Endermite, the lower one on the island and the higher one in the Dragon's Nest. The chance is an estimate.
 */
public class ActionEndStoneEndermites implements HypixelEventClass {
    public static final double ENDERMITE_CHANCE = 1;

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced()) || event.getMaterial() != Material.END_STONE) return;
        if (EnderNodes.isCoolingDown(event.getPoint())) return;
        if (ToolPassives.holding(event.getPlayer(), "STONK_PICKAXE")) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        if (region == null) return;
        RegionType type = region.getType();
        if (type != RegionType.THE_END && type != RegionType.DRAGONS_NEST) return;
        Instance instance = event.getPlayer().getInstance();
        if (instance == null || !LootRoll.roll(event.getPlayer().getUuid(), null, ENDERMITE_CHANCE)) return;
        Pos at = new Pos(event.getPoint().blockX() + 0.5, event.getPoint().blockY() + 1, event.getPoint().blockZ() + 0.5);
        if (type == RegionType.DRAGONS_NEST) new MobEndermiteHigh().setInstance(instance, at);
        else new MobEndermiteLow().setInstance(instance, at);
    }
}
