package dev.daweapon.type.skyblockgeneric.crystalhollows;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;

import java.util.Set;

public final class HardStone {
    public static final double TREASURE_CHANCE_PERCENT = 0.2;
    private static final Set<Material> MATERIALS = Set.of(Material.STONE, Material.COBBLESTONE, Material.ANDESITE,
            Material.GRANITE, Material.DIORITE, Material.TUFF);

    private HardStone() {
    }

    public static boolean isHardStone(Material material, RegionType region) {
        return material != null && CrystalHollowsZones.isCrystalHollows(region) && MATERIALS.contains(material);
    }

    public static Set<Material> materials() {
        return MATERIALS;
    }

    public static MineableBlock mineable() {
        return MineableBlock.HARD_STONE;
    }
}
