package dev.daweapon.type.skyblockgeneric.user.island;

import net.hollowcube.polar.PolarWorld;

/** Upgrades a stored island world by exactly one version. */
@FunctionalInterface
public interface IslandMigration {
    PolarWorld migrate(PolarWorld world, SkyBlockIsland island);
}
