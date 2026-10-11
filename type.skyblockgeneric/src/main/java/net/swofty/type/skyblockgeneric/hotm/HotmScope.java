package net.swofty.type.skyblockgeneric.hotm;

import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.region.mining.MiningBlockClassification;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
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
