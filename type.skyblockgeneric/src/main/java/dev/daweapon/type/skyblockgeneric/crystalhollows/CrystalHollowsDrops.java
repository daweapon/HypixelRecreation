package dev.daweapon.type.skyblockgeneric.crystalhollows;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

public final class CrystalHollowsDrops {
    public static final double GEMSTONE_MINING_XP = 15;

    public record Drop(ItemType item, int min, int max, double miningXp) {
        public int roll(java.util.random.RandomGenerator random) {
            return min >= max ? min : random.nextInt(min, max + 1);
        }
    }

    private static final Map<Material, Drop> TABLE = new LinkedHashMap<>();

    static {
        for (Material mithril : new Material[]{Material.GRAY_WOOL, Material.CYAN_TERRACOTTA, Material.LIGHT_BLUE_WOOL,
                Material.PRISMARINE, Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS}) {
            TABLE.put(mithril, new Drop(ItemType.MITHRIL, 1, 1, 6));
        }
        TABLE.put(Material.POLISHED_DIORITE, new Drop(ItemType.TITANIUM, 1, 1, 0));
        TABLE.put(Material.GOLD_BLOCK, new Drop(ItemType.GOLD_INGOT, 2, 4, 6));
        TABLE.put(Material.EMERALD_BLOCK, new Drop(ItemType.EMERALD, 2, 4, 9));
        TABLE.put(Material.DIAMOND_BLOCK, new Drop(ItemType.DIAMOND, 2, 4, 10));
        TABLE.put(Material.LAPIS_BLOCK, new Drop(ItemType.LAPIS_LAZULI, 4, 8, 7));
        TABLE.put(Material.REDSTONE_BLOCK, new Drop(ItemType.REDSTONE, 4, 8, 7));
        TABLE.put(Material.IRON_BLOCK, new Drop(ItemType.IRON_INGOT, 2, 4, 5));
        TABLE.put(Material.COAL_BLOCK, new Drop(ItemType.COAL, 2, 4, 5));
        TABLE.put(Material.QUARTZ_BLOCK, new Drop(ItemType.QUARTZ, 2, 4, 5));
        TABLE.put(Material.QUARTZ_PILLAR, new Drop(ItemType.QUARTZ, 2, 4, 5));
    }

    private CrystalHollowsDrops() {
    }

    public static Drop of(Material material, RegionType region) {
        if (material == null || !CrystalHollowsZones.isCrystalHollows(region)) return null;
        return TABLE.get(material);
    }

    public static Set<Material> materials() {
        return TABLE.keySet();
    }

    public static OptionalDouble miningXp(Material material, RegionType region) {
        if (CrystalHollowsOre.of(material, region) != null) return OptionalDouble.of(GEMSTONE_MINING_XP);
        Drop drop = of(material, region);
        return drop == null ? OptionalDouble.empty() : OptionalDouble.of(drop.miningXp());
    }
}
