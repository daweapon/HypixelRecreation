package dev.daweapon.type.skyblockgeneric.fossil;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gems.GemRarity;
import dev.daweapon.type.skyblockgeneric.gems.Gemstone;

import java.util.List;

public record ChiselStats(int baseCharges, int slots, int hotmTier, double extraChargeChance, double highlightChance,
                          double extraTreasureChance, double essenceBonus) {
    private static final double[] PERCENT_BY_RARITY = {30, 40, 50, 60, 100};

    public static ChiselStats of(ItemType chisel, List<ItemType> gems) {
        int[] base = baseOf(chisel);
        if (base == null) throw new IllegalArgumentException(chisel + " is not a chisel");
        double charge = 0, highlight = 0, treasure = 0, essence = 0;
        for (ItemType gem : gems) {
            if (gem == null) continue;
            Gemstone gemstone = Gemstone.getFromItemType(gem);
            int index = gemstone == null ? -1 : gemstone.getItem().indexOf(gem);
            if (index < 0) continue;
            double percent = PERCENT_BY_RARITY[Math.min(index, GemRarity.values().length - 1)];
            switch (gemstone) {
                case AQUAMARINE -> charge += percent;
                case CITRINE -> highlight += percent;
                case ONYX -> treasure += percent;
                case PERIDOT -> essence += percent;
                default -> {}
            }
        }
        return new ChiselStats(base[0], base[1], base[2], charge, highlight, treasure, essence);
    }

    public static boolean isChisel(ItemType type) {
        return baseOf(type) != null;
    }

    private static int[] baseOf(ItemType type) {
        if (type == ItemType.CHISEL) return new int[]{16, 0, 0};
        if (type == ItemType.REINFORCED_CHISEL) return new int[]{18, 1, 8};
        if (type == ItemType.GLACITE_CHISEL) return new int[]{20, 2, 9};
        if (type == ItemType.PERFECT_CHISEL) return new int[]{22, 3, 10};
        return null;
    }
}
