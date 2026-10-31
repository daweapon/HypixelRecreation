package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;

public final class MineshaftBlocks {
    public static final int SPREAD_DIVISOR = 2;

    private MineshaftBlocks() {
    }

    public static int quality(MineableBlock block) {
        if (block == null) return 0;
        return switch (block) {
            case MITHRIL_WEAK, MITHRIL_WEAK2, MITHRIL_MEDIUM, MITHRIL_MEDIUM2, MITHRIL_MEDIUM3, MITHRIL_STRONG -> 2;
            case UMBER_ORE, TUNGSTEN_ORE, GLACITE_ORE -> 4;
            case TITANIUM, RUBY_ORE -> 8;
            case SAPPHIRE_ORE, JADE_ORE, AMBER_ORE, AMETHYST_ORE, TOPAZ_ORE, AQUAMARINE_ORE, PERIDOT_ORE, CITRINE_ORE, ONYX_ORE, JASPER_ORE -> 10;
            default -> 0;
        };
    }

    public static int quality(MineableBlock block, boolean spreadBlock) {
        int base = quality(block);
        return spreadBlock ? base / SPREAD_DIVISOR : base;
    }
}
