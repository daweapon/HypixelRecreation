package dev.daweapon.type.skyblockgeneric.item.handlers.orbs;

import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.entity.ServerCrystalImpl;

import java.util.function.Function;

public record ServerOrbHandler(
        Function<ServerCrystalImpl, Block> spawnMaterialFunction
) {
}