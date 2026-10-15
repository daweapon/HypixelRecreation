package dev.daweapon.type.skyblockgeneric.glacite;

import lombok.Getter;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;

import java.util.Set;

@Getter
public enum GlaciteOre {
    UMBER(MineableBlock.UMBER_ORE, ItemType.UMBER, true,
            Material.BROWN_TERRACOTTA, Material.TERRACOTTA, Material.RED_SANDSTONE, Material.SMOOTH_RED_SANDSTONE),
    TUNGSTEN(MineableBlock.TUNGSTEN_ORE, ItemType.TUNGSTEN, true, Material.COBBLESTONE, Material.INFESTED_COBBLESTONE, Material.CLAY),
    GLACITE(MineableBlock.GLACITE_ORE, ItemType.GLACITE, true, Material.PACKED_ICE),
    AQUAMARINE(MineableBlock.AQUAMARINE_ORE, ItemType.ROUGH_AQUAMARINE_GEM, false,
            Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS_PANE),
    CITRINE(MineableBlock.CITRINE_ORE, ItemType.ROUGH_CITRINE_GEM, false,
            Material.BROWN_STAINED_GLASS, Material.BROWN_STAINED_GLASS_PANE),
    ONYX(MineableBlock.ONYX_ORE, ItemType.ROUGH_ONYX_GEM, false,
            Material.BLACK_STAINED_GLASS, Material.BLACK_STAINED_GLASS_PANE),
    PERIDOT(MineableBlock.PERIDOT_ORE, ItemType.ROUGH_PERIDOT_GEM, false,
            Material.GREEN_STAINED_GLASS, Material.GREEN_STAINED_GLASS_PANE);

    public static final long BASE_GLACITE_POWDER = 2;

    private final MineableBlock mineable;
    private final ItemType drop;
    private final boolean givesGlacitePowder;
    private final Set<Material> materials;

    GlaciteOre(MineableBlock mineable, ItemType drop, boolean givesGlacitePowder, Material... materials) {
        this.mineable = mineable;
        this.drop = drop;
        this.givesGlacitePowder = givesGlacitePowder;
        this.materials = Set.of(materials);
    }

    public static GlaciteOre of(Material material, RegionType region) {
        if (!GlaciteZones.isColdZone(region) || material == null) return null;
        for (GlaciteOre ore : values()) {
            if (ore.materials.contains(material)) return ore;
        }
        return null;
    }

    public static GlaciteOre of(Block block, RegionType region) {
        return of(Material.fromKey(block.name()), region);
    }

    public long basePowder() {
        return givesGlacitePowder ? BASE_GLACITE_POWDER : 0;
    }

    public static Set<Material> allMaterials() {
        Set<Material> all = new java.util.HashSet<>();
        for (GlaciteOre ore : values()) all.addAll(ore.materials);
        return all;
    }
}
