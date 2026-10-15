package dev.daweapon.type.skyblockgeneric.crystalhollows;

import lombok.Getter;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;

import java.util.HashSet;
import java.util.Set;

@Getter
public enum CrystalHollowsOre {
    RUBY(MineableBlock.RUBY_ORE, ItemType.ROUGH_RUBY_GEM, Material.RED_STAINED_GLASS, Material.RED_STAINED_GLASS_PANE),
    AMETHYST(MineableBlock.AMETHYST_ORE, ItemType.ROUGH_AMETHYST_GEM, Material.PURPLE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS_PANE),
    JADE(MineableBlock.JADE_ORE, ItemType.ROUGH_JADE_GEM, Material.LIME_STAINED_GLASS, Material.LIME_STAINED_GLASS_PANE),
    SAPPHIRE(MineableBlock.SAPPHIRE_ORE, ItemType.ROUGH_SAPPHIRE_GEM, Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS_PANE),
    AMBER(MineableBlock.AMBER_ORE, ItemType.ROUGH_AMBER_GEM, Material.ORANGE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS_PANE),
    TOPAZ(MineableBlock.TOPAZ_ORE, ItemType.ROUGH_TOPAZ_GEM, Material.YELLOW_STAINED_GLASS, Material.YELLOW_STAINED_GLASS_PANE),
    JASPER(MineableBlock.JASPER_ORE, ItemType.ROUGH_JASPER_GEM, Material.MAGENTA_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS_PANE);

    public static final int MIN_ROUGH_DROP = 3;
    public static final int MAX_ROUGH_DROP = 5;

    private final MineableBlock mineable;
    private final ItemType drop;
    private final Set<Material> materials;

    CrystalHollowsOre(MineableBlock mineable, ItemType drop, Material... materials) {
        this.mineable = mineable;
        this.drop = drop;
        this.materials = Set.of(materials);
    }

    public static CrystalHollowsOre of(Material material, RegionType region) {
        if (material == null || !CrystalHollowsZones.isCrystalHollows(region)) return null;
        for (CrystalHollowsOre ore : values()) {
            if (ore.materials.contains(material)) return ore;
        }
        return null;
    }

    public static CrystalHollowsOre of(Block block, RegionType region) {
        return of(Material.fromKey(block.name()), region);
    }

    public static Set<Material> allMaterials() {
        Set<Material> all = new HashSet<>();
        for (CrystalHollowsOre ore : values()) all.addAll(ore.materials);
        return all;
    }
}
