package dev.daweapon.type.skyblockgeneric.hunting;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public enum HuntrapMedium {
    LAND, UNDERWATER, UNDER_LAVA, ANY;

    public static HuntrapMedium of(boolean water, boolean lava) {
        if (lava) return UNDER_LAVA;
        return water ? UNDERWATER : LAND;
    }

    public static HuntrapMedium at(Instance instance, int x, int y, int z) {
        Block block = instance.getBlock(x, y, z);
        Block above = instance.getBlock(x, y + 1, z);
        return of(block.compare(Block.WATER) || above.compare(Block.WATER),
                block.compare(Block.LAVA) || above.compare(Block.LAVA));
    }

    public boolean accepts(HuntrapMedium actual) {
        return this == ANY || this == actual;
    }
}
