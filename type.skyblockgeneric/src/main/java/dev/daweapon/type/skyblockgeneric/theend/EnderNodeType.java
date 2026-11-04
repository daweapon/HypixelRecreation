package dev.daweapon.type.skyblockgeneric.theend;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;

public enum EnderNodeType {
    END_STONE(Block.END_STONE, Material.END_STONE),
    OBSIDIAN(Block.OBSIDIAN, Material.OBSIDIAN);

    private final Block block;
    private final Material material;

    EnderNodeType(Block block, Material material) {
        this.block = block;
        this.material = material;
    }

    public Block block() {
        return block;
    }

    public Material material() {
        return material;
    }
}
