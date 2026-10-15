package dev.daweapon.type.skyblockgeneric.hotm;

import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import dev.daweapon.type.skyblockgeneric.region.mining.MiningBlockClassification;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import net.minestom.server.item.Material;

public enum HotmScope {
    ALWAYS,
    MINING_ISLAND,
    GEMSTONE,
    DWARVEN_METAL,
    HARD_STONE,
    MINESHAFT;

    public boolean isGlobal() {
        return this == ALWAYS || this == MINING_ISLAND;
    }

    public boolean matches(MineableBlock block) {
        if (block == null) return false;
        MiningBlockClassification type = block.getBlockType();
        return switch (this) {
            case ALWAYS, MINING_ISLAND -> true;
            case GEMSTONE -> type.specificBlockFortune() == ItemStatistic.GEMSTONE_FORTUNE;
            case DWARVEN_METAL -> type.specificBlockFortune() == ItemStatistic.DWARVEN_METAL_FORTUNE;
            case HARD_STONE -> block.getMaterial() == Material.STONE;
            case MINESHAFT -> false;
        };
    }
}
