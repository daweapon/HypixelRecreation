package dev.daweapon.type.skyblockgeneric.event.actions.custom.rare;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.loottable.RareDropAward;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.Set;

public class ActionRareBlockDrops implements HypixelEventClass {
    private static final double RARE_DIAMOND_CHANCE = 0.2;
    private static final double DYE_EMERALD_CHANCE = 1.0 / 5_000_000 * 100;
    private static final double DYE_MANGO_CHANCE = 1.0 / 10_000_000 * 100;
    private static final double DYE_WILD_STRAWBERRY_CHANCE = 1.0 / 150_000_000 * 100;

    private static final Set<Material> CROPS = Set.of(
            Material.WHEAT, Material.CARROT, Material.POTATO, Material.PUMPKIN, Material.MELON,
            Material.NETHER_WART, Material.SUGAR_CANE, Material.COCOA_BEANS, Material.CACTUS,
            Material.RED_MUSHROOM, Material.BROWN_MUSHROOM
    );

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        Material material = event.getMaterial();

        if (material == Material.DIAMOND_ORE && inDeepDiamondRegion(event)) {
            RareDropAward.tryAward(event.getPlayer(), ItemType.RARE_DIAMOND, RARE_DIAMOND_CHANCE);
        }
        if (material == Material.EMERALD_ORE) {
            RareDropAward.tryAward(event.getPlayer(), ItemType.DYE_EMERALD, DYE_EMERALD_CHANCE);
        }
        if (material.key().value().endsWith("_log") && !HypixelConst.isIslandServer()) {
            RareDropAward.tryAward(event.getPlayer(), ItemType.DYE_MANGO, DYE_MANGO_CHANCE);
        }
        if (CROPS.contains(material)) {
            RareDropAward.tryAward(event.getPlayer(), ItemType.DYE_WILD_STRAWBERRY, DYE_WILD_STRAWBERRY_CHANCE);
        }
    }

    private boolean inDeepDiamondRegion(CustomBlockBreakEvent event) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        if (region == null) return false;
        return region.getType() == RegionType.DIAMOND_RESERVE || region.getType() == RegionType.OBSIDIAN_SANCTUARY;
    }
}
