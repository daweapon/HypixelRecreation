package dev.daweapon.type.skyblockgeneric.chest;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

// Whether a chest is a single block or joined into a double chest, detected from adjacent blocks.
public enum ChestType {
    SINGLE,
    DOUBLE;

    public static ChestType from(Instance instance, Point position) {
        for (BlockFace face : BlockFace.values()) {
            Block adjacent = instance.getBlock(position.add(face.toDirection().normalX(), face.toDirection().normalY(), face.toDirection().normalZ()));
            if (adjacent.name().equals("minecraft:chest")) {
                if (face == BlockFace.EAST || face == BlockFace.WEST || face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    return ChestType.DOUBLE;
                }
            }
        }
        return ChestType.SINGLE;
    }
}
