package net.swofty.type.skyblockgeneric.crystalhollows;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobThyst;

import java.util.concurrent.ThreadLocalRandom;

public final class ThystSpawner {
    private static final double SPAWN_CHANCE = 0.2;

    private ThystSpawner() {
    }

    public static void onBreak(CrystalHollowsOre ore, Instance instance, Point blockPosition) {
        if (ore != CrystalHollowsOre.AMETHYST || instance == null) return;
        if (ThreadLocalRandom.current().nextDouble() >= SPAWN_CHANCE) return;
        new MobThyst().setInstance(instance, new Pos(blockPosition.x() + 0.5, blockPosition.y(), blockPosition.z() + 0.5));
    }
}
